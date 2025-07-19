package de.vptr.midas.gui.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vptr.midas.gui.client.PostClient;
import de.vptr.midas.gui.dto.PostDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class PostService {

    private static final Logger LOG = LoggerFactory.getLogger(PostService.class);

    @Inject
    @RestClient
    PostClient postClient;

    @Inject
    AuthService authService;

    public List<PostDto> getAllPosts() {
        LOG.debug("Fetching all posts");
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.postClient.getAllPosts(authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching posts", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching posts: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching posts", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public List<PostDto> getPublishedPosts() {
        LOG.debug("Fetching published posts");
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.postClient.getPublishedPosts(authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching published posts", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching published posts: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching published posts", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public Optional<PostDto> getPostById(final Long id) {
        LOG.debug("Fetching post with ID: {}", id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Optional.empty();
            }

            final Response response = this.postClient.getPost(id, authHeader);
            if (response.getStatus() == 200) {
                return Optional.of(response.readEntity(PostDto.class));
            } else if (response.getStatus() == 404) {
                return Optional.empty();
            } else {
                throw new ServiceException("Backend error: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching post {}", id, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching post {}: {}", id, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching post {}", id, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public List<PostDto> getPostsByUser(final Long userId) {
        LOG.debug("Fetching posts for user: {}", userId);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.postClient.getPostsByUser(userId, authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching posts for user {}", userId, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching posts for user {}: {}", userId, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching posts for user {}", userId, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public List<PostDto> getPostsByCategory(final Long categoryId) {
        LOG.debug("Fetching posts for category: {}", categoryId);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.postClient.getPostsByCategory(categoryId, authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching posts for category {}", categoryId, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching posts for category {}: {}", categoryId, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching posts for category {}", categoryId, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public PostDto createPost(final PostDto post) {
        LOG.debug("Creating new post: {}", post.title);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.postClient.createPost(post, authHeader);
            if (response.getStatus() == 201) {
                return response.readEntity(PostDto.class);
            } else {
                throw new ServiceException("Failed to create post: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while creating post", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while creating post: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while creating post", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public PostDto updatePost(final PostDto post) {
        LOG.debug("Updating post: {}", post.id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.postClient.updatePost(post.id, post, authHeader);
            if (response.getStatus() == 200) {
                return response.readEntity(PostDto.class);
            } else {
                throw new ServiceException("Failed to update post: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while updating post", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while updating post: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while updating post", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public boolean deletePost(final Long id) {
        LOG.debug("Deleting post: {}", id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.postClient.deletePost(id, authHeader);
            return response.getStatus() == 204 || response.getStatus() == 200;
        } catch (final ProcessingException e) {
            LOG.error("Connection error while deleting post {}", id, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while deleting post {}: {}", id, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while deleting post {}", id, e);
            throw new ServiceException("Unexpected error", e);
        }
    }
}
