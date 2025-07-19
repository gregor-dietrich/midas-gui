package de.vptr.midas.gui.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import de.vptr.midas.gui.client.UserAccountClient;
import de.vptr.midas.gui.dto.UserAccountDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @RestClient
    @Inject
    @Mock
    UserAccountClient accountClient;

    @Mock
    AuthService authService;

    @InjectMocks
    UserAccountService userAccountService;

    @Test
    void getAllAccounts_shouldReturnAccountList_whenAuthenticationProvided() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final UserAccountDto account1 = new UserAccountDto("Account 1");
        account1.id = 1L;
        final UserAccountDto account2 = new UserAccountDto("Account 2");
        account2.id = 2L;
        final List<UserAccountDto> expectedAccounts = Arrays.asList(account1, account2);

        when(this.accountClient.getAllAccounts(authHeader)).thenReturn(expectedAccounts);

        // When
        final List<UserAccountDto> result = this.userAccountService.getAllAccounts(authHeader);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(account1, account2);
        verify(this.accountClient).getAllAccounts(authHeader);
    }

    @Test
    void getAllAccounts_shouldThrowAuthenticationException_whenAuthHeaderIsNull() {
        // When & Then
        assertThatThrownBy(() -> this.userAccountService.getAllAccounts(null))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Authentication required");
    }

    @Test
    void getAllAccounts_shouldThrowServiceException_whenProcessingExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.accountClient.getAllAccounts(authHeader))
                .thenThrow(new ProcessingException("Connection failed"));

        // When & Then
        assertThatThrownBy(() -> this.userAccountService.getAllAccounts(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backend connection failed");
    }

    @Test
    void getAllAccounts_shouldThrowAuthenticationException_when401Error() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final Response mockResponse = Response.status(401).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.accountClient.getAllAccounts(authHeader)).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.userAccountService.getAllAccounts(authHeader))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session expired");

        verify(this.authService).logout();
    }

    @Test
    void getAccountById_shouldReturnAccount_whenAccountExists() {
        // Given
        final Long accountId = 1L;
        final UserAccountDto expectedAccount = new UserAccountDto("Test Account");
        expectedAccount.id = accountId;
        final Response mockResponse = Response.status(200).entity(expectedAccount).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.accountClient.getAccount(accountId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<UserAccountDto> result = this.userAccountService.getAccountById(accountId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id).isEqualTo(accountId);
        assertThat(result.get().name).isEqualTo("Test Account");
    }

    @Test
    void getAccountById_shouldReturnEmpty_whenAccountNotFound() {
        // Given
        final Long accountId = 999L;
        final Response mockResponse = Response.status(404).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.accountClient.getAccount(accountId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<UserAccountDto> result = this.userAccountService.getAccountById(accountId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getAccountByName_shouldReturnAccount_whenAccountExists() {
        // Given
        final String accountName = "Test Account";
        final UserAccountDto expectedAccount = new UserAccountDto(accountName);
        expectedAccount.id = 1L;
        final Response mockResponse = Response.status(200).entity(expectedAccount).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.accountClient.getAccountByName(accountName, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<UserAccountDto> result = this.userAccountService.getAccountByName(accountName);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().name).isEqualTo(accountName);
    }

    @Test
    void getAccountsByUser_shouldReturnUserAccounts_whenUserExists() {
        // Given
        final Long userId = 1L;
        final UserAccountDto account1 = new UserAccountDto("Account 1");
        final UserAccountDto account2 = new UserAccountDto("Account 2");
        final List<UserAccountDto> expectedAccounts = Arrays.asList(account1, account2);

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.accountClient.getAccountsByUser(userId, "Basic dGVzdDp0ZXN0")).thenReturn(expectedAccounts);

        // When
        final List<UserAccountDto> result = this.userAccountService.getAccountsByUser(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(account1, account2);
    }

    @Test
    void searchAccounts_shouldReturnMatchingAccounts_whenQueryProvided() {
        // Given
        final String query = "test";
        final UserAccountDto account = new UserAccountDto("Test Account");
        final List<UserAccountDto> expectedAccounts = Collections.singletonList(account);

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.accountClient.searchAccounts(query, "Basic dGVzdDp0ZXN0")).thenReturn(expectedAccounts);

        // When
        final List<UserAccountDto> result = this.userAccountService.searchAccounts(query);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name).isEqualTo("Test Account");
    }

    @Test
    void createAccount_shouldReturnCreatedAccount_whenValidAccount() {
        // Given
        final UserAccountDto newAccount = new UserAccountDto("New Account");
        final UserAccountDto createdAccount = new UserAccountDto("New Account");
        createdAccount.id = 1L;
        final Response mockResponse = Response.status(201).entity(createdAccount).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.accountClient.createAccount(newAccount, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final UserAccountDto result = this.userAccountService.createAccount(newAccount);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.name).isEqualTo("New Account");
        verify(this.accountClient).createAccount(newAccount, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void createAccount_shouldThrowServiceException_whenCreationFails() {
        // Given
        final UserAccountDto newAccount = new UserAccountDto("New Account");
        final Response mockResponse = Response.status(400).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.accountClient.createAccount(newAccount, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> this.userAccountService.createAccount(newAccount))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void updateAccount_shouldReturnUpdatedAccount_whenValidAccount() {
        // Given
        final UserAccountDto accountToUpdate = new UserAccountDto("Updated Account");
        accountToUpdate.id = 1L;
        final Response mockResponse = Response.status(200).entity(accountToUpdate).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.accountClient.updateAccount(1L, accountToUpdate, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final UserAccountDto result = this.userAccountService.updateAccount(accountToUpdate);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.name).isEqualTo("Updated Account");
        verify(this.accountClient).updateAccount(1L, accountToUpdate, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void deleteAccount_shouldReturnTrue_whenDeletionSuccessful() {
        // Given
        final Long accountId = 1L;
        final Response mockResponse = Response.status(204).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.accountClient.deleteAccount(accountId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userAccountService.deleteAccount(accountId);

        // Then
        assertThat(result).isTrue();
        verify(this.accountClient).deleteAccount(accountId, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void deleteAccount_shouldReturnFalse_whenDeletionFails() {
        // Given
        final Long accountId = 1L;
        final Response mockResponse = Response.status(400).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.accountClient.deleteAccount(accountId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userAccountService.deleteAccount(accountId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getAccountById_shouldReturnEmpty_whenNotAuthenticated() {
        // Given
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When
        final Optional<UserAccountDto> result = this.userAccountService.getAccountById(1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void createAccount_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final UserAccountDto newAccount = new UserAccountDto("New Account");
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> this.userAccountService.createAccount(newAccount))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void getAllAccounts_shouldThrowServiceException_whenUnexpectedExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.accountClient.getAllAccounts(authHeader))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThatThrownBy(() -> this.userAccountService.getAllAccounts(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void getAccountById_shouldThrowServiceException_whenWebApplicationExceptionOccurs() {
        // Given
        final Long accountId = 1L;
        final Response mockResponse = Response.status(500).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.accountClient.getAccount(accountId, "Basic dGVzdDp0ZXN0")).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.userAccountService.getAccountById(accountId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backend error: 500");
    }
}
