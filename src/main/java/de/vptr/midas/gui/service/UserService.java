package de.vptr.midas.gui.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vptr.midas.gui.client.UserClient;
import de.vptr.midas.gui.dto.UserDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Inject
    @RestClient
    UserClient userClient;

    @Inject
    AuthService authService;

    public List<UserDto> getAllUsers(final String authHeader) {
        LOG.debug("Fetching all users");
        try {
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.userClient.getAllUsers(authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching users", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching users: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching users", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public Optional<UserDto> getCurrentUser() {
        LOG.debug("Fetching current user");
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Optional.empty();
            }

            final Response response = this.userClient.getCurrentUser(authHeader);
            if (response.getStatus() == 200) {
                return Optional.of(response.readEntity(UserDto.class));
            } else if (response.getStatus() == 404) {
                return Optional.empty();
            } else {
                throw new ServiceException("Backend error: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching current user", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching current user: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching current user", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public Optional<UserDto> getUserById(final Long id) {
        LOG.debug("Fetching user with ID: {}", id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Optional.empty();
            }

            final Response response = this.userClient.getUser(id, authHeader);
            if (response.getStatus() == 200) {
                return Optional.of(response.readEntity(UserDto.class));
            } else if (response.getStatus() == 404) {
                return Optional.empty();
            } else {
                throw new ServiceException("Backend error: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching user {}", id, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching user {}: {}", id, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching user {}", id, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public Optional<UserDto> getUserByUsername(final String username) {
        LOG.debug("Fetching user with username: {}", username);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Optional.empty();
            }

            final Response response = this.userClient.getUserByUsername(username, authHeader);
            if (response.getStatus() == 200) {
                return Optional.of(response.readEntity(UserDto.class));
            } else if (response.getStatus() == 404) {
                return Optional.empty();
            } else {
                throw new ServiceException("Backend error: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching user by username {}", username, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching user by username {}: {}", username, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching user by username {}", username, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public UserDto createUser(final UserDto user) {
        LOG.debug("Creating new user: {}", user.username);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.userClient.createUser(user, authHeader);
            if (response.getStatus() == 201) {
                return response.readEntity(UserDto.class);
            } else {
                throw new ServiceException("Failed to create user: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while creating user", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while creating user: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while creating user", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public UserDto updateUser(final UserDto user) {
        LOG.debug("Updating user: {}", user.id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.userClient.updateUser(user.id, user, authHeader);
            if (response.getStatus() == 200) {
                return response.readEntity(UserDto.class);
            } else {
                throw new ServiceException("Failed to update user: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while updating user", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while updating user: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while updating user", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public boolean deleteUser(final Long id) {
        LOG.debug("Deleting user: {}", id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.userClient.deleteUser(id, authHeader);
            return response.getStatus() == 204 || response.getStatus() == 200;
        } catch (final ProcessingException e) {
            LOG.error("Connection error while deleting user {}", id, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while deleting user {}: {}", id, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while deleting user {}", id, e);
            throw new ServiceException("Unexpected error", e);
        }
    }
}
