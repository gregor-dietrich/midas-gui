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

import de.vptr.midas.gui.client.UserGroupClient;
import de.vptr.midas.gui.dto.UserDto;
import de.vptr.midas.gui.dto.UserGroupDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class UserGroupServiceTest {

    @RestClient
    @Inject
    @Mock
    UserGroupClient groupClient;

    @Mock
    AuthService authService;

    @InjectMocks
    UserGroupService userGroupService;

    @Test
    void getAllGroups_shouldReturnGroupList_whenAuthenticationProvided() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final UserGroupDto group1 = new UserGroupDto(1L, "Group 1");
        final UserGroupDto group2 = new UserGroupDto(2L, "Group 2");
        final List<UserGroupDto> expectedGroups = Arrays.asList(group1, group2);

        when(this.groupClient.getAllGroups(authHeader)).thenReturn(expectedGroups);

        // When
        final List<UserGroupDto> result = this.userGroupService.getAllGroups(authHeader);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(group1, group2);
        verify(this.groupClient).getAllGroups(authHeader);
    }

    @Test
    void getAllGroups_shouldThrowServiceException_whenAuthHeaderIsNull() {
        // When & Then
        assertThatThrownBy(() -> this.userGroupService.getAllGroups(null))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void getAllGroups_shouldThrowServiceException_whenProcessingExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.groupClient.getAllGroups(authHeader))
                .thenThrow(new ProcessingException("Connection failed"));

        // When & Then
        assertThatThrownBy(() -> this.userGroupService.getAllGroups(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backend connection failed");
    }

    @Test
    void getAllGroups_shouldThrowAuthenticationException_when401Error() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final Response mockResponse = Response.status(401).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.groupClient.getAllGroups(authHeader)).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.userGroupService.getAllGroups(authHeader))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session expired");

        verify(this.authService).logout();
    }

    @Test
    void getGroupById_shouldReturnGroup_whenGroupExists() {
        // Given
        final Long groupId = 1L;
        final UserGroupDto expectedGroup = new UserGroupDto(groupId, "Test Group");
        final Response mockResponse = Response.status(200).entity(expectedGroup).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.groupClient.getGroup(groupId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<UserGroupDto> result = this.userGroupService.getGroupById(groupId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id).isEqualTo(groupId);
        assertThat(result.get().name).isEqualTo("Test Group");
    }

    @Test
    void getGroupById_shouldReturnEmpty_whenGroupNotFound() {
        // Given
        final Long groupId = 999L;
        final Response mockResponse = Response.status(404).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.groupClient.getGroup(groupId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<UserGroupDto> result = this.userGroupService.getGroupById(groupId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getUsersInGroup_shouldReturnUserList_whenGroupExists() {
        // Given
        final Long groupId = 1L;
        final UserDto user1 = new UserDto("user1", "user1@example.com");
        user1.id = 1L;
        final UserDto user2 = new UserDto("user2", "user2@example.com");
        user2.id = 2L;
        final List<UserDto> expectedUsers = Arrays.asList(user1, user2);

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.groupClient.getUsersInGroup(groupId, "Basic dGVzdDp0ZXN0")).thenReturn(expectedUsers);

        // When
        final List<UserDto> result = this.userGroupService.getUsersInGroup(groupId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(user1, user2);
        verify(this.groupClient).getUsersInGroup(groupId, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void createGroup_shouldReturnCreatedGroup_whenValidGroup() {
        // Given
        final UserGroupDto newGroup = new UserGroupDto(null, "New Group");
        final UserGroupDto createdGroup = new UserGroupDto(1L, "New Group");
        createdGroup.created = LocalDateTime.now();
        final Response mockResponse = Response.status(201).entity(createdGroup).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.groupClient.createGroup(newGroup, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final UserGroupDto result = this.userGroupService.createGroup(newGroup);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.name).isEqualTo("New Group");
        verify(this.groupClient).createGroup(newGroup, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void createGroup_shouldThrowServiceException_whenCreationFails() {
        // Given
        final UserGroupDto newGroup = new UserGroupDto(null, "New Group");
        final Response mockResponse = Response.status(400).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.groupClient.createGroup(newGroup, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> this.userGroupService.createGroup(newGroup))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void updateGroup_shouldReturnUpdatedGroup_whenValidGroup() {
        // Given
        final UserGroupDto groupToUpdate = new UserGroupDto(1L, "Updated Group");
        final Response mockResponse = Response.status(200).entity(groupToUpdate).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.groupClient.updateGroup(1L, groupToUpdate, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final UserGroupDto result = this.userGroupService.updateGroup(groupToUpdate);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.name).isEqualTo("Updated Group");
        verify(this.groupClient).updateGroup(1L, groupToUpdate, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void deleteGroup_shouldReturnTrue_whenDeletionSuccessful() {
        // Given
        final Long groupId = 1L;
        final Response mockResponse = Response.status(204).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.groupClient.deleteGroup(groupId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userGroupService.deleteGroup(groupId);

        // Then
        assertThat(result).isTrue();
        verify(this.groupClient).deleteGroup(groupId, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void deleteGroup_shouldReturnTrue_whenDeletionReturns200() {
        // Given
        final Long groupId = 1L;
        final Response mockResponse = Response.status(200).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.groupClient.deleteGroup(groupId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userGroupService.deleteGroup(groupId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void deleteGroup_shouldReturnFalse_whenDeletionFails() {
        // Given
        final Long groupId = 1L;
        final Response mockResponse = Response.status(400).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.groupClient.deleteGroup(groupId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userGroupService.deleteGroup(groupId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void addUserToGroup_shouldReturnTrue_whenSuccessful() {
        // Given
        final Long groupId = 1L;
        final Long userId = 1L;
        final Response mockResponse = Response.status(200).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.groupClient.addUserToGroup(groupId, userId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userGroupService.addUserToGroup(groupId, userId);

        // Then
        assertThat(result).isTrue();
        verify(this.groupClient).addUserToGroup(groupId, userId, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void removeUserFromGroup_shouldReturnTrue_whenSuccessful() {
        // Given
        final Long groupId = 1L;
        final Long userId = 1L;
        final Response mockResponse = Response.status(204).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.groupClient.removeUserFromGroup(groupId, userId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userGroupService.removeUserFromGroup(groupId, userId);

        // Then
        assertThat(result).isTrue();
        verify(this.groupClient).removeUserFromGroup(groupId, userId, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void getGroupById_shouldReturnEmpty_whenNotAuthenticated() {
        // Given
        final Long groupId = 1L;
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When
        final Optional<UserGroupDto> result = this.userGroupService.getGroupById(groupId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void createGroup_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final UserGroupDto newGroup = new UserGroupDto(null, "New Group");
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> this.userGroupService.createGroup(newGroup))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void getAllGroups_shouldThrowServiceException_whenUnexpectedExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.groupClient.getAllGroups(authHeader))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThatThrownBy(() -> this.userGroupService.getAllGroups(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void getGroupById_shouldThrowServiceException_whenWebApplicationExceptionOccurs() {
        // Given
        final Long groupId = 1L;
        final Response mockResponse = Response.status(500).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.groupClient.getGroup(groupId, "Basic dGVzdDp0ZXN0")).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.userGroupService.getGroupById(groupId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backend error: 500");
    }

    @Test
    void getUsersInGroup_shouldReturnEmptyList_whenGroupHasNoUsers() {
        // Given
        final Long groupId = 1L;
        final List<UserDto> emptyList = Collections.emptyList();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.groupClient.getUsersInGroup(groupId, "Basic dGVzdDp0ZXN0")).thenReturn(emptyList);

        // When
        final List<UserDto> result = this.userGroupService.getUsersInGroup(groupId);

        // Then
        assertThat(result).isEmpty();
    }
}
