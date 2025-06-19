package de.vptr.midas.gui.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.vptr.midas.gui.client.HealthClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class HealthServiceTest {

    @RestClient
    @Inject
    @Mock
    HealthClient healthClient;

    @InjectMocks
    HealthService healthService;

    @Test
    void isBackendAvailable_shouldReturnTrue_whenStatusIsBelow500() {
        // Given
        final Response response = Mockito.mock(Response.class);
        when(this.healthClient.checkHealth()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);

        // When
        final boolean result = this.healthService.isBackendAvailable();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isBackendAvailable_shouldReturnTrue_whenStatusIs400() {
        // Given
        final Response response = Mockito.mock(Response.class);
        when(this.healthClient.checkHealth()).thenReturn(response);
        when(response.getStatus()).thenReturn(400);

        // When
        final boolean result = this.healthService.isBackendAvailable();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isBackendAvailable_shouldReturnFalse_whenStatusIs500OrHigher() {
        // Given
        final Response response = Mockito.mock(Response.class);
        when(this.healthClient.checkHealth()).thenReturn(response);
        when(response.getStatus()).thenReturn(500);

        // When
        final boolean result = this.healthService.isBackendAvailable();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isBackendAvailable_shouldReturnFalse_whenProcessingExceptionOccurs() {
        // Given
        when(this.healthClient.checkHealth()).thenThrow(new ProcessingException("Connection failed"));

        // When
        final boolean result = this.healthService.isBackendAvailable();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isBackendAvailable_shouldReturnTrue_whenNonProcessingExceptionOccurs() {
        // Given
        when(this.healthClient.checkHealth()).thenThrow(new RuntimeException("Some other error"));

        // When
        final boolean result = this.healthService.isBackendAvailable();

        // Then
        assertThat(result).isTrue();
    }
}
