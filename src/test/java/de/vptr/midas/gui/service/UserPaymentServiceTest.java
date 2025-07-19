package de.vptr.midas.gui.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import de.vptr.midas.gui.client.UserPaymentClient;
import de.vptr.midas.gui.dto.UserDto;
import de.vptr.midas.gui.dto.UserPaymentDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class UserPaymentServiceTest {

    @RestClient
    @Inject
    @Mock
    UserPaymentClient paymentClient;

    @Mock
    AuthService authService;

    @InjectMocks
    UserPaymentService userPaymentService;

    @Test
    void getAllPayments_shouldReturnPaymentList_whenAuthenticationProvided() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final UserDto user = new UserDto("testuser", "test@example.com");
        user.id = 1L;

        final UserPaymentDto payment1 = new UserPaymentDto(user, 1L, 2L, new BigDecimal("100.00"),
                LocalDate.now(), "Payment 1", LocalDateTime.now(), LocalDateTime.now());
        payment1.id = 1L;
        final UserPaymentDto payment2 = new UserPaymentDto(user, 2L, 1L, new BigDecimal("50.00"),
                LocalDate.now(), "Payment 2", LocalDateTime.now(), LocalDateTime.now());
        payment2.id = 2L;
        final List<UserPaymentDto> expectedPayments = Arrays.asList(payment1, payment2);

        when(this.paymentClient.getAllPayments(authHeader)).thenReturn(expectedPayments);

        // When
        final List<UserPaymentDto> result = this.userPaymentService.getAllPayments(authHeader);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(payment1, payment2);
        verify(this.paymentClient).getAllPayments(authHeader);
    }

    @Test
    void getAllPayments_shouldThrowAuthenticationException_whenAuthHeaderIsNull() {
        // When & Then
        assertThatThrownBy(() -> this.userPaymentService.getAllPayments(null))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Authentication required");
    }

    @Test
    void getAllPayments_shouldThrowServiceException_whenProcessingExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.paymentClient.getAllPayments(authHeader))
                .thenThrow(new ProcessingException("Connection failed"));

        // When & Then
        assertThatThrownBy(() -> this.userPaymentService.getAllPayments(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backend connection failed");
    }

    @Test
    void getAllPayments_shouldThrowAuthenticationException_when401Error() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final Response mockResponse = Response.status(401).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.paymentClient.getAllPayments(authHeader)).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.userPaymentService.getAllPayments(authHeader))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session expired");

        verify(this.authService).logout();
    }

    @Test
    void getPaymentById_shouldReturnPayment_whenPaymentExists() {
        // Given
        final Long paymentId = 1L;
        final UserDto user = new UserDto("testuser", "test@example.com");
        final UserPaymentDto expectedPayment = new UserPaymentDto(user, 1L, 2L, new BigDecimal("100.00"),
                LocalDate.now(), "Test Payment", LocalDateTime.now(), LocalDateTime.now());
        expectedPayment.id = paymentId;
        final Response mockResponse = Response.status(200).entity(expectedPayment).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.paymentClient.getPayment(paymentId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<UserPaymentDto> result = this.userPaymentService.getPaymentById(paymentId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id).isEqualTo(paymentId);
        assertThat(result.get().comment).isEqualTo("Test Payment");
    }

    @Test
    void getPaymentById_shouldReturnEmpty_whenPaymentNotFound() {
        // Given
        final Long paymentId = 999L;
        final Response mockResponse = Response.status(404).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.paymentClient.getPayment(paymentId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<UserPaymentDto> result = this.userPaymentService.getPaymentById(paymentId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getPaymentsByUser_shouldReturnUserPayments_whenUserExists() {
        // Given
        final Long userId = 1L;
        final UserDto user = new UserDto("testuser", "test@example.com");
        user.id = userId;
        final UserPaymentDto payment = new UserPaymentDto(user, 1L, 2L, new BigDecimal("100.00"),
                LocalDate.now(), "User Payment", LocalDateTime.now(), LocalDateTime.now());
        final List<UserPaymentDto> expectedPayments = Collections.singletonList(payment);

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.paymentClient.getPaymentsByUser(userId, "Basic dGVzdDp0ZXN0")).thenReturn(expectedPayments);

        // When
        final List<UserPaymentDto> result = this.userPaymentService.getPaymentsByUser(userId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).comment).isEqualTo("User Payment");
    }

    @Test
    void getRecentPayments_shouldReturnRecentPayments_whenLimitProvided() {
        // Given
        final int limit = 5;
        final UserDto user = new UserDto("testuser", "test@example.com");
        final UserPaymentDto recentPayment = new UserPaymentDto(user, 1L, 2L, new BigDecimal("100.00"),
                LocalDate.now(), "Recent Payment", LocalDateTime.now(), LocalDateTime.now());
        final List<UserPaymentDto> expectedPayments = Collections.singletonList(recentPayment);

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.paymentClient.getRecentPayments(limit, "Basic dGVzdDp0ZXN0")).thenReturn(expectedPayments);

        // When
        final List<UserPaymentDto> result = this.userPaymentService.getRecentPayments(limit);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).comment).isEqualTo("Recent Payment");
        verify(this.paymentClient).getRecentPayments(limit, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void getPaymentsByDateRange_shouldReturnPaymentsInRange_whenValidDates() {
        // Given
        final LocalDate startDate = LocalDate.of(2023, 1, 1);
        final LocalDate endDate = LocalDate.of(2023, 12, 31);
        final UserDto user = new UserDto("testuser", "test@example.com");
        final UserPaymentDto payment = new UserPaymentDto(user, 1L, 2L, new BigDecimal("100.00"),
                LocalDate.of(2023, 6, 15), "Date Range Payment", LocalDateTime.now(), LocalDateTime.now());
        final List<UserPaymentDto> expectedPayments = Collections.singletonList(payment);

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.paymentClient.getPaymentsByDateRange(startDate, endDate, "Basic dGVzdDp0ZXN0"))
                .thenReturn(expectedPayments);

        // When
        final List<UserPaymentDto> result = this.userPaymentService.getPaymentsByDateRange(startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).comment).isEqualTo("Date Range Payment");
        verify(this.paymentClient).getPaymentsByDateRange(startDate, endDate, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void createPayment_shouldReturnCreatedPayment_whenValidPayment() {
        // Given
        final UserDto user = new UserDto("testuser", "test@example.com");
        final UserPaymentDto newPayment = new UserPaymentDto(user, 1L, 2L, new BigDecimal("100.00"),
                LocalDate.now(), "New Payment", null, null);
        final UserPaymentDto createdPayment = new UserPaymentDto(user, 1L, 2L, new BigDecimal("100.00"),
                LocalDate.now(), "New Payment", LocalDateTime.now(), LocalDateTime.now());
        createdPayment.id = 1L;
        final Response mockResponse = Response.status(201).entity(createdPayment).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.paymentClient.createPayment(newPayment, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final UserPaymentDto result = this.userPaymentService.createPayment(newPayment);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.comment).isEqualTo("New Payment");
        verify(this.paymentClient).createPayment(newPayment, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void createPayment_shouldThrowServiceException_whenCreationFails() {
        // Given
        final UserPaymentDto newPayment = new UserPaymentDto();
        newPayment.comment = "New Payment";
        final Response mockResponse = Response.status(400).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.paymentClient.createPayment(newPayment, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> this.userPaymentService.createPayment(newPayment))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void updatePayment_shouldReturnUpdatedPayment_whenValidPayment() {
        // Given
        final UserDto user = new UserDto("testuser", "test@example.com");
        final UserPaymentDto paymentToUpdate = new UserPaymentDto(user, 1L, 2L, new BigDecimal("150.00"),
                LocalDate.now(), "Updated Payment", LocalDateTime.now(), LocalDateTime.now());
        paymentToUpdate.id = 1L;
        final Response mockResponse = Response.status(200).entity(paymentToUpdate).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.paymentClient.updatePayment(1L, paymentToUpdate, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final UserPaymentDto result = this.userPaymentService.updatePayment(paymentToUpdate);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.comment).isEqualTo("Updated Payment");
        verify(this.paymentClient).updatePayment(1L, paymentToUpdate, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void deletePayment_shouldReturnTrue_whenDeletionSuccessful() {
        // Given
        final Long paymentId = 1L;
        final Response mockResponse = Response.status(204).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.paymentClient.deletePayment(paymentId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userPaymentService.deletePayment(paymentId);

        // Then
        assertThat(result).isTrue();
        verify(this.paymentClient).deletePayment(paymentId, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void deletePayment_shouldReturnFalse_whenDeletionFails() {
        // Given
        final Long paymentId = 1L;
        final Response mockResponse = Response.status(400).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.paymentClient.deletePayment(paymentId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.userPaymentService.deletePayment(paymentId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getPaymentById_shouldReturnEmpty_whenNotAuthenticated() {
        // Given
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When
        final Optional<UserPaymentDto> result = this.userPaymentService.getPaymentById(1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void createPayment_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final UserPaymentDto newPayment = new UserPaymentDto();
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> this.userPaymentService.createPayment(newPayment))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void getAllPayments_shouldThrowServiceException_whenUnexpectedExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.paymentClient.getAllPayments(authHeader))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThatThrownBy(() -> this.userPaymentService.getAllPayments(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void getPaymentById_shouldThrowServiceException_whenWebApplicationExceptionOccurs() {
        // Given
        final Long paymentId = 1L;
        final Response mockResponse = Response.status(500).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.paymentClient.getPayment(paymentId, "Basic dGVzdDp0ZXN0")).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.userPaymentService.getPaymentById(paymentId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backend error: 500");
    }

    @Test
    void getPaymentsByUser_shouldReturnEmptyList_whenUserHasNoPayments() {
        // Given
        final Long userId = 1L;
        final List<UserPaymentDto> emptyList = Collections.emptyList();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.paymentClient.getPaymentsByUser(userId, "Basic dGVzdDp0ZXN0")).thenReturn(emptyList);

        // When
        final List<UserPaymentDto> result = this.userPaymentService.getPaymentsByUser(userId);

        // Then
        assertThat(result).isEmpty();
    }
}
