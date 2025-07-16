package de.vptr.midas.gui.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vptr.midas.gui.client.UserAccountClient;
import de.vptr.midas.gui.dto.UserAccount;
import de.vptr.midas.gui.dto.UserPayment;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class UserAccountService {

    private static final Logger LOG = LoggerFactory.getLogger(UserAccountService.class);

    @Inject
    @RestClient
    UserAccountClient accountClient;

    @Inject
    AuthService authService;

    public List<UserAccount> getAllAccounts(final String authHeader) throws ServiceException, AuthenticationException {
        LOG.debug("Fetching all user accounts with provided auth header");
        try {
            if (authHeader == null) {
                LOG.warn("No authentication header provided");
                return Collections.emptyList();
            }

            return this.accountClient.getAllAccounts(authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching accounts", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching accounts: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching accounts", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public Optional<UserAccount> getAccountById(final Long id) {
        LOG.debug("Fetching account with ID: {}", id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Optional.empty();
            }

            final Response response = this.accountClient.getAccount(id, authHeader);
            if (response.getStatus() == 200) {
                return Optional.of(response.readEntity(UserAccount.class));
            } else if (response.getStatus() == 404) {
                return Optional.empty();
            } else {
                throw new ServiceException("Backend error: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching account {}", id, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching account {}: {}", id, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching account {}", id, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public Optional<UserAccount> getAccountByName(final String name) {
        LOG.debug("Fetching account with name: {}", name);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Optional.empty();
            }

            final Response response = this.accountClient.getAccountByName(name, authHeader);
            if (response.getStatus() == 200) {
                return Optional.of(response.readEntity(UserAccount.class));
            } else if (response.getStatus() == 404) {
                return Optional.empty();
            } else {
                throw new ServiceException("Backend error: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching account by name {}", name, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching account by name {}: {}", name, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching account by name {}", name, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public List<UserAccount> getAccountsByUser(final Long userId) {
        LOG.debug("Fetching accounts for user: {}", userId);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.accountClient.getAccountsByUser(userId, authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching accounts for user {}", userId, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching accounts for user {}: {}", userId, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching accounts for user {}", userId, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public List<UserAccount> searchAccounts(final String query) {
        LOG.debug("Searching accounts with query: {}", query);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.accountClient.searchAccounts(query, authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while searching accounts", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while searching accounts: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while searching accounts", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<UserPayment> getOutgoingPayments(final Long accountId) {
        LOG.debug("Fetching outgoing payments for account: {}", accountId);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            final Response response = this.accountClient.getOutgoingPayments(accountId, authHeader);
            if (response.getStatus() == 200) {
                return response.readEntity(List.class);
            } else {
                throw new ServiceException("Backend error: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching outgoing payments for account {}", accountId, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching outgoing payments for account {}: {}", accountId,
                    e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching outgoing payments for account {}", accountId, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<UserPayment> getIncomingPayments(final Long accountId) {
        LOG.debug("Fetching incoming payments for account: {}", accountId);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            final Response response = this.accountClient.getIncomingPayments(accountId, authHeader);
            if (response.getStatus() == 200) {
                return response.readEntity(List.class);
            } else {
                throw new ServiceException("Backend error: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching incoming payments for account {}", accountId, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching incoming payments for account {}: {}", accountId,
                    e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching incoming payments for account {}", accountId, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public UserAccount createAccount(final UserAccount account) {
        LOG.debug("Creating new account: {}", account);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.accountClient.createAccount(account, authHeader);
            if (response.getStatus() == 201) {
                return response.readEntity(UserAccount.class);
            } else {
                throw new ServiceException("Failed to create account: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while creating account", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while creating account: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while creating account", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public UserAccount updateAccount(final UserAccount account) {
        LOG.debug("Updating account: {}", account);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.accountClient.updateAccount(account.id, account, authHeader);
            if (response.getStatus() == 200) {
                return response.readEntity(UserAccount.class);
            } else {
                throw new ServiceException("Failed to update account: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while updating account", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while updating account: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while updating account", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public boolean deleteAccount(final Long id) {
        LOG.debug("Deleting account with ID: {}", id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.accountClient.deleteAccount(id, authHeader);
            return response.getStatus() == 204;
        } catch (final ProcessingException e) {
            LOG.error("Connection error while deleting account {}", id, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while deleting account {}: {}", id, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while deleting account {}", id, e);
            throw new ServiceException("Unexpected error", e);
        }
    }
}
