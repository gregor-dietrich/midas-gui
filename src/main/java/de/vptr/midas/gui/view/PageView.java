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
import com.vaadin.flow.component.textfield.TextArea;
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
import de.vptr.midas.gui.dto.PageDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import de.vptr.midas.gui.service.AuthService;
import de.vptr.midas.gui.service.PageService;
import de.vptr.midas.gui.util.NotificationUtil;
import jakarta.inject.Inject;

@Route(value = "pages", layout = MainLayout.class)
public class PageView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(PageView.class);

    @Inject
    PageService pageService;

    @Inject
    AuthService authService;

    private Grid<PageDto> grid;
    private TextField searchField;
    private Button searchButton;
    private Button showPublishedButton;

    private Dialog pageDialog;
    private Binder<PageDto> binder;
    private PageDto currentPage;

    public PageView() {
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
        this.loadPagesAsync();
    }

    private void loadPagesAsync() {
        LOG.info("Starting async page loading");

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
            LOG.info("Making REST call to load pages");
            try {
                return this.pageService.getAllPages(authHeader);
            } catch (final AuthenticationException e) {
                LOG.error("Authentication failed while loading pages", e);
                throw e;
            } catch (final ServiceException e) {
                LOG.error("Service error while loading pages", e);
                throw e;
            } catch (final Exception e) {
                LOG.error("Error loading pages", e);
                throw new RuntimeException("Failed to load pages", e);
            }
        }).orTimeout(30, TimeUnit.SECONDS)
                .whenComplete((pages, throwable) -> {
                    this.getUI().ifPresent(ui -> ui.access(() -> {
                        if (throwable != null) {
                            LOG.error("Error loading pages: {}", throwable.getMessage(), throwable);
                            NotificationUtil.showError("Failed to load pages: " + throwable.getMessage());
                        } else {
                            LOG.info("Successfully loaded {} pages", pages.size());
                            this.grid.setItems(pages);
                        }
                    }));
                });
    }

    private void buildUI() {
        this.removeAll();

        // Header
        final var header = new H1("Pages");
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

        // Page dialog
        this.createPageDialog();
    }

    private HorizontalLayout createFilterLayout() {
        final var layout = new HorizontalLayout();
        layout.setAlignItems(Alignment.END);
        layout.setSpacing(true);

        this.searchField = new TextField("Search Pages");
        this.searchField.setPlaceholder("Search by title or content...");
        this.searchField.setWidth("300px");

        this.searchButton = new Button("Search", e -> this.searchPages());
        this.searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        this.showPublishedButton = new Button("Show All", e -> this.loadPagesAsync());
        this.showPublishedButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        layout.add(this.searchField, this.searchButton, this.showPublishedButton);
        return layout;
    }

    private HorizontalLayout createButtonLayout() {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var createButton = new CreateButton(e -> this.openPageDialog(null));
        final var refreshButton = new RefreshButton(e -> this.loadPagesAsync());

        layout.add(createButton, refreshButton);
        return layout;
    }

    private void createGrid() {
        this.grid = new Grid<>(PageDto.class, false);
        this.grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        this.grid.setSizeFull();

        // Configure columns
        this.grid.addColumn(page -> page.id).setHeader("ID").setWidth("80px").setFlexGrow(0);

        // Make the title column clickable
        this.grid.addComponentColumn(page -> {
            final var titleSpan = new Span(page.title);
            titleSpan.getStyle().set("color", "var(--lumo-primary-text-color)");
            titleSpan.getStyle().set("cursor", "pointer");
            titleSpan.getStyle().set("width", "100%");
            titleSpan.getStyle().set("display", "block");
            titleSpan.addClickListener(e -> this.openPageDialog(page));
            return titleSpan;
        }).setHeader("Title").setFlexGrow(2);

        this.grid.addColumn(page -> page.slug).setHeader("Slug").setWidth("150px").setFlexGrow(1);

        this.grid.addComponentColumn(page -> {
            final var checkbox = new Checkbox();
            checkbox.setValue(page.published != null ? page.published : false);
            checkbox.setReadOnly(true);
            return checkbox;
        }).setHeader("Published").setWidth("100px").setFlexGrow(0);

        this.grid.addColumn(page -> page.created).setHeader("Created").setWidth("150px").setFlexGrow(0);
        this.grid.addColumn(page -> page.lastEdit).setHeader("Last Edit").setWidth("150px").setFlexGrow(0);

        // Add action column
        this.grid.addComponentColumn(this::createActionButtons).setHeader("Actions").setWidth("150px").setFlexGrow(0);
    }

    private HorizontalLayout createActionButtons(final PageDto page) {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var editButton = new EditButton(e -> this.openPageDialog(page));
        final var deleteButton = new DeleteButton(e -> this.deletePage(page));

        layout.add(editButton, deleteButton);
        return layout;
    }

    private void createPageDialog() {
        this.pageDialog = new Dialog();
        this.pageDialog.setWidth("700px");
        this.pageDialog.setHeight("600px");
        this.pageDialog.setCloseOnEsc(true);
        this.pageDialog.setCloseOnOutsideClick(false);

        this.binder = new Binder<>(PageDto.class);
    }

    private void openPageDialog(final PageDto page) {
        this.pageDialog.removeAll();
        this.currentPage = page != null ? page : new PageDto();

        final var title = new H3(page != null ? "Edit Page" : "Create Page");

        final var form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Form fields
        final var titleField = new TextField("Title");
        titleField.setRequired(true);
        titleField.setWidthFull();

        final var slugField = new TextField("Slug");
        slugField.setRequired(true);
        slugField.setWidthFull();
        slugField.setHelperText("URL-friendly identifier for the page");

        final var contentField = new TextArea("Content");
        contentField.setRequired(true);
        contentField.setWidthFull();
        contentField.setHeight("250px");

        final var publishedField = new Checkbox("Published");

        // Bind fields
        this.binder.bind(titleField, page1 -> page1.title, (page1, value) -> page1.title = value);
        this.binder.bind(slugField, page1 -> page1.slug, (page1, value) -> page1.slug = value);
        this.binder.bind(contentField, page1 -> page1.content, (page1, value) -> page1.content = value);
        this.binder.bind(publishedField, page1 -> page1.published != null ? page1.published : false,
                (page1, value) -> page1.published = value);

        form.add(titleField, slugField, contentField, publishedField);

        // Button layout
        final var buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        final var saveButton = new Button("Save", e -> this.savePage());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final var cancelButton = new Button("Cancel", e -> this.pageDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttonLayout.add(saveButton, cancelButton);

        final var dialogLayout = new VerticalLayout(title, form, buttonLayout);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(false);
        dialogLayout.setSizeFull();

        this.pageDialog.add(dialogLayout);

        // Load current page data
        this.binder.readBean(this.currentPage);

        this.pageDialog.open();
    }

    private void savePage() {
        try {
            this.binder.writeBean(this.currentPage);

            if (this.currentPage.id == null) {
                this.pageService.createPage(this.currentPage);
                NotificationUtil.showSuccess("Page created successfully");
            } else {
                this.pageService.updatePage(this.currentPage);
                NotificationUtil.showSuccess("Page updated successfully");
            }

            this.pageDialog.close();
            this.loadPagesAsync();

        } catch (final ValidationException e) {
            NotificationUtil.showError("Please check the form for errors");
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error saving page: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error saving page", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void deletePage(final PageDto page) {
        try {
            if (this.pageService.deletePage(page.id)) {
                NotificationUtil.showSuccess("Page deleted successfully");
                this.loadPagesAsync();
            } else {
                NotificationUtil.showError("Failed to delete page");
            }
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error deleting page: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error deleting page", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void searchPages() {
        // For now, just reload all pages
        // In a real implementation, you would filter based on the search field
        this.loadPagesAsync();
    }
}
