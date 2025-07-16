package de.vptr.midas.gui.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vptr.midas.gui.client.UserPaymentClient;
import de.vptr.midas.gui.dto.UserPaymentDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class UserPaymentService {

    private static final Logger LOG = LoggerFactory.getLogger(UserPaymentService.class);

    @Inject
    @RestClient
    UserPaymentClient paymentClient;

    @Inject
    AuthService authService;

    public List<UserPaymentDto> getAllPayments(final String authHeader)
            throws ServiceException, AuthenticationException {
        LOG.debug("Fetching all payments with provided auth header");
        try {
            if (authHeader == null) {
                LOG.warn("No authentication header provided");
                return Collections.emptyList();
            }

            return this.paymentClient.getAllPayments(authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching payments", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching payments: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching payments", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public Optional<UserPaymentDto> getPaymentById(final Long id) {
        LOG.debug("Fetching payment with ID: {}", id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Optional.empty();
            }

            final Response response = this.paymentClient.getPayment(id, authHeader);
            if (response.getStatus() == 200) {
                return Optional.of(response.readEntity(UserPaymentDto.class));
            } else if (response.getStatus() == 404) {
                return Optional.empty();
            } else {
                throw new ServiceException("Backend error: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching payment {}", id, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching payment {}: {}", id, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching payment {}", id, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public List<UserPaymentDto> getPaymentsByUser(final Long userId) {
        LOG.debug("Fetching payments for user: {}", userId);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.paymentClient.getPaymentsByUser(userId, authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching payments for user {}", userId, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching payments for user {}: {}", userId, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching payments for user {}", userId, e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public List<UserPaymentDto> getRecentPayments(final int limit) {
        LOG.debug("Fetching recent payments with limit: {}", limit);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.paymentClient.getRecentPayments(limit, authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching recent payments", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching recent payments: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching recent payments", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public List<UserPaymentDto> getPaymentsByDateRange(final LocalDate startDate, final LocalDate endDate) {
        LOG.debug("Fetching payments for date range: {} to {}", startDate, endDate);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                return Collections.emptyList();
            }

            return this.paymentClient.getPaymentsByDateRange(startDate, endDate, authHeader);
        } catch (final ProcessingException e) {
            LOG.error("Connection error while fetching payments by date range", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while fetching payments by date range: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while fetching payments by date range", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public UserPaymentDto createPayment(final UserPaymentDto payment) {
        LOG.debug("Creating new payment: {}", payment);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.paymentClient.createPayment(payment, authHeader);
            if (response.getStatus() == 201) {
                return response.readEntity(UserPaymentDto.class);
            } else {
                throw new ServiceException("Failed to create payment: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while creating payment", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while creating payment: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while creating payment", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public UserPaymentDto updatePayment(final UserPaymentDto payment) {
        LOG.debug("Updating payment: {}", payment);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.paymentClient.updatePayment(payment.id, payment, authHeader);
            if (response.getStatus() == 200) {
                return response.readEntity(UserPaymentDto.class);
            } else {
                throw new ServiceException("Failed to update payment: " + response.getStatus());
            }
        } catch (final ProcessingException e) {
            LOG.error("Connection error while updating payment", e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while updating payment: {}", e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while updating payment", e);
            throw new ServiceException("Unexpected error", e);
        }
    }

    public boolean deletePayment(final Long id) {
        LOG.debug("Deleting payment with ID: {}", id);
        try {
            final var authHeader = this.authService.getBasicAuthHeader();
            if (authHeader == null) {
                LOG.warn("No authentication header available");
                throw new AuthenticationException("Not authenticated");
            }

            final Response response = this.paymentClient.deletePayment(id, authHeader);
            return response.getStatus() == 204;
        } catch (final ProcessingException e) {
            LOG.error("Connection error while deleting payment {}", id, e);
            throw new ServiceException("Backend connection failed", e);
        } catch (final WebApplicationException e) {
            LOG.error("HTTP error while deleting payment {}: {}", id, e.getResponse().getStatus());
            if (e.getResponse().getStatus() == 401) {
                this.authService.logout();
                throw new AuthenticationException("Session expired");
            }
            throw new ServiceException("Backend error: " + e.getResponse().getStatus(), e);
        } catch (final Exception e) {
            LOG.error("Unexpected error while deleting payment {}", id, e);
            throw new ServiceException("Unexpected error", e);
        }
    }
}
