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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.vptr.midas.gui.client.PostCategoryClient;
import de.vptr.midas.gui.dto.PostCategoryDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class PostCategoryServiceTest {

    @RestClient
    @Inject
    @Mock
    PostCategoryClient categoryClient;

    @Mock
    AuthService authService;

    @InjectMocks
    PostCategoryService postCategoryService;

    @Test
    void getAllCategories_shouldReturnCategoryList_whenAuthenticationProvided() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final PostCategoryDto category1 = new PostCategoryDto(1L, "Category 1", null);
        final PostCategoryDto category2 = new PostCategoryDto(2L, "Category 2", null);
        final List<PostCategoryDto> expectedCategories = Arrays.asList(category1, category2);

        when(this.categoryClient.getAllCategories(authHeader)).thenReturn(expectedCategories);

        // When
        final List<PostCategoryDto> result = this.postCategoryService.getAllCategories(authHeader);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(category1, category2);
        verify(this.categoryClient).getAllCategories(authHeader);
    }

    @Test
    @DisplayName("Should return empty list when auth header is null")
    void getAllCategories_shouldReturnEmptyList_whenAuthHeaderIsNull() {
        // When
        final List<PostCategoryDto> categories = this.postCategoryService.getAllCategories(null);

        // Then
        assertThat(categories).isEmpty();
    }

    @Test
    void getAllCategories_shouldThrowServiceException_whenProcessingExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.categoryClient.getAllCategories(authHeader))
                .thenThrow(new ProcessingException("Connection failed"));

        // When & Then
        assertThatThrownBy(() -> this.postCategoryService.getAllCategories(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backend connection failed");
    }

    @Test
    void getAllCategories_shouldThrowAuthenticationException_when401Error() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final Response mockResponse = Response.status(401).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.categoryClient.getAllCategories(authHeader)).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.postCategoryService.getAllCategories(authHeader))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session expired");

        verify(this.authService).logout();
    }

    @Test
    void getRootCategories_shouldReturnRootCategories_whenAuthenticated() {
        // Given
        final PostCategoryDto rootCategory = new PostCategoryDto(1L, "Root Category", null);
        final List<PostCategoryDto> expectedCategories = Collections.singletonList(rootCategory);

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.categoryClient.getRootCategories("Basic dGVzdDp0ZXN0")).thenReturn(expectedCategories);

        // When
        final List<PostCategoryDto> result = this.postCategoryService.getRootCategories();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name).isEqualTo("Root Category");
        verify(this.categoryClient).getRootCategories("Basic dGVzdDp0ZXN0");
    }

    @Test
    void getCategoryById_shouldReturnCategory_whenCategoryExists() {
        // Given
        final Long categoryId = 1L;
        final PostCategoryDto expectedCategory = new PostCategoryDto(categoryId, "Test Category", null);
        final Response mockResponse = Response.status(200).entity(expectedCategory).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.categoryClient.getCategory(categoryId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<PostCategoryDto> result = this.postCategoryService.getCategoryById(categoryId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id).isEqualTo(categoryId);
        assertThat(result.get().name).isEqualTo("Test Category");
    }

    @Test
    void getCategoryById_shouldReturnEmpty_whenCategoryNotFound() {
        // Given
        final Long categoryId = 999L;
        final Response mockResponse = Response.status(404).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.categoryClient.getCategory(categoryId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final Optional<PostCategoryDto> result = this.postCategoryService.getCategoryById(categoryId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getCategoriesByParent_shouldReturnChildCategories_whenParentExists() {
        // Given
        final Long parentId = 1L;
        final PostCategoryDto parent = new PostCategoryDto(parentId, "Parent", null);
        final PostCategoryDto child1 = new PostCategoryDto(2L, "Child 1", parent);
        final PostCategoryDto child2 = new PostCategoryDto(3L, "Child 2", parent);
        final List<PostCategoryDto> expectedChildren = Arrays.asList(child1, child2);

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.categoryClient.getCategoriesByParent(parentId, "Basic dGVzdDp0ZXN0")).thenReturn(expectedChildren);

        // When
        final List<PostCategoryDto> result = this.postCategoryService.getCategoriesByParent(parentId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(child1, child2);
    }

    @Test
    void createCategory_shouldReturnCreatedCategory_whenValidCategory() {
        // Given
        final PostCategoryDto newCategory = new PostCategoryDto(null, "New Category", null);
        final PostCategoryDto createdCategory = new PostCategoryDto(1L, "New Category", null);
        final Response mockResponse = Response.status(201).entity(createdCategory).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.categoryClient.createCategory(newCategory, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final PostCategoryDto result = this.postCategoryService.createCategory(newCategory);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.name).isEqualTo("New Category");
        verify(this.categoryClient).createCategory(newCategory, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void createCategory_shouldThrowServiceException_whenCreationFails() {
        // Given
        final PostCategoryDto newCategory = new PostCategoryDto(null, "New Category", null);
        final Response mockResponse = Response.status(400).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.categoryClient.createCategory(newCategory, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> this.postCategoryService.createCategory(newCategory))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void updateCategory_shouldReturnUpdatedCategory_whenValidCategory() {
        // Given
        final PostCategoryDto categoryToUpdate = new PostCategoryDto(1L, "Updated Category", null);
        final Response mockResponse = Response.status(200).entity(categoryToUpdate).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.categoryClient.updateCategory(1L, categoryToUpdate, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final PostCategoryDto result = this.postCategoryService.updateCategory(categoryToUpdate);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.name).isEqualTo("Updated Category");
        verify(this.categoryClient).updateCategory(1L, categoryToUpdate, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void deleteCategory_shouldReturnTrue_whenDeletionSuccessful() {
        // Given
        final Long categoryId = 1L;
        final Response mockResponse = Response.status(204).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.categoryClient.deleteCategory(categoryId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.postCategoryService.deleteCategory(categoryId);

        // Then
        assertThat(result).isTrue();
        verify(this.categoryClient).deleteCategory(categoryId, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void deleteCategory_shouldReturnFalse_whenDeletionFails() {
        // Given
        final Long categoryId = 1L;
        final Response mockResponse = Response.status(400).build();

        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.categoryClient.deleteCategory(categoryId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.postCategoryService.deleteCategory(categoryId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getAllCategories_shouldThrowServiceException_whenUnexpectedExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.categoryClient.getAllCategories(authHeader))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThatThrownBy(() -> this.postCategoryService.getAllCategories(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }
}
