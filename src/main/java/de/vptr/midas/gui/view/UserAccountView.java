package de.vptr.midas.gui.view;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
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
import de.vptr.midas.gui.dto.UserAccountDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import de.vptr.midas.gui.service.AuthService;
import de.vptr.midas.gui.service.UserAccountService;
import de.vptr.midas.gui.util.NotificationUtil;
import jakarta.inject.Inject;

@Route(value = "accounts", layout = MainLayout.class)
public class UserAccountView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(UserAccountView.class);

    @Inject
    UserAccountService accountService;

    @Inject
    AuthService authService;

    private Grid<UserAccountDto> grid;
    private TextField searchField;
    private Button searchButton;
    private NumberField userIdField;
    private Button filterByUserButton;

    private Dialog accountDialog;
    private Binder<UserAccountDto> binder;
    private UserAccountDto currentAccount;

    public UserAccountView() {
        this.setSizeFull();
        this.setSpacing(true);
        this.setPadding(true);
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        LOG.info("UserAccountView.beforeEnter - Starting view initialization");
        this.buildUI();
        this.loadAccountsAsync();
        LOG.info("UserAccountView.beforeEnter - View initialization completed");
    }

    private void loadAccountsAsync() {
        LOG.info("Starting async account loading");

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
            LOG.info("Making REST call to load accounts");
            try {
                return this.accountService.getAllAccounts(authHeader);
            } catch (final AuthenticationException e) {
                LOG.error("Authentication failed while loading accounts", e);
                throw e;
            } catch (final ServiceException e) {
                LOG.error("Service error while loading accounts", e);
                throw e;
            } catch (final Exception e) {
                LOG.error("Error loading accounts", e);
                throw new RuntimeException("Failed to load accounts", e);
            }
        }).orTimeout(30, TimeUnit.SECONDS)
                .whenComplete((accounts, throwable) -> {
                    this.getUI().ifPresent(ui -> ui.access(() -> {
                        if (throwable != null) {
                            LOG.error("Error loading accounts: {}", throwable.getMessage(), throwable);
                            NotificationUtil.showError("Failed to load accounts: " + throwable.getMessage());
                            LOG.info("Successfully loaded 0 accounts");
                        } else {
                            LOG.info("Successfully loaded {} accounts", accounts.size());
                            this.grid.setItems(accounts);
                        }
                    }));
                });
    }

    private void buildUI() {
        this.removeAll();

        // Header
        final var header = new H1("User Accounts");
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

        // Account dialog
        this.createAccountDialog();
    }

    private HorizontalLayout createFilterLayout() {
        final var filterLayout = new HorizontalLayout();
        filterLayout.setAlignItems(Alignment.END);
        filterLayout.setSpacing(true);

        // Search field
        this.searchField = new TextField("Search Accounts");
        this.searchField.setPlaceholder("Enter account name...");
        this.searchField.setWidth("200px");

        this.searchButton = new Button("Search", e -> this.searchAccounts());
        this.searchButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // User filter
        this.userIdField = new NumberField("Filter by User ID");
        this.userIdField.setWidth("150px");

        this.filterByUserButton = new Button("Filter by User", e -> this.filterByUser());
        this.filterByUserButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        filterLayout.add(this.searchField, this.searchButton, this.userIdField, this.filterByUserButton);
        return filterLayout;
    }

    private HorizontalLayout createButtonLayout() {
        final var buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        final var refreshButton = new RefreshButton(e -> this.loadAccountsAsync());
        final var createButton = new CreateButton(e -> this.openAccountDialog(null), "Create Account");

        buttonLayout.add(refreshButton, createButton);
        return buttonLayout;
    }

    private void createGrid() {
        this.grid = new Grid<>(UserAccountDto.class, false);
        this.grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        this.grid.setSizeFull();

        // Configure columns
        this.grid.addColumn(account -> account.id).setHeader("ID").setWidth("80px").setFlexGrow(0);

        // Make the name column clickable
        this.grid.addComponentColumn(account -> {
            final var nameSpan = new Span(account.name);
            nameSpan.getStyle().set("color", "var(--lumo-primary-text-color)");
            nameSpan.getStyle().set("cursor", "pointer");
            nameSpan.getStyle().set("width", "100%");
            nameSpan.getStyle().set("display", "block");
            nameSpan.addClickListener(e -> this.showPayments(account));
            return nameSpan;
        }).setHeader("Name").setFlexGrow(1);

        // Add action column
        this.grid.addComponentColumn(this::createActionButtons).setHeader("Actions").setWidth("200px").setFlexGrow(0);
    }

    private HorizontalLayout createActionButtons(final UserAccountDto account) {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var editButton = new EditButton(e -> this.openAccountDialog(account));
        final var deleteButton = new DeleteButton(e -> this.deleteAccount(account));

        layout.add(editButton, deleteButton);
        return layout;
    }

    private void createAccountDialog() {
        this.accountDialog = new Dialog();
        this.accountDialog.setWidth("400px");
        this.accountDialog.setCloseOnEsc(true);
        this.accountDialog.setCloseOnOutsideClick(false);

        this.binder = new Binder<>(UserAccountDto.class);
    }

    private void openAccountDialog(final UserAccountDto account) {
        this.accountDialog.removeAll();
        this.currentAccount = account != null ? account : new UserAccountDto();

        final var title = new H3(account != null ? "Edit Account" : "Create Account");

        final var form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Form fields
        final var nameField = new TextField("Account Name");
        nameField.setRequired(true);

        // Bind fields
        this.binder.bind(nameField, account1 -> account1.name, (account1, value) -> account1.name = value);

        form.add(nameField);

        // Button layout
        final var buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        final var saveButton = new Button("Save", e -> this.saveAccount());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final var cancelButton = new Button("Cancel", e -> this.accountDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttonLayout.add(saveButton, cancelButton);

        final var dialogLayout = new VerticalLayout(title, form, buttonLayout);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(false);

        this.accountDialog.add(dialogLayout);

        // Load current account data
        this.binder.readBean(this.currentAccount);

        this.accountDialog.open();
    }

    private void saveAccount() {
        try {
            this.binder.writeBean(this.currentAccount);

            if (this.currentAccount.id == null) {
                this.accountService.createAccount(this.currentAccount);
                NotificationUtil.showSuccess("Account created successfully");
            } else {
                this.accountService.updateAccount(this.currentAccount);
                NotificationUtil.showSuccess("Account updated successfully");
            }

            this.accountDialog.close();
            this.loadAccountsAsync();

        } catch (final ValidationException e) {
            NotificationUtil.showError("Please check the form for errors");
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error saving account: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error saving account", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void deleteAccount(final UserAccountDto account) {
        try {
            if (this.accountService.deleteAccount(account.id)) {
                NotificationUtil.showSuccess("Account deleted successfully");
                this.loadAccountsAsync();
            } else {
                NotificationUtil.showError("Failed to delete account");
            }
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error deleting account: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error deleting account", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void searchAccounts() {
        final String query = this.searchField.getValue();
        if (query == null || query.trim().isEmpty()) {
            NotificationUtil.showWarning("Please enter a search query");
            return;
        }

        try {
            final List<UserAccountDto> accounts = this.accountService.searchAccounts(query.trim());
            this.grid.setItems(accounts);
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error searching accounts: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error searching accounts", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void filterByUser() {
        final Double userId = this.userIdField.getValue();
        if (userId == null) {
            NotificationUtil.showWarning("Please enter a user ID");
            return;
        }

        try {
            final List<UserAccountDto> accounts = this.accountService.getAccountsByUser(userId.longValue());
            this.grid.setItems(accounts);
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error filtering accounts: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error filtering accounts", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void showPayments(final UserAccountDto account) {
        this.getUI().ifPresent(ui -> ui.navigate("payments"));
        NotificationUtil.showInfo("Navigated to payments view. You can filter by account ID: " + account.id);
    }
}
