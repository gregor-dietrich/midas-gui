package de.vptr.midas.gui.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.vptr.midas.gui.client.UserRankClient;
import de.vptr.midas.gui.dto.UserRankDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class UserRankServiceTest {

    @RestClient
    @Inject
    @Mock
    UserRankClient userRankClient;

    @Mock
    AuthService authService;

    @InjectMocks
    UserRankService userRankService;

    @Test
    void getAllRanks_shouldReturnRankList_whenAuthenticationProvided() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final UserRankDto rank1 = new UserRankDto("Admin");
        rank1.id = 1L;
        rank1.userAdd = true;
        rank1.userEdit = true;
        final UserRankDto rank2 = new UserRankDto("User");
        rank2.id = 2L;
        rank2.postAdd = true;
        final List<UserRankDto> expectedRanks = Arrays.asList(rank1, rank2);

        when(this.userRankClient.getAllRanks(authHeader)).thenReturn(expectedRanks);

        // When
        final List<UserRankDto> result = this.userRankService.getAllRanks(authHeader);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(rank1, rank2);
        verify(this.userRankClient).getAllRanks(authHeader);
    }

    @Test
    void getAllRanks_shouldThrowAuthenticationException_whenAuthHeaderIsNull() {
        // When & Then
        assertThatThrownBy(() -> this.userRankService.getAllRanks(null))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Authentication required");
    }

    @Test
    void getAllRanks_shouldThrowServiceException_whenProcessingExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.userRankClient.getAllRanks(authHeader))
                .thenThrow(new ProcessingException("Connection failed"));

        // When & Then
        assertThatThrownBy(() -> this.userRankService.getAllRanks(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backend connection failed");
    }

    @Test
    void getAllRanks_shouldThrowAuthenticationException_when401Error() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final Response mockResponse = Response.status(401).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.userRankClient.getAllRanks(authHeader)).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.userRankService.getAllRanks(authHeader))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session expired");
        verify(this.authService).logout();
    }

    @Test
    void getRankById_shouldReturnRank_whenRankExists() {
        // Given
        final Long rankId = 1L;
        final UserRankDto expectedRank = new UserRankDto("Test Rank");
        expectedRank.id = rankId;
        expectedRank.userAdd = true;
        expectedRank.postEdit = true;
        final Response mockResponse = Response.status(200).entity(expectedRank).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userRankClient.getRank(rankId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final UserRankDto result = this.userRankService.getRankById(rankId);

        // Then
        assertThat(result.id).isEqualTo(rankId);
        assertThat(result.name).isEqualTo("Test Rank");
        assertThat(result.userAdd).isTrue();
        assertThat(result.postEdit).isTrue();
    }

    @Test
    void getRankById_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final Long rankId = 1L;
        when(this.authService.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> this.userRankService.getRankById(rankId))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User is not authenticated");
    }

    @Test
    void getRankById_shouldThrowServiceException_whenRankNotFound() {
        // Given
        final Long rankId = 999L;
        final Response mockResponse = Response.status(404).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userRankClient.getRank(rankId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> this.userRankService.getRankById(rankId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error occurred");
    }

    @Test
    void createRank_shouldReturnCreatedRank_whenValidRank() {
        // Given
        final UserRankDto newRank = new UserRankDto("New Rank");
        newRank.userAdd = true;
        newRank.postEdit = true;
        final UserRankDto createdRank = new UserRankDto("New Rank");
        createdRank.id = 1L;
        createdRank.userAdd = true;
        createdRank.postEdit = true;
        final Response mockResponse = Response.status(201).entity(createdRank).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userRankClient.createRank(newRank, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final UserRankDto result = this.userRankService.createRank(newRank);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.name).isEqualTo("New Rank");
        assertThat(result.userAdd).isTrue();
        verify(this.userRankClient).createRank(newRank, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void createRank_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final UserRankDto newRank = new UserRankDto("New Rank");
        when(this.authService.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> this.userRankService.createRank(newRank))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User is not authenticated");
    }

    @Test
    void createRank_shouldThrowServiceException_whenCreationFails() {
        // Given
        final UserRankDto newRank = new UserRankDto("New Rank");
        final Response mockResponse = Response.status(400).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userRankClient.createRank(newRank, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> this.userRankService.createRank(newRank))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error occurred");
    }

    @Test
    void updateRank_shouldReturnUpdatedRank_whenValidRank() {
        // Given
        final UserRankDto rankToUpdate = new UserRankDto("Updated Rank");
        rankToUpdate.id = 1L;
        rankToUpdate.userEdit = true;
        rankToUpdate.postDelete = true;
        final Response mockResponse = Response.status(200).entity(rankToUpdate).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userRankClient.updateRank(1L, rankToUpdate, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final UserRankDto result = this.userRankService.updateRank(rankToUpdate);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.name).isEqualTo("Updated Rank");
        assertThat(result.userEdit).isTrue();
        verify(this.userRankClient).updateRank(1L, rankToUpdate, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void updateRank_shouldThrowServiceException_whenUpdateFails() {
        // Given
        final UserRankDto rankToUpdate = new UserRankDto("Updated Rank");
        rankToUpdate.id = 1L;
        final Response mockResponse = Response.status(400).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userRankClient.updateRank(1L, rankToUpdate, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> this.userRankService.updateRank(rankToUpdate))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error occurred");
    }

    @Test
    void deleteRank_shouldReturnTrue_whenDeletionSuccessful() {
        // Given
        final Long rankId = 1L;
        final Response mockResponse = Response.status(204).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userRankClient.deleteRank(rankId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userRankService.deleteRank(rankId);

        // Then
        assertThat(result).isTrue();
        verify(this.userRankClient).deleteRank(rankId, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void deleteRank_shouldReturnTrue_whenDeletionReturns200() {
        // Given
        final Long rankId = 1L;
        final Response mockResponse = Response.status(200).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userRankClient.deleteRank(rankId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userRankService.deleteRank(rankId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void deleteRank_shouldReturnFalse_whenDeletionFails() {
        // Given
        final Long rankId = 1L;
        final Response mockResponse = Response.status(400).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userRankClient.deleteRank(rankId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userRankService.deleteRank(rankId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void deleteRank_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final Long rankId = 1L;
        when(this.authService.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> this.userRankService.deleteRank(rankId))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User is not authenticated");
    }

    @Test
    void getRankById_shouldThrowAuthenticationException_when401ErrorOnFetch() {
        // Given
        final Long rankId = 1L;
        final Response mockResponse = Response.status(401).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userRankClient.getRank(rankId, "Basic dGVzdDp0ZXN0")).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.userRankService.getRankById(rankId))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session expired");
    }

    @Test
    void createRank_shouldThrowAuthenticationException_when401ErrorOnCreate() {
        // Given
        final UserRankDto newRank = new UserRankDto("New Rank");
        final Response mockResponse = Response.status(401).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userRankClient.createRank(newRank, "Basic dGVzdDp0ZXN0")).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.userRankService.createRank(newRank))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session expired");
    }

    @Test
    void getAllRanks_shouldThrowServiceException_whenUnexpectedExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.userRankClient.getAllRanks(authHeader))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThatThrownBy(() -> this.userRankService.getAllRanks(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void getRankById_shouldThrowServiceException_whenUnexpectedExceptionOccurs() {
        // Given
        final Long rankId = 1L;
        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.userRankClient.getRank(rankId, "Basic dGVzdDp0ZXN0"))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThatThrownBy(() -> this.userRankService.getRankById(rankId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error occurred");
    }
}
