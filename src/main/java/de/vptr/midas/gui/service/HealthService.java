package de.vptr.midas.gui.service;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.vptr.midas.gui.client.HealthClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;

@ApplicationScoped
public class HealthService {

    private static final Logger LOG = LoggerFactory.getLogger(HealthService.class);

    @Inject
    @RestClient
    HealthClient healthClient;

    public boolean isBackendAvailable() {
        try {
            LOG.trace("Checking backend availability");

            final var response = this.healthClient.checkHealth();
            final var available = response.getStatus() < 500; // Any non-server error means backend is reachable

            LOG.trace("Backend availability check - Status: {}, Available: {}", response.getStatus(), available);
            return available;

        } catch (final ProcessingException e) {
            LOG.error("Backend unavailable - Connection error: {}", e.getMessage());
            return false;

        } catch (final Exception e) {
            LOG.debug("Backend is available (got HTTP response, even if error): {}", e.getMessage());
            return true;
        }
    }
}