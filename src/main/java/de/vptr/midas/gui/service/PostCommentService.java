package de.vptr.midas.gui.service;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vptr.midas.gui.client.PostCommentClient;
import de.vptr.midas.gui.dto.PostCommentDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class PostCommentService {

    private static final Logger LOG = LoggerFactory.getLogger(PostCommentService.class);

    @Inject
    @RestClient
    PostCommentClient commentClient;

    @Inject
    AuthService authService;

    public List<PostCommentDto> getAllComments(final String authHeader)
            throws AuthenticationException, ServiceException {
        LOG.debug("Fetching all comments");
        try {
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Authentication required");
            }

            return this.commentClient.getAllComments(authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching comments", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching comments: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching comments", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public List<PostCommentDto> getCommentsByPost(final Long postId) throws AuthenticationException, ServiceException {
        LOG.info("Getting comments for post: {}", postId);

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.commentClient.getCommentsByPost(postId, authHeader);
            LOG.info("Successfully retrieved {} comments for post {}", response.size(), postId);
            return response;
        } catch (final WebApplicationException e) {
            LOG.error("Error getting comments for post {}: {}", postId, e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to get comments: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error getting comments for post {}", postId, e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }

    public PostCommentDto getCommentById(final Long id) throws AuthenticationException, ServiceException {
        LOG.info("Getting comment by id: {}", id);

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.commentClient.getComment(id, authHeader);

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                final var comment = response.readEntity(PostCommentDto.class);
                LOG.info("Successfully retrieved comment: {}", id);
                return comment;
            } else {
                throw new ServiceException("Comment not found");
            }
        } catch (final WebApplicationException e) {
            LOG.error("Error getting comment {}: {}", id, e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to get comment: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error getting comment {}", id, e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }

    public PostCommentDto createComment(final PostCommentDto comment) throws AuthenticationException, ServiceException {
        LOG.info("Creating comment for post: {}", comment.post != null ? comment.post.id : "unknown");

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.commentClient.createComment(comment, authHeader);

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                final var createdComment = response.readEntity(PostCommentDto.class);
                LOG.info("Successfully created comment: {}", createdComment.id);
                return createdComment;
            } else {
                throw new ServiceException("Failed to create comment");
            }
        } catch (final WebApplicationException e) {
            LOG.error("Error creating comment: {}", e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to create comment: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error creating comment", e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }

    public PostCommentDto updateComment(final PostCommentDto comment) throws AuthenticationException, ServiceException {
        LOG.info("Updating comment: {}", comment.id);

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.commentClient.updateComment(comment.id, comment, authHeader);

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                final var updatedComment = response.readEntity(PostCommentDto.class);
                LOG.info("Successfully updated comment: {}", updatedComment.id);
                return updatedComment;
            } else {
                throw new ServiceException("Failed to update comment");
            }
        } catch (final WebApplicationException e) {
            LOG.error("Error updating comment {}: {}", comment.id, e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to update comment: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error updating comment {}", comment.id, e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }

    public boolean deleteComment(final Long id) throws AuthenticationException, ServiceException {
        LOG.info("Deleting comment: {}", id);

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.commentClient.deleteComment(id, authHeader);

            if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode() ||
                    response.getStatus() == Response.Status.OK.getStatusCode()) {
                LOG.info("Successfully deleted comment: {}", id);
                return true;
            } else {
                return false;
            }
        } catch (final WebApplicationException e) {
            LOG.error("Error deleting comment {}: {}", id, e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to delete comment: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error deleting comment {}", id, e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }
}
