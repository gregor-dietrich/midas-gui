package de.vptr.midas.gui.view;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import de.vptr.midas.gui.component.CreateButton;
import de.vptr.midas.gui.component.DeleteButton;
import de.vptr.midas.gui.component.EditButton;
import de.vptr.midas.gui.component.RefreshButton;
import de.vptr.midas.gui.dto.PostCategoryDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import de.vptr.midas.gui.service.AuthService;
import de.vptr.midas.gui.service.PostCategoryService;
import de.vptr.midas.gui.util.NotificationUtil;
import jakarta.inject.Inject;

@Route(value = "categories", layout = MainLayout.class)
public class PostCategoryView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(PostCategoryView.class);

    @Inject
    PostCategoryService categoryService;

    @Inject
    AuthService authService;

    private TreeGrid<PostCategoryDto> treeGrid;
    private List<PostCategoryDto> allCategories;

    private Dialog categoryDialog;
    private Binder<PostCategoryDto> binder;
    private PostCategoryDto currentCategory;

    public PostCategoryView() {
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
        this.loadCategoriesAsync();
    }

    private void loadCategoriesAsync() {
        LOG.info("Starting async category loading");

        CompletableFuture.supplyAsync(() -> {
            LOG.info("Making REST call to load categories");
            try {
                return this.categoryService.getAllCategories();
            } catch (final AuthenticationException e) {
                LOG.error("Authentication failed while loading categories", e);
                throw e;
            } catch (final ServiceException e) {
                LOG.error("Service error while loading categories", e);
                throw e;
            } catch (final Exception e) {
                LOG.error("Error loading categories", e);
                throw new RuntimeException("Failed to load categories", e);
            }
        }).orTimeout(30, TimeUnit.SECONDS)
                .whenComplete((categories, throwable) -> {
                    this.getUI().ifPresent(ui -> ui.access(() -> {
                        if (throwable != null) {
                            LOG.error("Error loading categories: {}", throwable.getMessage(), throwable);
                            NotificationUtil.showError("Failed to load categories: " + throwable.getMessage());
                        } else {
                            LOG.info("Successfully loaded {} categories", categories.size());
                            this.allCategories = categories;
                            this.updateTreeGrid();
                        }
                    }));
                });
    }

    private void updateTreeGrid() {
        // Find root categories (categories without parent)
        final var rootCategories = this.allCategories.stream()
                .filter(PostCategoryDto::isRootCategory)
                .toList();

        this.treeGrid.setItems(rootCategories, this::getChildrenOfCategory);
        this.treeGrid.expandRecursively(rootCategories, 2); // Expand up to 2 levels
    }

    private List<PostCategoryDto> getChildrenOfCategory(final PostCategoryDto parent) {
        return this.allCategories.stream()
                .filter(category -> category.parent != null && category.parent.id.equals(parent.id))
                .toList();
    }

    private void buildUI() {
        this.removeAll();

        // Header
        final var header = new H1("Post Categories");
        this.add(header);

        // Action buttons
        final var buttonLayout = this.createButtonLayout();
        this.add(buttonLayout);

        // Tree Grid
        this.createTreeGrid();
        this.add(this.treeGrid);

        // Category dialog
        this.createCategoryDialog();
    }

    private HorizontalLayout createButtonLayout() {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var createButton = new CreateButton(e -> this.openCategoryDialog(null));
        final var refreshButton = new RefreshButton(e -> this.loadCategoriesAsync());

        layout.add(createButton, refreshButton);
        return layout;
    }

    private void createTreeGrid() {
        this.treeGrid = new TreeGrid<>(PostCategoryDto.class);
        this.treeGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        this.treeGrid.setSizeFull();
        this.treeGrid.removeAllColumns();

        // Configure columns
        this.treeGrid.addHierarchyColumn(category -> category.name)
                .setHeader("Category Name")
                .setFlexGrow(3);

        this.treeGrid.addColumn(category -> category.parent != null ? "Subcategory" : "Root Category")
                .setHeader("Type")
                .setFlexGrow(1);

        // Add action column
        this.treeGrid.addComponentColumn(this::createActionButtons)
                .setHeader("Actions")
                .setWidth("150px")
                .setFlexGrow(0);
    }

    private HorizontalLayout createActionButtons(final PostCategoryDto category) {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var editButton = new EditButton(e -> this.openCategoryDialog(category));
        final var deleteButton = new DeleteButton(e -> this.deleteCategory(category));

        layout.add(editButton, deleteButton);
        return layout;
    }

    private void createCategoryDialog() {
        this.categoryDialog = new Dialog();
        this.categoryDialog.setWidth("500px");
        this.categoryDialog.setHeight("400px");
        this.categoryDialog.setCloseOnEsc(true);
        this.categoryDialog.setCloseOnOutsideClick(false);

        this.binder = new Binder<>(PostCategoryDto.class);
    }

    private void openCategoryDialog(final PostCategoryDto category) {
        this.categoryDialog.removeAll();
        this.currentCategory = category != null ? category : new PostCategoryDto();

        final var title = new H3(category != null ? "Edit Category" : "Create Category");

        final var form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Form fields
        final var nameField = new TextField("Name");
        nameField.setRequired(true);
        nameField.setWidthFull();

        final var parentField = new ComboBox<PostCategoryDto>("Parent Category");
        parentField.setItemLabelGenerator(cat -> cat.name);
        parentField.setWidthFull();
        if (this.allCategories != null) {
            // Only show categories that are not descendants of the current category
            final var availableParents = this.allCategories.stream()
                    .filter(cat -> category == null || !this.isDescendantOf(cat, category))
                    .filter(cat -> category == null || !cat.id.equals(category.id))
                    .toList();
            parentField.setItems(availableParents);
        }

        // Bind fields
        this.binder.bind(nameField, cat -> cat.name, (cat, value) -> cat.name = value);
        this.binder.bind(parentField, cat -> cat.parent, (cat, value) -> cat.parent = value);

        form.add(nameField, parentField);

        // Button layout
        final var buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        final var saveButton = new Button("Save", e -> this.saveCategory());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final var cancelButton = new Button("Cancel", e -> this.categoryDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttonLayout.add(saveButton, cancelButton);

        final var dialogLayout = new VerticalLayout(title, form, buttonLayout);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(false);
        dialogLayout.setSizeFull();

        this.categoryDialog.add(dialogLayout);

        // Load current category data
        this.binder.readBean(this.currentCategory);

        this.categoryDialog.open();
    }

    private boolean isDescendantOf(final PostCategoryDto potential, final PostCategoryDto ancestor) {
        if (potential.parent == null) {
            return false;
        }
        if (potential.parent.id.equals(ancestor.id)) {
            return true;
        }

        // Find the parent in the list and check recursively
        final var parent = this.allCategories.stream()
                .filter(cat -> cat.id.equals(potential.parent.id))
                .findFirst()
                .orElse(null);

        if (parent != null) {
            return this.isDescendantOf(parent, ancestor);
        }

        return false;
    }

    private void saveCategory() {
        try {
            this.binder.writeBean(this.currentCategory);

            if (this.currentCategory.id == null) {
                this.categoryService.createCategory(this.currentCategory);
                NotificationUtil.showSuccess("Category created successfully");
            } else {
                this.categoryService.updateCategory(this.currentCategory);
                NotificationUtil.showSuccess("Category updated successfully");
            }

            this.categoryDialog.close();
            this.loadCategoriesAsync();

        } catch (final ValidationException e) {
            NotificationUtil.showError("Please check the form for errors");
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error saving category: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error saving category", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void deleteCategory(final PostCategoryDto category) {
        // Check if category has children
        final boolean hasChildren = this.allCategories.stream()
                .anyMatch(cat -> cat.parent != null && cat.parent.id.equals(category.id));

        if (hasChildren) {
            NotificationUtil
                    .showError("Cannot delete category with subcategories. Please delete or move subcategories first.");
            return;
        }

        try {
            if (this.categoryService.deleteCategory(category.id)) {
                NotificationUtil.showSuccess("Category deleted successfully");
                this.loadCategoriesAsync();
            } else {
                NotificationUtil.showError("Failed to delete category");
            }
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error deleting category: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error deleting category", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }
}
