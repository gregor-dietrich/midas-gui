package de.vptr.midas.gui.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vptr.midas.gui.client.UserGroupClient;
import de.vptr.midas.gui.dto.UserDto;
import de.vptr.midas.gui.dto.UserGroupDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class UserGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(UserGroupService.class);

    @Inject
    @RestClient
    UserGroupClient groupClient;

    @Inject
    AuthService authService;

    public List<UserGroupDto> getAllGroups() {
        LOG.debug("Fetching all user groups");
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.groupClient.getAllGroups(authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching user groups", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching user groups: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching user groups", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public Optional<UserGroupDto> getGroupById(final Long id) {
        LOG.debug("Fetching group with ID: {}", id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Optional.empty();
            }

            final Response response = this.groupClient.getGroup(id, authHeader);
            if (response.getStatus() == 200) {
                return Optional.of(response.readEntity(UserGroupDto.class));
            } else if (response.getStatus() == 404) {
                return Optional.empty();
            } else {
                throw new ServiceException("Backend error: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching group {}", id, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching group {}: {}", id, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching group {}", id, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public List<UserDto> getUsersInGroup(final Long groupId) {
        LOG.debug("Fetching users in group: {}", groupId);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.groupClient.getUsersInGroup(groupId, authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching users in group {}", groupId, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching users in group {}: {}", groupId, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching users in group {}", groupId, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public UserGroupDto createGroup(final UserGroupDto group) {
        LOG.debug("Creating new group: {}", group.name);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.groupClient.createGroup(group, authHeader);
            if (response.getStatus() == 201) {
                return response.readEntity(UserGroupDto.class);
            } else {
                throw new ServiceException("Failed to create group: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while creating group", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while creating group: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while creating group", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public UserGroupDto updateGroup(final UserGroupDto group) {
        LOG.debug("Updating group: {}", group.id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.groupClient.updateGroup(group.id, group, authHeader);
            if (response.getStatus() == 200) {
                return response.readEntity(UserGroupDto.class);
            } else {
                throw new ServiceException("Failed to update group: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while updating group", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while updating group: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while updating group", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public boolean deleteGroup(final Long id) {
        LOG.debug("Deleting group: {}", id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.groupClient.deleteGroup(id, authHeader);
            return response.getStatus() == 204 || response.getStatus() == 200;
        } catch (final ProcessingException e) {
            LOG.error("Connection error while deleting group {}", id, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while deleting group {}: {}", id, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while deleting group {}", id, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public boolean addUserToGroup(final Long groupId, final Long userId) {
        LOG.debug("Adding user {} to group {}", userId, groupId);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.groupClient.addUserToGroup(groupId, userId, authHeader);
            return response.getStatus() == 200 || response.getStatus() == 201;
        } catch (final ProcessingException e) {
            LOG.error("Connection error while adding user {} to group {}", userId, groupId, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while adding user {} to group {}: {}", userId, groupId, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while adding user {} to group {}", userId, groupId, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public boolean removeUserFromGroup(final Long groupId, final Long userId) {
        LOG.debug("Removing user {} from group {}", userId, groupId);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.groupClient.removeUserFromGroup(groupId, userId, authHeader);
            return response.getStatus() == 200 || response.getStatus() == 204;
        } catch (final ProcessingException e) {
            LOG.error("Connection error while removing user {} from group {}", userId, groupId, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while removing user {} from group {}: {}", userId, groupId,
                    e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while removing user {} from group {}", userId, groupId, e);
            throw new ServiceException("Unexpected error", e);
        }
    }
}
