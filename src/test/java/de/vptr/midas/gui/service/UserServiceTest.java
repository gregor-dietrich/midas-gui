package de.vptr.midas.gui.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.vptr.midas.gui.client.UserClient;
import de.vptr.midas.gui.dto.UserDto;
import de.vptr.midas.gui.dto.UserRankDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @RestClient
    @Inject
    @Mock
    UserClient userClient;

    @Mock
    AuthService authService;

    @InjectMocks
    UserService userService;

    @Test
    void getAllUsers_shouldReturnUserList_whenAuthenticationProvided() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final UserRankDto rank = new UserRankDto("User");
        rank.id = 1L;

        final UserDto user1 = new UserDto("user1", "user1@example.com");
        user1.id = 1L;
        user1.rank = rank;
        user1.activated = true;
        user1.banned = false;
        user1.created = LocalDateTime.now();

        final UserDto user2 = new UserDto("user2", "user2@example.com");
        user2.id = 2L;
        user2.rank = rank;
        user2.activated = true;
        user2.banned = false;
        user2.created = LocalDateTime.now();

        final List<UserDto> expectedUsers = Arrays.asList(user1, user2);

        when(this.userClient.getAllUsers(authHeader)).thenReturn(expectedUsers);

        // When
        final List<UserDto> result = this.userService.getAllUsers(authHeader);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(user1, user2);
        verify(this.userClient).getAllUsers(authHeader);
    }

    @Test
    void getAllUsers_shouldThrowAuthenticationException_whenAuthHeaderIsNull() {
        // When & Then
        assertThatThrownBy(() -> this.userService.getAllUsers(null))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void getAllUsers_shouldThrowServiceException_whenProcessingExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.userClient.getAllUsers(authHeader))
                .thenThrow(new ProcessingException("Connection failed"));

        // When & Then
        assertThatThrownBy(() -> this.userService.getAllUsers(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backend connection failed");
    }

    @Test
    void getAllUsers_shouldThrowAuthenticationException_when401Error() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final Response mockResponse = Response.status(401).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.userClient.getAllUsers(authHeader)).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.userService.getAllUsers(authHeader))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session expired");

        verify(this.authService).logout();
    }

    @Test
    void getCurrentUser_shouldReturnUser_whenAuthenticated() {
        // Given
        final UserDto expectedUser = new UserDto("currentuser", "current@example.com");
        expectedUser.id = 1L;
        expectedUser.activated = true;
        final Response mockResponse = Response.status(200).entity(expectedUser).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.getCurrentUser("Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<UserDto> result = this.userService.getCurrentUser();

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().username).isEqualTo("currentuser");
        assertThat(result.get().email).isEqualTo("current@example.com");
    }

    @Test
    void getCurrentUser_shouldReturnEmpty_whenNotAuthenticated() {
        // Given
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When
        final Optional<UserDto> result = this.userService.getCurrentUser();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentUser_shouldReturnEmpty_whenUserNotFound() {
        // Given
        final Response mockResponse = Response.status(404).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.getCurrentUser("Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<UserDto> result = this.userService.getCurrentUser();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {
        // Given
        final Long userId = 1L;
        final UserDto expectedUser = new UserDto("testuser", "test@example.com");
        expectedUser.id = userId;
        final Response mockResponse = Response.status(200).entity(expectedUser).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.getUser(userId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<UserDto> result = this.userService.getUserById(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id).isEqualTo(userId);
        assertThat(result.get().username).isEqualTo("testuser");
    }

    @Test
    void getUserById_shouldReturnEmpty_whenUserNotFound() {
        // Given
        final Long userId = 999L;
        final Response mockResponse = Response.status(404).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.getUser(userId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<UserDto> result = this.userService.getUserById(userId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getUserByUsername_shouldReturnUser_whenUserExists() {
        // Given
        final String username = "testuser";
        final UserDto expectedUser = new UserDto(username, "test@example.com");
        expectedUser.id = 1L;
        final Response mockResponse = Response.status(200).entity(expectedUser).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.getUserByUsername(username, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<UserDto> result = this.userService.getUserByUsername(username);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().username).isEqualTo(username);
    }

    @Test
    void createUser_shouldReturnCreatedUser_whenValidUser() {
        // Given
        final UserDto newUser = new UserDto("newuser", "new@example.com");
        newUser.password = "password123";
        final UserDto createdUser = new UserDto("newuser", "new@example.com");
        createdUser.id = 1L;
        createdUser.activated = false;
        createdUser.banned = false;
        createdUser.created = LocalDateTime.now();
        final Response mockResponse = Response.status(201).entity(createdUser).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.createUser(newUser, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final UserDto result = this.userService.createUser(newUser);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.username).isEqualTo("newuser");
        assertThat(result.email).isEqualTo("new@example.com");
        verify(this.userClient).createUser(newUser, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void createUser_shouldThrowServiceException_whenCreationFails() {
        // Given
        final UserDto newUser = new UserDto("newuser", "new@example.com");
        final Response mockResponse = Response.status(400).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.createUser(newUser, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> this.userService.createUser(newUser))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void updateUser_shouldReturnUpdatedUser_whenValidUser() {
        // Given
        final UserDto userToUpdate = new UserDto("updateduser", "updated@example.com");
        userToUpdate.id = 1L;
        userToUpdate.activated = true;
        final Response mockResponse = Response.status(200).entity(userToUpdate).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.updateUser(1L, userToUpdate, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final UserDto result = this.userService.updateUser(userToUpdate);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.username).isEqualTo("updateduser");
        assertThat(result.email).isEqualTo("updated@example.com");
        verify(this.userClient).updateUser(1L, userToUpdate, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void updateUser_shouldThrowServiceException_whenUpdateFails() {
        // Given
        final UserDto userToUpdate = new UserDto("updateduser", "updated@example.com");
        userToUpdate.id = 1L;
        final Response mockResponse = Response.status(400).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.updateUser(1L, userToUpdate, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> this.userService.updateUser(userToUpdate))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void deleteUser_shouldReturnTrue_whenDeletionSuccessful() {
        // Given
        final Long userId = 1L;
        final Response mockResponse = Response.status(204).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.deleteUser(userId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userService.deleteUser(userId);

        // Then
        assertThat(result).isTrue();
        verify(this.userClient).deleteUser(userId, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void deleteUser_shouldReturnFalse_whenDeletionFails() {
        // Given
        final Long userId = 1L;
        final Response mockResponse = Response.status(400).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.deleteUser(userId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userService.deleteUser(userId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getUserById_shouldReturnEmpty_whenNotAuthenticated() {
        // Given
        final Long userId = 1L;
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When
        final Optional<UserDto> result = this.userService.getUserById(userId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void createUser_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final UserDto newUser = new UserDto("newuser", "new@example.com");
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> this.userService.createUser(newUser))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void getAllUsers_shouldThrowServiceException_whenUnexpectedExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.userClient.getAllUsers(authHeader))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThatThrownBy(() -> this.userService.getAllUsers(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void getUserById_shouldThrowServiceException_whenWebApplicationExceptionOccurs() {
        // Given
        final Long userId = 1L;
        final Response mockResponse = Response.status(500).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.getUser(userId, "Basic dGVzdDp0ZXN0")).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.userService.getUserById(userId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backend error: 500");
    }

    @Test
    void getCurrentUser_shouldThrowAuthenticationException_when401Error() {
        // Given
        final Response mockResponse = Response.status(401).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.getCurrentUser("Basic dGVzdDp0ZXN0")).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.userService.getCurrentUser())
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session expired");

        verify(this.authService).logout();
    }

    @Test
    void getAllUsers_shouldReturnEmptyList_whenNoUsersExist() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final List<UserDto> emptyList = Collections.emptyList();

        when(this.userClient.getAllUsers(authHeader)).thenReturn(emptyList);

        // When
        final List<UserDto> result = this.userService.getAllUsers(authHeader);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void updateUser_shouldHandleServiceExceptionProperly() {
        // Given
        final UserDto userToUpdate = new UserDto("updateduser", "updated@example.com");
        userToUpdate.id = 1L;

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userClient.updateUser(1L, userToUpdate, "Basic dGVzdDp0ZXN0"))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThatThrownBy(() -> this.userService.updateUser(userToUpdate))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }
}
