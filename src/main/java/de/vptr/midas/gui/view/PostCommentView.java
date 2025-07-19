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
import de.vptr.midas.gui.dto.PostCommentDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import de.vptr.midas.gui.service.AuthService;
import de.vptr.midas.gui.service.PostCommentService;
import de.vptr.midas.gui.util.NotificationUtil;
import jakarta.inject.Inject;

@Route(value = "comments", layout = MainLayout.class)
public class PostCommentView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(PostCommentView.class);

    @Inject
    PostCommentService commentService;

    @Inject
    AuthService authService;

    private Grid<PostCommentDto> grid;
    private TextField searchField;
    private Button searchButton;
    private Button showApprovedButton;

    private Dialog commentDialog;
    private Binder<PostCommentDto> binder;
    private PostCommentDto currentComment;

    public PostCommentView() {
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
        this.loadCommentsAsync();
    }

    private void loadCommentsAsync() {
        LOG.info("Starting async comment loading");

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
            LOG.info("Making REST call to load comments");
            try {
                return this.commentService.getAllComments(authHeader);
            } catch (final AuthenticationException e) {
                LOG.error("Authentication failed while loading comments", e);
                throw e;
            } catch (final ServiceException e) {
                LOG.error("Service error while loading comments", e);
                throw e;
            } catch (final Exception e) {
                LOG.error("Error loading comments", e);
                throw new RuntimeException("Failed to load comments", e);
            }
        }).orTimeout(30, TimeUnit.SECONDS)
                .whenComplete((comments, throwable) -> {
                    this.getUI().ifPresent(ui -> ui.access(() -> {
                        if (throwable != null) {
                            LOG.error("Error loading comments: {}", throwable.getMessage(), throwable);
                            NotificationUtil.showError("Failed to load comments: " + throwable.getMessage());
                        } else {
                            LOG.info("Successfully loaded {} comments", comments.size());
                            this.grid.setItems(comments);
                        }
                    }));
                });
    }

    private void buildUI() {
        this.removeAll();

        // Header
        final var header = new H1("Post Comments");
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

        // Comment dialog
        this.createCommentDialog();
    }

    private HorizontalLayout createFilterLayout() {
        final var layout = new HorizontalLayout();
        layout.setAlignItems(Alignment.END);
        layout.setSpacing(true);

        this.searchField = new TextField("Search Comments");
        this.searchField.setPlaceholder("Search by author or content...");
        this.searchField.setWidth("300px");

        this.searchButton = new Button("Search", e -> this.searchComments());
        this.searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        this.showApprovedButton = new Button("Show All", e -> this.loadCommentsAsync());
        this.showApprovedButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        layout.add(this.searchField, this.searchButton, this.showApprovedButton);
        return layout;
    }

    private HorizontalLayout createButtonLayout() {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var createButton = new CreateButton(e -> this.openCommentDialog(null));
        final var refreshButton = new RefreshButton(e -> this.loadCommentsAsync());

        layout.add(createButton, refreshButton);
        return layout;
    }

    private void createGrid() {
        this.grid = new Grid<>(PostCommentDto.class, false);
        this.grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        this.grid.setSizeFull();

        // Configure columns
        this.grid.addColumn(comment -> comment.id).setHeader("ID").setWidth("80px").setFlexGrow(0);

        this.grid.addColumn(comment -> comment.post != null ? comment.post.title : "").setHeader("Post")
                .setWidth("200px").setFlexGrow(1);
        this.grid.addColumn(comment -> comment.user != null ? comment.user.username : "").setHeader("Author")
                .setWidth("120px").setFlexGrow(0);

        // Content column with limited display
        this.grid.addComponentColumn(comment -> {
            final var content = comment.content != null ? comment.content : "";
            final var truncated = content.length() > 50 ? content.substring(0, 50) + "..." : content;
            final var span = new Span(truncated);
            span.setTitle(content); // Show full content on hover
            return span;
        }).setHeader("Content").setFlexGrow(2);

        this.grid.addColumn(comment -> comment.created).setHeader("Created").setWidth("150px").setFlexGrow(0);

        // Add action column
        this.grid.addComponentColumn(this::createActionButtons).setHeader("Actions").setWidth("150px").setFlexGrow(0);
    }

    private HorizontalLayout createActionButtons(final PostCommentDto comment) {
        final var layout = new HorizontalLayout();
        layout.setSpacing(true);

        final var editButton = new EditButton(e -> this.openCommentDialog(comment));
        final var deleteButton = new DeleteButton(e -> this.deleteComment(comment));

        layout.add(editButton, deleteButton);
        return layout;
    }

    private void createCommentDialog() {
        this.commentDialog = new Dialog();
        this.commentDialog.setWidth("600px");
        this.commentDialog.setHeight("500px");
        this.commentDialog.setCloseOnEsc(true);
        this.commentDialog.setCloseOnOutsideClick(false);

        this.binder = new Binder<>(PostCommentDto.class);
    }

    private void openCommentDialog(final PostCommentDto comment) {
        this.commentDialog.removeAll();
        this.currentComment = comment != null ? comment : new PostCommentDto();

        final var title = new H3(comment != null ? "Edit Comment" : "Create Comment");

        final var form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Form fields
        final var contentField = new TextArea("Content");
        contentField.setRequired(true);
        contentField.setWidthFull();
        contentField.setHeight("200px");

        // Bind fields
        this.binder.bind(contentField, comment1 -> comment1.content, (comment1, value) -> comment1.content = value);

        form.add(contentField);

        // Button layout
        final var buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        final var saveButton = new Button("Save", e -> this.saveComment());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        final var cancelButton = new Button("Cancel", e -> this.commentDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttonLayout.add(saveButton, cancelButton);

        final var dialogLayout = new VerticalLayout(title, form, buttonLayout);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(false);
        dialogLayout.setSizeFull();

        this.commentDialog.add(dialogLayout);

        // Load current comment data
        this.binder.readBean(this.currentComment);

        this.commentDialog.open();
    }

    private void saveComment() {
        try {
            this.binder.writeBean(this.currentComment);

            if (this.currentComment.id == null) {
                this.commentService.createComment(this.currentComment);
                NotificationUtil.showSuccess("Comment created successfully");
            } else {
                this.commentService.updateComment(this.currentComment);
                NotificationUtil.showSuccess("Comment updated successfully");
            }

            this.commentDialog.close();
            this.loadCommentsAsync();

        } catch (final ValidationException e) {
            NotificationUtil.showError("Please check the form for errors");
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error saving comment: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error saving comment", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void deleteComment(final PostCommentDto comment) {
        try {
            if (this.commentService.deleteComment(comment.id)) {
                NotificationUtil.showSuccess("Comment deleted successfully");
                this.loadCommentsAsync();
            } else {
                NotificationUtil.showError("Failed to delete comment");
            }
        } catch (final AuthenticationException e) {
            NotificationUtil.showError("Session expired. Please log in again.");
            this.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (final ServiceException e) {
            NotificationUtil.showError("Error deleting comment: " + e.getMessage());
        } catch (final Exception e) {
            LOG.error("Unexpected error deleting comment", e);
            NotificationUtil.showError("Unexpected error occurred");
        }
    }

    private void searchComments() {
        // For now, just reload all comments
        // In a real implementation, you would filter based on the search field
        this.loadCommentsAsync();
    }
}
