package de.vptr.midas.gui.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vptr.midas.gui.client.PostCategoryClient;
import de.vptr.midas.gui.dto.PostCategoryDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class PostCategoryService {

    private static final Logger LOG = LoggerFactory.getLogger(PostCategoryService.class);

    @Inject
    @RestClient
    PostCategoryClient categoryClient;

    @Inject
    AuthService authService;

    public List<PostCategoryDto> getAllCategories(final String authHeader) {
        LOG.debug("Fetching all categories");
        try {
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.categoryClient.getAllCategories(authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching categories", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching categories: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching categories", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public List<PostCategoryDto> getRootCategories() {
        LOG.debug("Fetching root categories");
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.categoryClient.getRootCategories(authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching root categories", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching root categories: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching root categories", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public Optional<PostCategoryDto> getCategoryById(final Long id) {
        LOG.debug("Fetching category with ID: {}", id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Optional.empty();
            }

            final Response response = this.categoryClient.getCategory(id, authHeader);
            if (response.getStatus() == 200) {
                return Optional.of(response.readEntity(PostCategoryDto.class));
            } else if (response.getStatus() == 404) {
                return Optional.empty();
            } else {
                throw new ServiceException("Backend error: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching category {}", id, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching category {}: {}", id, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching category {}", id, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public List<PostCategoryDto> getCategoriesByParent(final Long parentId) {
        LOG.debug("Fetching categories for parent: {}", parentId);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.categoryClient.getCategoriesByParent(parentId, authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching categories for parent {}", parentId, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching categories for parent {}: {}", parentId, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching categories for parent {}", parentId, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public PostCategoryDto createCategory(final PostCategoryDto category) {
        LOG.debug("Creating new category: {}", category.name);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.categoryClient.createCategory(category, authHeader);
            if (response.getStatus() == 201) {
                return response.readEntity(PostCategoryDto.class);
            } else {
                throw new ServiceException("Failed to create category: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while creating category", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while creating category: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while creating category", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public PostCategoryDto updateCategory(final PostCategoryDto category) {
        LOG.debug("Updating category: {}", category.id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.categoryClient.updateCategory(category.id, category, authHeader);
            if (response.getStatus() == 200) {
                return response.readEntity(PostCategoryDto.class);
            } else {
                throw new ServiceException("Failed to update category: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while updating category", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while updating category: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while updating category", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public boolean deleteCategory(final Long id) {
        LOG.debug("Deleting category: {}", id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.categoryClient.deleteCategory(id, authHeader);
            return response.getStatus() == 204 || response.getStatus() == 200;
        } catch (final ProcessingException e) {
            LOG.error("Connection error while deleting category {}", id, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while deleting category {}: {}", id, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while deleting category {}", id, e);
            throw new ServiceException("Unexpected error", e);
        }
    }
}
