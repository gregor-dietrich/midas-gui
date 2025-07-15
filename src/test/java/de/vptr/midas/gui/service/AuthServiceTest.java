package de.vptr.midas.gui.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.net.ConnectException;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.flow.server.VaadinSession;

import de.vptr.midas.gui.client.AuthClient;
import de.vptr.midas.gui.result.AuthResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @RestClient
    @Inject
    @Mock
    AuthClient authClient;

    @InjectMocks
    AuthService authService;

    private VaadinSession vaadinSession;
    private MockedStatic<VaadinSession> vaadinSessionMock;

    @BeforeEach
    void setUp() {
        this.vaadinSession = Mockito.mock(VaadinSession.class);
        this.vaadinSessionMock = mockStatic(VaadinSession.class);
        this.vaadinSessionMock.when(VaadinSession::getCurrent).thenReturn(this.vaadinSession);
    }

    @AfterEach
    void tearDown() {
        this.vaadinSessionMock.close();
    }

    @Test
    void authenticate_shouldReturnSuccess_whenCredentialsAreValid() {
        // Given
        final String username = "testuser";
        final String password = "testpass";
        // Mock successful authentication (no exception thrown)

        // When
        final AuthResult result = this.authService.authenticate(username, password);

        // Then
        assertThat(result.getStatus()).isEqualTo(AuthResult.Status.SUCCESS);
        verify(this.vaadinSession).setAttribute("authenticated.username", username);
        verify(this.vaadinSession).setAttribute("authenticated.password", password);
        verify(this.vaadinSession).setAttribute("authenticated.status", true);
    }

    @Test
    void authenticate_shouldReturnInvalidInput_whenUsernameIsEmpty() {
        // When
        final AuthResult result = this.authService.authenticate("", "password");

        // Then
        assertThat(result.getStatus()).isEqualTo(AuthResult.Status.INVALID_INPUT);
    }

    @Test
    void authenticate_shouldReturnInvalidInput_whenPasswordIsEmpty() {
        // When
        final AuthResult result = this.authService.authenticate("username", "");

        // Then
        assertThat(result.getStatus()).isEqualTo(AuthResult.Status.INVALID_INPUT);
    }

    @Test
    void authenticate_shouldReturnInvalidInput_whenUsernameIsNull() {
        // When
        final AuthResult result = this.authService.authenticate(null, "password");

        // Then
        assertThat(result.getStatus()).isEqualTo(AuthResult.Status.INVALID_INPUT);
    }

    @Test
    void authenticate_shouldReturnInvalidInput_whenPasswordIsNull() {
        // When
        final AuthResult result = this.authService.authenticate("username", null);

        // Then
        assertThat(result.getStatus()).isEqualTo(AuthResult.Status.INVALID_INPUT);
    }

    @Test
    void authenticate_shouldReturnInvalidCredentials_when401Received() {
        // Given
        final String username = "testuser";
        final String password = "wrongpass";

        final Response response = Mockito.mock(Response.class);
        final Response.StatusType statusType = Mockito.mock(Response.StatusType.class);

        when(response.getStatus()).thenReturn(401);
        when(response.getStatusInfo()).thenReturn(statusType);
        when(statusType.getStatusCode()).thenReturn(401);

        final WebApplicationException exception = new WebApplicationException(response);
        doThrow(exception).when(this.authClient).validateCredentials(anyString());

        // When
        final AuthResult result = this.authService.authenticate(username, password);

        // Then
        assertThat(result.getStatus()).isEqualTo(AuthResult.Status.INVALID_CREDENTIALS);
    }

    @Test
    void authenticate_shouldReturnBackendUnavailable_whenProcessingExceptionWithConnectException() {
        // Given
        final String username = "testuser";
        final String password = "testpass";

        final ProcessingException exception = new ProcessingException("Connection failed",
                new ConnectException("Connection refused"));
        doThrow(exception).when(this.authClient).validateCredentials(anyString());

        // When
        final AuthResult result = this.authService.authenticate(username, password);

        // Then
        assertThat(result.getStatus()).isEqualTo(AuthResult.Status.BACKEND_UNAVAILABLE);
        assertThat(result.getMessage()).contains("Connection refused");
    }

    @Test
    void authenticate_shouldReturnBackendUnavailable_whenProcessingException() {
        // Given
        final String username = "testuser";
        final String password = "testpass";

        final ProcessingException exception = new ProcessingException("Network error");
        doThrow(exception).when(this.authClient).validateCredentials(anyString());

        // When
        final AuthResult result = this.authService.authenticate(username, password);

        // Then
        assertThat(result.getStatus()).isEqualTo(AuthResult.Status.BACKEND_UNAVAILABLE);
        assertThat(result.getMessage()).contains("Connection error");
    }

    @Test
    void logout_shouldClearSessionAttributes() {
        // Given
        when(this.vaadinSession.getAttribute("authenticated.username")).thenReturn("testuser");

        // When
        this.authService.logout();

        // Then - Verify that session attributes are cleared
        verify(this.vaadinSession).setAttribute("authenticated.username", null);
        verify(this.vaadinSession).setAttribute("authenticated.password", null);
        verify(this.vaadinSession).setAttribute("authenticated.status", false);
    }

    @Test
    void isAuthenticated_shouldReturnTrue_whenSessionHasAuthenticatedStatus() {
        // Given
        when(this.vaadinSession.getAttribute("authenticated.status")).thenReturn(true);

        // When
        final boolean result = this.authService.isAuthenticated();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isAuthenticated_shouldReturnFalse_whenSessionHasNoAuthenticatedStatus() {
        // Given
        when(this.vaadinSession.getAttribute("authenticated.status")).thenReturn(null);

        // When
        final boolean result = this.authService.isAuthenticated();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isAuthenticated_shouldReturnFalse_whenSessionHasAuthenticatedStatusFalse() {
        // Given
        when(this.vaadinSession.getAttribute("authenticated.status")).thenReturn(false);

        // When
        final boolean result = this.authService.isAuthenticated();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getBasicAuthHeader_shouldReturnNull_whenNotAuthenticated() {
        // Given
        when(this.vaadinSession.getAttribute("authenticated.status")).thenReturn(false);

        // When
        final String result = this.authService.getBasicAuthHeader();

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getBasicAuthHeader_shouldReturnNull_whenUsernameIsNull() {
        // Given
        when(this.vaadinSession.getAttribute("authenticated.status")).thenReturn(true);
        when(this.vaadinSession.getAttribute("authenticated.username")).thenReturn(null);
        when(this.vaadinSession.getAttribute("authenticated.password")).thenReturn("password");

        // When
        final String result = this.authService.getBasicAuthHeader();

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getBasicAuthHeader_shouldReturnNull_whenPasswordIsNull() {
        // Given
        when(this.vaadinSession.getAttribute("authenticated.status")).thenReturn(true);
        when(this.vaadinSession.getAttribute("authenticated.username")).thenReturn("testuser");
        when(this.vaadinSession.getAttribute("authenticated.password")).thenReturn(null);

        // When
        final String result = this.authService.getBasicAuthHeader();

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getBasicAuthHeader_shouldReturnEncodedCredentials_whenAuthenticated() {
        // Given
        when(this.vaadinSession.getAttribute("authenticated.status")).thenReturn(true);
        when(this.vaadinSession.getAttribute("authenticated.username")).thenReturn("testuser");
        when(this.vaadinSession.getAttribute("authenticated.password")).thenReturn("testpass");

        // When
        final String result = this.authService.getBasicAuthHeader();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).startsWith("Basic ");
    }

    @Test
    void getUsername_shouldReturnStoredUsername() {
        // Given
        final String expectedUsername = "testuser";
        when(this.vaadinSession.getAttribute("authenticated.username")).thenReturn(expectedUsername);

        // When
        final String result = this.authService.getUsername();

        // Then
        assertThat(result).isEqualTo(expectedUsername);
    }
}
