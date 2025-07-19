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
import de.vptr.midas.gui.dto.UserRankDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import de.vptr.midas.gui.service.AuthService;
import de.vptr.midas.gui.service.UserRankService;
import de.vptr.midas.gui.util.NotificationUtil;
import jakarta.inject.Inject;

@Route(value = "ranks", layout = MainLayout.class)
public class UserRankView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(UserRankView.class);

    @Inject
    UserRankService rankService;

    @Inject
    AuthService authService;

    private Grid<UserRankDto> grid;
    private TextField searchField;
    private Button searchButton;

    private Dialog rankDialog;
    private Binder<UserRankDto> binder;
    private UserRankDto currentRank;

    public UserRankView() {
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
        this.loadRanksAsync();
    }

    private void loadRanksAsync() {
        LOG.info("Starting async rank loading");

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
            LOG.info("Making REST call to load ranks");
            try {
                return this.rankService.getAllRanks(authHeader);
            } catch (final AuthenticationException e) {
                LOG.error("Authentication failed while loading ranks", e);
                throw e;
            } catch (final ServiceException e) {
                LOG.error("Service error while loading ranks", e);
                throw e;
            } catch (final Exception e) {
                LOG.error("Error loading ranks", e);
                throw new RuntimeException("Failed to load ranks", e);
            }
        }).orTimeout(30, TimeUnit.SECONDS)
                .whenComplete((ranks, throwable) -> {
                    this.getUI().ifPresent(ui -> ui.access(() -> {
                        if (throwable != null) {
                            LOG.error("Error loading ranks: {}", throwable.getMessage(), throwable);
                            NotificationUtil.showError("Failed to load ranks: " + throwable.getMessage());
                        } else {
                            LOG.info("Successfully loaded {} ranks", ranks.size());
                            this.grid.setItems(ranks);
                        }
                    }));
                });
    }

    private void buildUI() {
        this.removeAll();

        // Header
        final var header = new H1("User Ranks");
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

        // Rank dialog
        this.createRankDialog();
    }

    private HorizontalLayout createFilterLayout() {
        final var layout = new HorizontalLayout();
        layout.setAlignItems(Alignment.END);
        layout.setSpacing(true);

        this.searchField = new TextField("Search Ranks");
        this.searchField.setPlaceholder("Search by name or description...");
        this.searchField.setWidth("300px");

        this.searchButton = new Button("Search", e -> this.searchRanks());
        this.searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(this.searchField, this.searchButton);
        return layout;
    }

    private HorizontalLayout createButtonLayout() {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var createButton = new CreateButton(e -> this.openRankDialog(null));
        final var refreshButton = new RefreshButton(e -> this.loadRanksAsync());

        layout.add(createButton, refreshButton);
        return layout;
    }

    private void createGrid() {
        this.grid = new Grid<>(UserRankDto.class, false);
        this.grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        this.grid.setSizeFull();

        // Configure columns
        this.grid.addColumn(rank -> rank.id).setHeader("ID").setWidth("80px").setFlexGrow(0);

        // Make the name column clickable
        this.grid.addComponentColumn(rank -> {
            final var nameSpan = new Span(rank.name);
            nameSpan.getStyle().set("color", "var(--lumo-primary-text-color)");
            nameSpan.getStyle().set("cursor", "pointer");
            nameSpan.getStyle().set("width", "100%");
            nameSpan.getStyle().set("display", "block");
            nameSpan.addClickListener(e -> this.openRankDialog(rank));
            return nameSpan;
        }).setHeader("Name").setFlexGrow(2);

        this.grid.addComponentColumn(rank -> {
            final var checkbox = new Checkbox();
            checkbox.setValue(rank.postAdd != null ? rank.postAdd : false);
            checkbox.setReadOnly(true);
            return checkbox;
        }).setHeader("Post Permissions").setWidth("120px").setFlexGrow(0);

        this.grid.addComponentColumn(rank -> {
            final var checkbox = new Checkbox();
            checkbox.setValue(rank.userAdd != null ? rank.userAdd : false);
            checkbox.setReadOnly(true);
            return checkbox;
        }).setHeader("User Permissions").setWidth("120px").setFlexGrow(0);

        // Add action column
        this.grid.addComponentColumn(this::createActionButtons).setHeader("Actions").setWidth("150px").setFlexGrow(0);
    }

    private HorizontalLayout createActionButtons(final UserRankDto rank) {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var editButton = new EditButton(e -> this.openRankDialog(rank));
        final var deleteButton = new DeleteButton(e -> this.deleteRank(rank));

        layout.add(editButton, deleteButton);
        return layout;
    }

    private void createRankDialog() {
        this.rankDialog = new Dialog();
        this.rankDialog.setWidth("500px");
        this.rankDialog.setHeight("450px");
        this.rankDialog.setCloseOnEsc(true);
        this.rankDialog.setCloseOnOutsideClick(false);

        this.binder = new Binder<>(UserRankDto.class);
    }

    private void openRankDialog(final UserRankDto rank) {
        this.rankDialog.removeAll();
        this.currentRank = rank != null ? rank : new UserRankDto();

        final var title = new H3(rank != null ? "Edit Rank" : "Create Rank");

        final var form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Form fields
        final var nameField = new TextField("Name");
        nameField.setRequired(true);
        nameField.setWidthFull();

        final var postAddField = new Checkbox("Can Add Posts");
        final var postEditField = new Checkbox("Can Edit Posts");
        final var postDeleteField = new Checkbox("Can Delete Posts");

        final var userAddField = new Checkbox("Can Add Users");
        final var userEditField = new Checkbox("Can Edit Users");
        final var userDeleteField = new Checkbox("Can Delete Users");

        // Bind fields
        this.binder.bind(nameField, rank1 -> rank1.name, (rank1, value) -> rank1.name = value);
        this.binder.bind(postAddField, rank1 -> rank1.postAdd != null ? rank1.postAdd : false,
                (rank1, value) -> rank1.postAdd = value);
        this.binder.bind(postEditField, rank1 -> rank1.postEdit != null ? rank1.postEdit : false,
                (rank1, value) -> rank1.postEdit = value);
        this.binder.bind(postDeleteField, rank1 -> rank1.postDelete != null ? rank1.postDelete : false,
                (rank1, value) -> rank1.postDelete = value);
        this.binder.bind(userAddField, rank1 -> rank1.userAdd != null ? rank1.userAdd : false,
                (rank1, value) -> rank1.userAdd = value);
        this.binder.bind(userEditField, rank1 -> rank1.userEdit != null ? rank1.userEdit : false,
                (rank1, value) -> rank1.userEdit = value);
        this.binder.bind(userDeleteField, rank1 -> rank1.userDelete != null ? rank1.userDelete : false,
                (rank1, value) -> rank1.userDelete = value);

        form.add(nameField, postAddField, postEditField, postDeleteField,
                userAddField, userEditField, userDeleteField);

        // Button layout
        final var buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        final var saveButton = new Button("Save", e -> this.saveRank());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final var cancelButton = new Button("Cancel", e -> this.rankDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttonLayout.add(saveButton, cancelButton);

        final var dialogLayout = new VerticalLayout(title, form, buttonLayout);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(false);
        dialogLayout.setSizeFull();

        this.rankDialog.add(dialogLayout);

        // Load current rank data
        this.binder.readBean(this.currentRank);

        this.rankDialog.open();
    }

    private void saveRank() {
        try {
            this.binder.writeBean(this.currentRank);

            if (this.currentRank.id == null) {
                this.rankService.createRank(this.currentRank);
                NotificationUtil.showSuccess("Rank created successfully");
            } else {
                this.rankService.updateRank(this.currentRank);
                NotificationUtil.showSuccess("Rank updated successfully");
            }

            this.rankDialog.close();
            this.loadRanksAsync();

        } catch (final ValidationException e) {
            NotificationUtil.showError("Please check the form for errors");
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error saving rank: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error saving rank", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void deleteRank(final UserRankDto rank) {
        try {
            if (this.rankService.deleteRank(rank.id)) {
                NotificationUtil.showSuccess("Rank deleted successfully");
                this.loadRanksAsync();
            } else {
                NotificationUtil.showError("Failed to delete rank");
            }
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error deleting rank: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error deleting rank", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void searchRanks() {
        // For now, just reload all ranks
        // In a real implementation, you would filter based on the search field
        this.loadRanksAsync();
    }
}
