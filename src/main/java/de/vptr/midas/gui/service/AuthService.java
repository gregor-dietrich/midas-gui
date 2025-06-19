package de.vptr.midas.gui.service;

import java.net.ConnectException;
import java.util.Base64;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.server.VaadinSession;

import de.vptr.midas.gui.client.AuthClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class AuthService {
    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    @Inject
    @RestClient
    AuthClient authClient;

    private static final String USERNAME_KEY = "authenticated.username";
    private static final String PASSWORD_KEY = "authenticated.password";
    private static final String AUTHENTICATED_KEY = "authenticated.status";

    public AuthResult authenticate(final String username, final String password) {
        LOG.trace("Starting authentication for user: {}", username);

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            LOG.trace("Username or password is empty");
            return AuthResult.invalidInput();
        }

        try {
            final var testAuthHeader = "Basic "
                    + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

            this.authClient.validateCredentials(testAuthHeader);

            VaadinSession.getCurrent().setAttribute(USERNAME_KEY, username);
            VaadinSession.getCurrent().setAttribute(PASSWORD_KEY, password);
            VaadinSession.getCurrent().setAttribute(AUTHENTICATED_KEY, true);

            LOG.trace("User authenticated successfully: {}", username);
            return AuthResult.success();

        } catch (final WebApplicationException e) {
            if (e.getResponse().getStatus() == 401) {
                LOG.trace("Authentication failed - invalid credentials for user: {}", username);
                return AuthResult.invalidCredentials();
            } else {
                LOG.error("Backend returned HTTP error {} for user: {}", e.getResponse().getStatus(), username);
                return AuthResult.backendUnavailable("HTTP " + e.getResponse().getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Backend connection failed for user: {} - Error: {}", username, e.getMessage(), e);
            if (e.getCause() instanceof ConnectException) {
                return AuthResult.backendUnavailable("Connection refused");
            } else {
                return AuthResult.backendUnavailable("Connection error: " + e.getMessage());
            }
        } catch (final Exception e) {
            LOG.error("Unexpected error during authentication for user: {} - Error: {}", username, e.getMessage(), e);
            return AuthResult.backendUnavailable("Unexpected error: " + e.getMessage());
        }
    }

    public void logout() {
        final var username = this.getUsername();
        LOG.trace("Logging out user: {}", username);

        VaadinSession.getCurrent().setAttribute(USERNAME_KEY, null);
        VaadinSession.getCurrent().setAttribute(PASSWORD_KEY, null);
        VaadinSession.getCurrent().setAttribute(AUTHENTICATED_KEY, false);

        LOG.trace("User logged out");
    }

    public boolean isAuthenticated() {
        final var authenticated = (Boolean) VaadinSession.getCurrent().getAttribute(AUTHENTICATED_KEY);
        final var result = authenticated != null && authenticated;
        LOG.trace("Checking authentication status: {}", result);
        return result;
    }

    public String getBasicAuthHeader() {
        if (!this.isAuthenticated()) {
            LOG.warn("Attempting to get auth header but user is not authenticated");
            return null;
        }
        final var username = this.getUsername();
        final var password = (String) VaadinSession.getCurrent().getAttribute(PASSWORD_KEY);
        final var credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    public String getUsername() {
        return (String) VaadinSession.getCurrent().getAttribute(USERNAME_KEY);
    }
}
