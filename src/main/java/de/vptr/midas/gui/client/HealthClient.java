package de.vptr.midas.gui.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "midas-api")
@Path("/health")
public interface HealthClient {

    @HEAD
    Response checkHealth();
}