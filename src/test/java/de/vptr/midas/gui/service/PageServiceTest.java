package de.vptr.midas.gui.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.vptr.midas.gui.client.PageClient;
import de.vptr.midas.gui.dto.PageDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class PageServiceTest {

    @RestClient
    @Inject
    @Mock
    PageClient pageClient;

    @Mock
    AuthService authService;

    @InjectMocks
    PageService pageService;

    @Test
    void getAllPages_shouldReturnPages_whenAuthHeaderProvided() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final List<PageDto> expectedPages = List.of(new PageDto(), new PageDto());
        when(this.pageClient.getAllPages(authHeader)).thenReturn(expectedPages);

        // When
        final List<PageDto> result = this.pageService.getAllPages(authHeader);

        // Then
        assertThat(result).isEqualTo(expectedPages);
        verify(this.pageClient).getAllPages(authHeader);
    }

    @Test
    void getAllPages_shouldThrowServiceException_whenAuthHeaderIsNull() {
        // When & Then
        assertThatThrownBy(() -> this.pageService.getAllPages(null))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void getAllPages_shouldThrowServiceException_whenProcessingException() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.pageClient.getAllPages(authHeader)).thenThrow(new ProcessingException("Connection failed"));

        // When & Then
        assertThatThrownBy(() -> this.pageService.getAllPages(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backend connection failed");
    }

    @Test
    void getAllPages_shouldThrowAuthenticationException_when401() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final WebApplicationException webAppException = new WebApplicationException(401);
        when(this.pageClient.getAllPages(authHeader)).thenThrow(webAppException);

        // When & Then
        assertThatThrownBy(() -> this.pageService.getAllPages(authHeader))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session expired");
        verify(this.authService).logout();
    }

    @Test
    void getPageById_shouldReturnPage_whenSuccessful() {
        // Given
        final Long pageId = 1L;
        final PageDto expectedPage = new PageDto();
        final Response response = mock(Response.class);
        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.pageClient.getPage(pageId, "Basic dGVzdDp0ZXN0")).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(PageDto.class)).thenReturn(expectedPage);

        // When
        final PageDto result = this.pageService.getPageById(pageId);

        // Then
        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    void getPageById_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final Long pageId = 1L;
        when(this.authService.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> this.pageService.getPageById(pageId))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User is not authenticated");
    }

    @Test
    void createPage_shouldReturnCreatedPage_whenSuccessful() {
        // Given
        final PageDto page = new PageDto();
        final PageDto createdPage = new PageDto();
        final Response response = mock(Response.class);
        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.pageClient.createPage(eq(page), anyString())).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.readEntity(PageDto.class)).thenReturn(createdPage);

        // When
        final PageDto result = this.pageService.createPage(page);

        // Then
        assertThat(result).isEqualTo(createdPage);
    }

    @Test
    void createPage_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final PageDto page = new PageDto();
        when(this.authService.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> this.pageService.createPage(page))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User is not authenticated");
    }

    @Test
    void updatePage_shouldReturnUpdatedPage_whenSuccessful() {
        // Given
        final PageDto page = new PageDto();
        page.id = 1L;
        final PageDto updatedPage = new PageDto();
        final Response response = mock(Response.class);
        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.pageClient.updatePage(eq(page.id), eq(page), anyString())).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(PageDto.class)).thenReturn(updatedPage);

        // When
        final PageDto result = this.pageService.updatePage(page);

        // Then
        assertThat(result).isEqualTo(updatedPage);
    }

    @Test
    void updatePage_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final PageDto page = new PageDto();
        when(this.authService.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> this.pageService.updatePage(page))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User is not authenticated");
    }

    @Test
    void deletePage_shouldReturnTrue_whenSuccessful() {
        // Given
        final Long pageId = 1L;
        final Response response = mock(Response.class);
        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.pageClient.deletePage(eq(pageId), anyString())).thenReturn(response);
        when(response.getStatus()).thenReturn(204);

        // When
        final boolean result = this.pageService.deletePage(pageId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void deletePage_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final Long pageId = 1L;
        when(this.authService.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> this.pageService.deletePage(pageId))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User is not authenticated");
    }
}
