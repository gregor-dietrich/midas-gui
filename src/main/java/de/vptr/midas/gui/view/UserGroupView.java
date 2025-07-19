package de.vptr.midas.gui.view;

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
import de.vptr.midas.gui.dto.UserGroupDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import de.vptr.midas.gui.service.AuthService;
import de.vptr.midas.gui.service.UserGroupService;
import de.vptr.midas.gui.util.NotificationUtil;
import jakarta.inject.Inject;

@Route(value = "groups", layout = MainLayout.class)
public class UserGroupView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(UserGroupView.class);

    @Inject
    UserGroupService groupService;

    @Inject
    AuthService authService;

    private Grid<UserGroupDto> grid;
    private TextField searchField;
    private Button searchButton;

    private Dialog groupDialog;
    private Binder<UserGroupDto> binder;
    private UserGroupDto currentGroup;

    public UserGroupView() {
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
        this.loadGroupsAsync();
    }

    private void loadGroupsAsync() {
        LOG.info("Starting async group loading");

        CompletableFuture.supplyAsync(() -> {
            LOG.info("Making REST call to load groups");
            try {
                return this.groupService.getAllGroups();
            } catch (final AuthenticationException e) {
                LOG.error("Authentication failed while loading groups", e);
                throw e;
            } catch (final ServiceException e) {
                LOG.error("Service error while loading groups", e);
                throw e;
            } catch (final Exception e) {
                LOG.error("Error loading groups", e);
                throw new RuntimeException("Failed to load groups", e);
            }
        }).orTimeout(30, TimeUnit.SECONDS)
                .whenComplete((groups, throwable) -> {
                    this.getUI().ifPresent(ui -> ui.access(() -> {
                        if (throwable != null) {
                            LOG.error("Error loading groups: {}", throwable.getMessage(), throwable);
                            NotificationUtil.showError("Failed to load groups: " + throwable.getMessage());
                        } else {
                            LOG.info("Successfully loaded {} groups", groups.size());
                            this.grid.setItems(groups);
                        }
                    }));
                });
    }

    private void buildUI() {
        this.removeAll();

        // Header
        final var header = new H1("User Groups");
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

        // Group dialog
        this.createGroupDialog();
    }

    private HorizontalLayout createFilterLayout() {
        final var layout = new HorizontalLayout();
        layout.setAlignItems(Alignment.END);
        layout.setSpacing(true);

        this.searchField = new TextField("Search Groups");
        this.searchField.setPlaceholder("Search by name or description...");
        this.searchField.setWidth("300px");

        this.searchButton = new Button("Search", e -> this.searchGroups());
        this.searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(this.searchField, this.searchButton);
        return layout;
    }

    private HorizontalLayout createButtonLayout() {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var createButton = new CreateButton(e -> this.openGroupDialog(null));
        final var refreshButton = new RefreshButton(e -> this.loadGroupsAsync());

        layout.add(createButton, refreshButton);
        return layout;
    }

    private void createGrid() {
        this.grid = new Grid<>(UserGroupDto.class, false);
        this.grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        this.grid.setSizeFull();

        // Configure columns
        this.grid.addColumn(group -> group.id).setHeader("ID").setWidth("80px").setFlexGrow(0);

        // Make the name column clickable
        this.grid.addComponentColumn(group -> {
            final var nameSpan = new Span(group.name);
            nameSpan.getStyle().set("color", "var(--lumo-primary-text-color)");
            nameSpan.getStyle().set("cursor", "pointer");
            nameSpan.getStyle().set("width", "100%");
            nameSpan.getStyle().set("display", "block");
            nameSpan.addClickListener(e -> this.openGroupDialog(group));
            return nameSpan;
        }).setHeader("Name").setFlexGrow(2);

        this.grid.addColumn(group -> group.userCount != null ? group.userCount.toString() : "0")
                .setHeader("User Count")
                .setWidth("120px")
                .setFlexGrow(0);

        this.grid.addColumn(group -> group.created).setHeader("Created").setWidth("150px").setFlexGrow(0);

        // Add action column
        this.grid.addComponentColumn(this::createActionButtons).setHeader("Actions").setWidth("150px").setFlexGrow(0);
    }

    private HorizontalLayout createActionButtons(final UserGroupDto group) {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var editButton = new EditButton(e -> this.openGroupDialog(group));
        final var deleteButton = new DeleteButton(e -> this.deleteGroup(group));

        layout.add(editButton, deleteButton);
        return layout;
    }

    private void createGroupDialog() {
        this.groupDialog = new Dialog();
        this.groupDialog.setWidth("500px");
        this.groupDialog.setHeight("400px");
        this.groupDialog.setCloseOnEsc(true);
        this.groupDialog.setCloseOnOutsideClick(false);

        this.binder = new Binder<>(UserGroupDto.class);
    }

    private void openGroupDialog(final UserGroupDto group) {
        this.groupDialog.removeAll();
        this.currentGroup = group != null ? group : new UserGroupDto();

        final var title = new H3(group != null ? "Edit Group" : "Create Group");

        final var form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Form fields
        final var nameField = new TextField("Name");
        nameField.setRequired(true);
        nameField.setWidthFull();

        // Bind fields
        this.binder.bind(nameField, group1 -> group1.name, (group1, value) -> group1.name = value);

        form.add(nameField);

        // Button layout
        final var buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        final var saveButton = new Button("Save", e -> this.saveGroup());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final var cancelButton = new Button("Cancel", e -> this.groupDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttonLayout.add(saveButton, cancelButton);

        final var dialogLayout = new VerticalLayout(title, form, buttonLayout);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(false);
        dialogLayout.setSizeFull();

        this.groupDialog.add(dialogLayout);

        // Load current group data
        this.binder.readBean(this.currentGroup);

        this.groupDialog.open();
    }

    private void saveGroup() {
        try {
            this.binder.writeBean(this.currentGroup);

            if (this.currentGroup.id == null) {
                this.groupService.createGroup(this.currentGroup);
                NotificationUtil.showSuccess("Group created successfully");
            } else {
                this.groupService.updateGroup(this.currentGroup);
                NotificationUtil.showSuccess("Group updated successfully");
            }

            this.groupDialog.close();
            this.loadGroupsAsync();

        } catch (final ValidationException e) {
            NotificationUtil.showError("Please check the form for errors");
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error saving group: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error saving group", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void deleteGroup(final UserGroupDto group) {
        try {
            if (this.groupService.deleteGroup(group.id)) {
                NotificationUtil.showSuccess("Group deleted successfully");
                this.loadGroupsAsync();
            } else {
                NotificationUtil.showError("Failed to delete group");
            }
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error deleting group: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error deleting group", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void searchGroups() {
        // For now, just reload all groups
        // In a real implementation, you would filter based on the search field
        this.loadGroupsAsync();
    }
}
