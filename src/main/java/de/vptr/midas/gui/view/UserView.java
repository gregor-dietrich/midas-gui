package de.vptr.midas.gui.view;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import de.vptr.midas.gui.component.CreateButton;
import de.vptr.midas.gui.component.DeleteButton;
import de.vptr.midas.gui.component.EditButton;
import de.vptr.midas.gui.component.RefreshButton;
import de.vptr.midas.gui.dto.UserDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import de.vptr.midas.gui.service.AuthService;
import de.vptr.midas.gui.service.UserService;
import de.vptr.midas.gui.util.NotificationUtil;
import jakarta.inject.Inject;

@Route(value = "users", layout = MainLayout.class)
public class UserView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(UserView.class);

    @Inject
    UserService userService;

    @Inject
    AuthService authService;

    private Grid<UserDto> grid;
    private TextField searchField;
    private Button searchButton;

    private Dialog userDialog;
    private Binder<UserDto> binder;
    private UserDto currentUser;

    public UserView() {
        this.setSizeFull();
        this.setPadding(true);
        this.setSpacing(true);
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        if (!this.authService.isAuthenticated()) {
            event.forwardTo(LoginView.class);
            return;
        }

        this.buildUI();
        this.loadUsersAsync();
    }

    private void loadUsersAsync() {
        LOG.info("Starting async user loading");

        // Capture the auth header in the UI thread where VaadinSession is available
        final String authHeader;
        try {
            authHeader = this.authService.getBasicAuthHeader();
        } catch (final Exception e) {
            LOG.error("Failed to get auth header", e);
            this.getUI().ifPresent(ui -> ui.access(() -> {
                NotificationUtil.showError("Authentication failed");
            }));
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            LOG.info("Making REST call to load users");
            try {
                return this.userService.getAllUsers(authHeader);
            } catch (final AuthenticationException e) {
                LOG.error("Authentication failed while loading users", e);
                throw e;
            } catch (final ServiceException e) {
                LOG.error("Service error while loading users", e);
                throw e;
            } catch (final Exception e) {
                LOG.error("Error loading users", e);
                throw new RuntimeException("Failed to load users", e);
            }
        }).orTimeout(30, TimeUnit.SECONDS)
                .whenComplete((users, throwable) -> {
                    this.getUI().ifPresent(ui -> ui.access(() -> {
                        if (throwable != null) {
                            LOG.error("Error loading users: {}", throwable.getMessage(), throwable);
                            NotificationUtil.showError("Failed to load users: " + throwable.getMessage());
                        } else {
                            LOG.info("Successfully loaded {} users", users.size());
                            this.grid.setItems(users);
                        }
                    }));
                });
    }

    private void buildUI() {
        this.removeAll();

        // Header
        final var header = new H1("Users");
        this.add(header);

        // Filter and search controls
        final var filterLayout = this.createFilterLayout();
        this.add(filterLayout);

        // Action buttons
        final var buttonLayout = this.createButtonLayout();
        this.add(buttonLayout);

        // Grid
        this.createGrid();
        this.add(this.grid);

        // User dialog
        this.createUserDialog();
    }

    private HorizontalLayout createFilterLayout() {
        final var layout = new HorizontalLayout();
        layout.setAlignItems(Alignment.END);
        layout.setSpacing(true);

        this.searchField = new TextField("Search Users");
        this.searchField.setPlaceholder("Search by username or email...");
        this.searchField.setWidth("300px");

        this.searchButton = new Button("Search", e -> this.searchUsers());
        this.searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(this.searchField, this.searchButton);
        return layout;
    }

    private HorizontalLayout createButtonLayout() {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var createButton = new CreateButton(e -> this.openUserDialog(null));
        final var refreshButton = new RefreshButton(e -> this.loadUsersAsync());

        layout.add(createButton, refreshButton);
        return layout;
    }

    private void createGrid() {
        this.grid = new Grid<>(UserDto.class, false);
        this.grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        this.grid.setSizeFull();

        // Configure columns
        this.grid.addColumn(user -> user.id).setHeader("ID").setWidth("80px").setFlexGrow(0);

        // Make the username column clickable
        this.grid.addComponentColumn(user -> {
            final var usernameSpan = new Span(user.username);
            usernameSpan.getStyle().set("color", "var(--lumo-primary-text-color)");
            usernameSpan.getStyle().set("cursor", "pointer");
            usernameSpan.getStyle().set("width", "100%");
            usernameSpan.getStyle().set("display", "block");
            usernameSpan.addClickListener(e -> this.openUserDialog(user));
            return usernameSpan;
        }).setHeader("Username").setFlexGrow(1);

        this.grid.addColumn(user -> user.email).setHeader("Email").setFlexGrow(1);
        this.grid.addColumn(user -> user.rank != null ? user.rank.getName() : "").setHeader("Rank").setWidth("120px")
                .setFlexGrow(0);
        this.grid.addComponentColumn(user -> {
            final var checkbox = new Checkbox();
            checkbox.setValue(user.activated != null ? user.activated : false);
            checkbox.setReadOnly(true);
            return checkbox;
        }).setHeader("Activated").setWidth("100px").setFlexGrow(0);
        this.grid.addComponentColumn(user -> {
            final var checkbox = new Checkbox();
            checkbox.setValue(user.banned != null ? user.banned : false);
            checkbox.setReadOnly(true);
            return checkbox;
        }).setHeader("Banned").setWidth("100px").setFlexGrow(0);
        this.grid.addColumn(user -> user.created).setHeader("Created").setWidth("150px").setFlexGrow(0);
        this.grid.addColumn(user -> user.lastLogin).setHeader("Last Login").setWidth("150px").setFlexGrow(0);

        // Add action column
        this.grid.addComponentColumn(this::createActionButtons).setHeader("Actions").setWidth("150px").setFlexGrow(0);
    }

    private HorizontalLayout createActionButtons(final UserDto user) {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var editButton = new EditButton(e -> this.openUserDialog(user));
        final var deleteButton = new DeleteButton(e -> this.deleteUser(user));

        layout.add(editButton, deleteButton);
        return layout;
    }

    private void createUserDialog() {
        this.userDialog = new Dialog();
        this.userDialog.setWidth("500px");
        this.userDialog.setCloseOnEsc(true);
        this.userDialog.setCloseOnOutsideClick(false);

        this.binder = new Binder<>(UserDto.class);
    }

    private void openUserDialog(final UserDto user) {
        this.userDialog.removeAll();
        this.currentUser = user != null ? user : new UserDto();

        final var title = new H3(user != null ? "Edit User" : "Create User");

        final var form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Form fields
        final var usernameField = new TextField("Username");
        usernameField.setRequired(true);

        final var emailField = new EmailField("Email");
        emailField.setRequired(true);

        final var passwordField = new TextField("Password");
        if (user == null) {
            passwordField.setRequired(true);
        }

        final var activatedField = new Checkbox("Activated");
        final var bannedField = new Checkbox("Banned");

        // Bind fields
        this.binder.bind(usernameField, user1 -> user1.username, (user1, value) -> user1.username = value);
        this.binder.bind(emailField, user1 -> user1.email, (user1, value) -> user1.email = value);
        this.binder.bind(passwordField, user1 -> user1.password, (user1, value) -> user1.password = value);
        this.binder.bind(activatedField, user1 -> user1.activated != null ? user1.activated : false,
                (user1, value) -> user1.activated = value);
        this.binder.bind(bannedField, user1 -> user1.banned != null ? user1.banned : false,
                (user1, value) -> user1.banned = value);

        form.add(usernameField, emailField, passwordField, activatedField, bannedField);

        // Button layout
        final var buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        final var saveButton = new Button("Save", e -> this.saveUser());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final var cancelButton = new Button("Cancel", e -> this.userDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttonLayout.add(saveButton, cancelButton);

        final var dialogLayout = new VerticalLayout(title, form, buttonLayout);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(false);

        this.userDialog.add(dialogLayout);

        // Load current user data
        this.binder.readBean(this.currentUser);

        this.userDialog.open();
    }

    private void saveUser() {
        try {
            this.binder.writeBean(this.currentUser);

            if (this.currentUser.id == null) {
                this.userService.createUser(this.currentUser);
                NotificationUtil.showSuccess("User created successfully");
            } else {
                this.userService.updateUser(this.currentUser);
                NotificationUtil.showSuccess("User updated successfully");
            }

            this.userDialog.close();
            this.loadUsersAsync();

        } catch (final ValidationException e) {
            NotificationUtil.showError("Please check the form for errors");
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error saving user: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error saving user", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void deleteUser(final UserDto user) {
        try {
            if (this.userService.deleteUser(user.id)) {
                NotificationUtil.showSuccess("User deleted successfully");
                this.loadUsersAsync();
            } else {
                NotificationUtil.showError("Failed to delete user");
            }
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error deleting user: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error deleting user", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void searchUsers() {
        // For now, just reload all users
        // In a real implementation, you would filter based on the search field
        this.loadUsersAsync();
    }
}
