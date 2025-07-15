package de.vptr.midas.gui.result;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthResultTest {

    @Test
    void success_shouldCreateSuccessResult() {
        // When
        final AuthResult result = AuthResult.success();

        // Then
        assertThat(result.getStatus()).isEqualTo(AuthResult.Status.SUCCESS);
        assertThat(result.getMessage()).isEqualTo("Authentication successful");
    }

    @Test
    void invalidCredentials_shouldCreateInvalidCredentialsResult() {
        // When
        final AuthResult result = AuthResult.invalidCredentials();

        // Then
        assertThat(result.getStatus()).isEqualTo(AuthResult.Status.INVALID_CREDENTIALS);
        assertThat(result.getMessage()).isEqualTo("Invalid username or password");
    }

    @Test
    void invalidInput_shouldCreateInvalidInputResult() {
        // When
        final AuthResult result = AuthResult.invalidInput();

        // Then
        assertThat(result.getStatus()).isEqualTo(AuthResult.Status.INVALID_INPUT);
        assertThat(result.getMessage()).isEqualTo("Username and password are required");
    }

    @Test
    void backendUnavailable_shouldCreateBackendUnavailableResult() {
        // Given
        final String customMessage = "Custom error message";

        // When
        final AuthResult result = AuthResult.backendUnavailable(customMessage);

        // Then
        assertThat(result.getStatus()).isEqualTo(AuthResult.Status.BACKEND_UNAVAILABLE);
        assertThat(result.getMessage()).isEqualTo("Backend service unavailable: " + customMessage);
    }
}
