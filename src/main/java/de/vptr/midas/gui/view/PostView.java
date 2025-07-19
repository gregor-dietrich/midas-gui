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
import de.vptr.midas.gui.dto.PostDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import de.vptr.midas.gui.service.AuthService;
import de.vptr.midas.gui.service.PostService;
import de.vptr.midas.gui.util.NotificationUtil;
import jakarta.inject.Inject;

@Route(value = "posts", layout = MainLayout.class)
public class PostView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(PostView.class);

    @Inject
    PostService postService;

    @Inject
    AuthService authService;

    private Grid<PostDto> grid;
    private TextField searchField;
    private Button searchButton;
    private Button showPublishedButton;

    private Dialog postDialog;
    private Binder<PostDto> binder;
    private PostDto currentPost;

    public PostView() {
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
        this.loadPostsAsync();
    }

    private void loadPostsAsync() {
        LOG.info("Starting async post loading");

        CompletableFuture.supplyAsync(() -> {
            LOG.info("Making REST call to load posts");
            try {
                return this.postService.getAllPosts();
            } catch (final AuthenticationException e) {
                LOG.error("Authentication failed while loading posts", e);
                throw e;
            } catch (final ServiceException e) {
                LOG.error("Service error while loading posts", e);
                throw e;
            } catch (final Exception e) {
                LOG.error("Error loading posts", e);
                throw new RuntimeException("Failed to load posts", e);
            }
        }).orTimeout(30, TimeUnit.SECONDS)
                .whenComplete((posts, throwable) -> {
                    this.getUI().ifPresent(ui -> ui.access(() -> {
                        if (throwable != null) {
                            LOG.error("Error loading posts: {}", throwable.getMessage(), throwable);
                            NotificationUtil.showError("Failed to load posts: " + throwable.getMessage());
                        } else {
                            LOG.info("Successfully loaded {} posts", posts.size());
                            this.grid.setItems(posts);
                        }
                    }));
                });
    }

    private void loadPublishedPostsAsync() {
        LOG.info("Starting async published post loading");

        CompletableFuture.supplyAsync(() -> {
            LOG.info("Making REST call to load published posts");
            try {
                return this.postService.getPublishedPosts();
            } catch (final AuthenticationException e) {
                LOG.error("Authentication failed while loading published posts", e);
                throw e;
            } catch (final ServiceException e) {
                LOG.error("Service error while loading published posts", e);
                throw e;
            } catch (final Exception e) {
                LOG.error("Error loading published posts", e);
                throw new RuntimeException("Failed to load published posts", e);
            }
        }).orTimeout(30, TimeUnit.SECONDS)
                .whenComplete((posts, throwable) -> {
                    this.getUI().ifPresent(ui -> ui.access(() -> {
                        if (throwable != null) {
                            LOG.error("Error loading published posts: {}", throwable.getMessage(), throwable);
                            NotificationUtil.showError("Failed to load published posts: " + throwable.getMessage());
                        } else {
                            LOG.info("Successfully loaded {} published posts", posts.size());
                            this.grid.setItems(posts);
                        }
                    }));
                });
    }

    private void buildUI() {
        this.removeAll();

        // Header
        final var header = new H1("Posts");
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

        // Post dialog
        this.createPostDialog();
    }

    private HorizontalLayout createFilterLayout() {
        final var layout = new HorizontalLayout();
        layout.setAlignItems(Alignment.END);
        layout.setSpacing(true);

        this.searchField = new TextField("Search Posts");
        this.searchField.setPlaceholder("Search by title or content...");
        this.searchField.setWidth("300px");

        this.searchButton = new Button("Search", e -> this.searchPosts());
        this.searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        this.showPublishedButton = new Button("Show Published Only", e -> this.loadPublishedPostsAsync());
        this.showPublishedButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        layout.add(this.searchField, this.searchButton, this.showPublishedButton);
        return layout;
    }

    private HorizontalLayout createButtonLayout() {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var createButton = new CreateButton(e -> this.openPostDialog(null));
        final var refreshButton = new RefreshButton(e -> this.loadPostsAsync());

        layout.add(createButton, refreshButton);
        return layout;
    }

    private void createGrid() {
        this.grid = new Grid<>(PostDto.class, false);
        this.grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        this.grid.setSizeFull();

        // Configure columns
        this.grid.addColumn(post -> post.id).setHeader("ID").setWidth("80px").setFlexGrow(0);

        // Make the title column clickable
        this.grid.addComponentColumn(post -> {
            final var titleSpan = new Span(post.title);
            titleSpan.getStyle().set("color", "var(--lumo-primary-text-color)");
            titleSpan.getStyle().set("cursor", "pointer");
            titleSpan.getStyle().set("width", "100%");
            titleSpan.getStyle().set("display", "block");
            titleSpan.addClickListener(e -> this.openPostDialog(post));
            return titleSpan;
        }).setHeader("Title").setFlexGrow(2);

        this.grid.addColumn(post -> post.user != null ? post.user.username : "").setHeader("Author").setWidth("120px")
                .setFlexGrow(0);
        this.grid.addColumn(post -> post.category != null ? post.category.name : "").setHeader("Category")
                .setWidth("120px").setFlexGrow(0);

        this.grid.addComponentColumn(post -> {
            final var checkbox = new Checkbox();
            checkbox.setValue(post.published != null ? post.published : false);
            checkbox.setReadOnly(true);
            return checkbox;
        }).setHeader("Published").setWidth("100px").setFlexGrow(0);

        this.grid.addComponentColumn(post -> {
            final var checkbox = new Checkbox();
            checkbox.setValue(post.commentable != null ? post.commentable : false);
            checkbox.setReadOnly(true);
            return checkbox;
        }).setHeader("Commentable").setWidth("100px").setFlexGrow(0);

        this.grid.addColumn(post -> post.created).setHeader("Created").setWidth("150px").setFlexGrow(0);
        this.grid.addColumn(post -> post.lastEdit).setHeader("Last Edit").setWidth("150px").setFlexGrow(0);

        // Add action column
        this.grid.addComponentColumn(this::createActionButtons).setHeader("Actions").setWidth("150px").setFlexGrow(0);
    }

    private HorizontalLayout createActionButtons(final PostDto post) {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var editButton = new EditButton(e -> this.openPostDialog(post));
        final var deleteButton = new DeleteButton(e -> this.deletePost(post));

        layout.add(editButton, deleteButton);
        return layout;
    }

    private void createPostDialog() {
        this.postDialog = new Dialog();
        this.postDialog.setWidth("600px");
        this.postDialog.setHeight("500px");
        this.postDialog.setCloseOnEsc(true);
        this.postDialog.setCloseOnOutsideClick(false);

        this.binder = new Binder<>(PostDto.class);
    }

    private void openPostDialog(final PostDto post) {
        this.postDialog.removeAll();
        this.currentPost = post != null ? post : new PostDto();

        final var title = new H3(post != null ? "Edit Post" : "Create Post");

        final var form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Form fields
        final var titleField = new TextField("Title");
        titleField.setRequired(true);
        titleField.setWidthFull();

        final var contentField = new TextArea("Content");
        contentField.setRequired(true);
        contentField.setWidthFull();
        contentField.setHeight("200px");

        final var publishedField = new Checkbox("Published");
        final var commentableField = new Checkbox("Commentable");

        // Bind fields
        this.binder.bind(titleField, post1 -> post1.title, (post1, value) -> post1.title = value);
        this.binder.bind(contentField, post1 -> post1.content, (post1, value) -> post1.content = value);
        this.binder.bind(publishedField, post1 -> post1.published != null ? post1.published : false,
                (post1, value) -> post1.published = value);
        this.binder.bind(commentableField, post1 -> post1.commentable != null ? post1.commentable : false,
                (post1, value) -> post1.commentable = value);

        form.add(titleField, contentField, publishedField, commentableField);

        // Button layout
        final var buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        final var saveButton = new Button("Save", e -> this.savePost());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final var cancelButton = new Button("Cancel", e -> this.postDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttonLayout.add(saveButton, cancelButton);

        final var dialogLayout = new VerticalLayout(title, form, buttonLayout);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(false);
        dialogLayout.setSizeFull();

        this.postDialog.add(dialogLayout);

        // Load current post data
        this.binder.readBean(this.currentPost);

        this.postDialog.open();
    }

    private void savePost() {
        try {
            this.binder.writeBean(this.currentPost);

            if (this.currentPost.id == null) {
                this.postService.createPost(this.currentPost);
                NotificationUtil.showSuccess("Post created successfully");
            } else {
                this.postService.updatePost(this.currentPost);
                NotificationUtil.showSuccess("Post updated successfully");
            }

            this.postDialog.close();
            this.loadPostsAsync();

        } catch (final ValidationException e) {
            NotificationUtil.showError("Please check the form for errors");
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error saving post: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error saving post", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void deletePost(final PostDto post) {
        try {
            if (this.postService.deletePost(post.id)) {
                NotificationUtil.showSuccess("Post deleted successfully");
                this.loadPostsAsync();
            } else {
                NotificationUtil.showError("Failed to delete post");
            }
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error deleting post: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error deleting post", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void searchPosts() {
        // For now, just reload all posts
        // In a real implementation, you would filter based on the search field
        this.loadPostsAsync();
    }
}
