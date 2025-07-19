package de.vptr.midas.gui.service;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vptr.midas.gui.client.UserRankClient;
import de.vptr.midas.gui.dto.UserRankDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class UserRankService {

    private static final Logger LOG = LoggerFactory.getLogger(UserRankService.class);

    @Inject
    @RestClient
    UserRankClient userRankClient;

    @Inject
    AuthService authService;

    public List<UserRankDto> getAllRanks() throws AuthenticationException, ServiceException {
        LOG.info("Getting all user ranks");

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.userRankClient.getAllRanks(authHeader);
            LOG.info("Successfully retrieved {} user ranks", response.size());
            return response;
        } catch (final WebApplicationException e) {
            LOG.error("Error getting user ranks: {}", e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to get user ranks: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error getting user ranks", e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }

    public UserRankDto getRankById(final Long id) throws AuthenticationException, ServiceException {
        LOG.info("Getting user rank by id: {}", id);

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.userRankClient.getRank(id, authHeader);

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                final var rank = response.readEntity(UserRankDto.class);
                LOG.info("Successfully retrieved user rank: {}", rank.name);
                return rank;
            } else {
                throw new ServiceException("User rank not found");
            }
        } catch (final WebApplicationException e) {
            LOG.error("Error getting user rank {}: {}", id, e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to get user rank: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error getting user rank {}", id, e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }

    public UserRankDto createRank(final UserRankDto rank) throws AuthenticationException, ServiceException {
        LOG.info("Creating user rank: {}", rank.name);

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.userRankClient.createRank(rank, authHeader);

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                final var createdRank = response.readEntity(UserRankDto.class);
                LOG.info("Successfully created user rank: {}", createdRank.name);
                return createdRank;
            } else {
                throw new ServiceException("Failed to create user rank");
            }
        } catch (final WebApplicationException e) {
            LOG.error("Error creating user rank: {}", e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to create user rank: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error creating user rank", e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }

    public UserRankDto updateRank(final UserRankDto rank) throws AuthenticationException, ServiceException {
        LOG.info("Updating user rank: {} ({})", rank.name, rank.id);

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.userRankClient.updateRank(rank.id, rank, authHeader);

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                final var updatedRank = response.readEntity(UserRankDto.class);
                LOG.info("Successfully updated user rank: {}", updatedRank.name);
                return updatedRank;
            } else {
                throw new ServiceException("Failed to update user rank");
            }
        } catch (final WebApplicationException e) {
            LOG.error("Error updating user rank {}: {}", rank.id, e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to update user rank: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error updating user rank {}", rank.id, e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }

    public boolean deleteRank(final Long id) throws AuthenticationException, ServiceException {
        LOG.info("Deleting user rank: {}", id);

        if (!this.authService.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            final var response = this.userRankClient.deleteRank(id, authHeader);

            if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode() ||
                    response.getStatus() == Response.Status.OK.getStatusCode()) {
                LOG.info("Successfully deleted user rank: {}", id);
                return true;
            } else {
                return false;
            }
        } catch (final WebApplicationException e) {
            LOG.error("Error deleting user rank {}: {}", id, e.getMessage(), e);
            if (e.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new AuthenticationException("Session expired", e);
            }
            throw new ServiceException("Failed to delete user rank: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error deleting user rank {}", id, e);
            throw new ServiceException("Unexpected error occurred", e);
        }
    }
}
