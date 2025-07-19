package de.vptr.midas.gui.service;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vptr.midas.gui.client.PageClient;
import de.vptr.midas.gui.dto.PageDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class PageService {

    private static final Logger LOG = LoggerFactory.getLogger(PageService.class);

    @Inject
    @RestClient
    PageClient pageClient;

    @Inject
    AuthService authService;

    public List<PageDto> getAllPages() throws AuthenticationException, ServiceException {
        LOG.info("Getting all pages");

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.pageClient.getAllPages(authHeader);
            LOG.info("Successfully retrieved {} pages", response.size());
            return response;
        } catch (final WebApplicationException e) {
            LOG.error("Error getting pages: {}", e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to get pages: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error getting pages", e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }

    public PageDto getPageById(final Long id) throws AuthenticationException, ServiceException {
        LOG.info("Getting page by id: {}", id);

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.pageClient.getPage(id, authHeader);

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                final var page = response.readEntity(PageDto.class);
                LOG.info("Successfully retrieved page: {}", page.title);
                return page;
            } else {
                throw new ServiceException("Page not found");
            }
        } catch (final WebApplicationException e) {
            LOG.error("Error getting page {}: {}", id, e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to get page: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error getting page {}", id, e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }

    public PageDto createPage(final PageDto page) throws AuthenticationException, ServiceException {
        LOG.info("Creating page: {}", page.title);

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.pageClient.createPage(page, authHeader);

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                final var createdPage = response.readEntity(PageDto.class);
                LOG.info("Successfully created page: {}", createdPage.title);
                return createdPage;
            } else {
                throw new ServiceException("Failed to create page");
            }
        } catch (final WebApplicationException e) {
            LOG.error("Error creating page: {}", e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to create page: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error creating page", e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }

    public PageDto updatePage(final PageDto page) throws AuthenticationException, ServiceException {
        LOG.info("Updating page: {} ({})", page.title, page.id);

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.pageClient.updatePage(page.id, page, authHeader);

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                final var updatedPage = response.readEntity(PageDto.class);
                LOG.info("Successfully updated page: {}", updatedPage.title);
                return updatedPage;
            } else {
                throw new ServiceException("Failed to update page");
            }
        } catch (final WebApplicationException e) {
            LOG.error("Error updating page {}: {}", page.id, e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to update page: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error updating page {}", page.id, e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }

    public boolean deletePage(final Long id) throws AuthenticationException, ServiceException {
        LOG.info("Deleting page: {}", id);

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.pageClient.deletePage(id, authHeader);

            if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode() ||
                    response.getStatus() == Response.Status.OK.getStatusCode()) {
                LOG.info("Successfully deleted page: {}", id);
                return true;
            } else {
                return false;
            }
        } catch (final WebApplicationException e) {
            LOG.error("Error deleting page {}: {}", id, e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to delete page: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error deleting page {}", id, e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }
}
