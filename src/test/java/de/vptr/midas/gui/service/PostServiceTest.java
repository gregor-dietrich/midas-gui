package de.vptr.midas.gui.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.vptr.midas.gui.client.PostClient;
import de.vptr.midas.gui.dto.PostDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @RestClient
    @Inject
    @Mock
    PostClient postClient;

    @Mock
    AuthService authService;

    @InjectMocks
    PostService postService;

    @Test
    void getAllPosts_shouldReturnPosts_whenAuthHeaderProvided() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final List<PostDto> expectedPosts = Arrays.asList(new PostDto(), new PostDto());
        when(this.postClient.getAllPosts(authHeader)).thenReturn(expectedPosts);

        // When
        final List<PostDto> result = this.postService.getAllPosts(authHeader);

        // Then
        assertThat(result).isEqualTo(expectedPosts);
        verify(this.postClient).getAllPosts(authHeader);
    }

    @Test
    void getAllPosts_shouldThrowAuthenticationException_whenAuthHeaderIsNull() {
        // When & Then
        assertThatThrownBy(() -> this.postService.getAllPosts(null))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Authentication required");
    }

    @Test
    void getAllPosts_shouldThrowServiceException_whenProcessingException() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.postClient.getAllPosts(authHeader)).thenThrow(new ProcessingException("Connection failed"));

        // When & Then
        assertThatThrownBy(() -> this.postService.getAllPosts(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backend connection failed");
    }

    @Test
    void getAllPosts_shouldThrowAuthenticationException_when401() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final WebApplicationException webAppException = new WebApplicationException(401);
        when(this.postClient.getAllPosts(authHeader)).thenThrow(webAppException);

        // When & Then
        assertThatThrownBy(() -> this.postService.getAllPosts(authHeader))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session expired");
        verify(this.authService).logout();
    }

    @Test
    void getPublishedPosts_shouldReturnPosts_whenAuthenticated() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final List<PostDto> expectedPosts = Arrays.asList(new PostDto(), new PostDto());
        when(this.authService.getBasicAuthHeader()).thenReturn(authHeader);
        when(this.postClient.getPublishedPosts(authHeader)).thenReturn(expectedPosts);

        // When
        final List<PostDto> result = this.postService.getPublishedPosts();

        // Then
        assertThat(result).isEqualTo(expectedPosts);
        verify(this.postClient).getPublishedPosts(authHeader);
    }

    @Test
    void getPublishedPosts_shouldReturnEmptyList_whenNotAuthenticated() {
        // Given
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When
        final List<PostDto> result = this.postService.getPublishedPosts();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getPostById_shouldReturnPost_whenPostExists() {
        // Given
        final Long postId = 1L;
        final PostDto expectedPost = new PostDto();
        final Response response = mock(Response.class);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.postClient.getPost(postId, "Basic dGVzdDp0ZXN0")).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(PostDto.class)).thenReturn(expectedPost);

        // When
        final Optional<PostDto> result = this.postService.getPostById(postId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedPost);
    }

    @Test
    void getPostById_shouldReturnEmpty_whenNotAuthenticated() {
        // Given
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When
        final Optional<PostDto> result = this.postService.getPostById(1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getPostById_shouldReturnEmpty_whenPostNotFound() {
        // Given
        final Long postId = 999L;
        final Response response = mock(Response.class);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.postClient.getPost(postId, "Basic dGVzdDp0ZXN0")).thenReturn(response);
        when(response.getStatus()).thenReturn(404);

        // When
        final Optional<PostDto> result = this.postService.getPostById(postId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getPostsByUser_shouldReturnPosts_whenAuthenticated() {
        // Given
        final Long userId = 1L;
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final List<PostDto> expectedPosts = Arrays.asList(new PostDto(), new PostDto());
        when(this.authService.getBasicAuthHeader()).thenReturn(authHeader);
        when(this.postClient.getPostsByUser(userId, authHeader)).thenReturn(expectedPosts);

        // When
        final List<PostDto> result = this.postService.getPostsByUser(userId);

        // Then
        assertThat(result).isEqualTo(expectedPosts);
    }

    @Test
    void getPostsByUser_shouldReturnEmptyList_whenNotAuthenticated() {
        // Given
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When
        final List<PostDto> result = this.postService.getPostsByUser(1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getPostsByCategory_shouldReturnPosts_whenAuthenticated() {
        // Given
        final Long categoryId = 1L;
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final List<PostDto> expectedPosts = Arrays.asList(new PostDto(), new PostDto());
        when(this.authService.getBasicAuthHeader()).thenReturn(authHeader);
        when(this.postClient.getPostsByCategory(categoryId, authHeader)).thenReturn(expectedPosts);

        // When
        final List<PostDto> result = this.postService.getPostsByCategory(categoryId);

        // Then
        assertThat(result).isEqualTo(expectedPosts);
    }

    @Test
    void getPostsByCategory_shouldReturnEmptyList_whenNotAuthenticated() {
        // Given
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When
        final List<PostDto> result = this.postService.getPostsByCategory(1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void createPost_shouldReturnCreatedPost_whenValid() {
        // Given
        final PostDto newPost = new PostDto();
        final PostDto createdPost = new PostDto();
        final Response response = mock(Response.class);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.postClient.createPost(newPost, "Basic dGVzdDp0ZXN0")).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.readEntity(PostDto.class)).thenReturn(createdPost);

        // When
        final PostDto result = this.postService.createPost(newPost);

        // Then
        assertThat(result).isEqualTo(createdPost);
        verify(this.postClient).createPost(newPost, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void createPost_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final PostDto newPost = new PostDto();
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> this.postService.createPost(newPost))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Not authenticated");
    }

    @Test
    void createPost_shouldThrowServiceException_whenCreationFails() {
        // Given
        final PostDto newPost = new PostDto();
        final Response response = mock(Response.class);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.postClient.createPost(newPost, "Basic dGVzdDp0ZXN0")).thenReturn(response);
        when(response.getStatus()).thenReturn(400);

        // When & Then
        assertThatThrownBy(() -> this.postService.createPost(newPost))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Failed to create post");
    }

    @Test
    void updatePost_shouldReturnUpdatedPost_whenValid() {
        // Given
        final PostDto postToUpdate = new PostDto();
        postToUpdate.id = 1L;
        final PostDto updatedPost = new PostDto();
        final Response response = mock(Response.class);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.postClient.updatePost(postToUpdate.id, postToUpdate, "Basic dGVzdDp0ZXN0")).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(PostDto.class)).thenReturn(updatedPost);

        // When
        final PostDto result = this.postService.updatePost(postToUpdate);

        // Then
        assertThat(result).isEqualTo(updatedPost);
        verify(this.postClient).updatePost(postToUpdate.id, postToUpdate, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void updatePost_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final PostDto postToUpdate = new PostDto();
        when(this.authService.getBasicAuthHeader()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> this.postService.updatePost(postToUpdate))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Not authenticated");
    }

    @Test
    void updatePost_shouldThrowServiceException_whenUpdateFails() {
        // Given
        final PostDto postToUpdate = new PostDto();
        postToUpdate.id = 1L;
        final Response response = mock(Response.class);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.postClient.updatePost(postToUpdate.id, postToUpdate, "Basic dGVzdDp0ZXN0")).thenReturn(response);
        when(response.getStatus()).thenReturn(400);

        // When & Then
        assertThatThrownBy(() -> this.postService.updatePost(postToUpdate))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Failed to update post");
    }

    @Test
    void deletePost_shouldReturnTrue_whenDeletionSuccessful() {
        // Given
        final Long postId = 1L;
        final Response response = mock(Response.class);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.postClient.deletePost(postId, "Basic dGVzdDp0ZXN0")).thenReturn(response);
        when(response.getStatus()).thenReturn(204);

        // When
        final boolean result = this.postService.deletePost(postId);

        // Then
        assertThat(result).isTrue();
        verify(this.postClient).deletePost(postId, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void deletePost_shouldReturnFalse_whenDeletionFails() {
        // Given
        final Long postId = 1L;
        final Response response = mock(Response.class);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.postClient.deletePost(postId, "Basic dGVzdDp0ZXN0")).thenReturn(response);
        when(response.getStatus()).thenReturn(400);

        // When
        final boolean result = this.postService.deletePost(postId);

        // Then
        assertThat(result).isFalse();
    }
}
