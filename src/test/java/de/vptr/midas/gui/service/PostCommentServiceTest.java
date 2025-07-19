package de.vptr.midas.gui.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.vptr.midas.gui.client.PostCommentClient;
import de.vptr.midas.gui.dto.PostCommentDto;
import de.vptr.midas.gui.dto.PostDto;
import de.vptr.midas.gui.dto.UserDto;
import de.vptr.midas.gui.exception.AuthenticationException;
import de.vptr.midas.gui.exception.ServiceException;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {

    @RestClient
    @Inject
    @Mock
    PostCommentClient commentClient;

    @Mock
    AuthService authService;

    @InjectMocks
    PostCommentService postCommentService;

    @Test
    void getAllComments_shouldReturnCommentList_whenAuthenticationProvided() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final UserDto user = new UserDto("testuser", "test@example.com");
        user.id = 1L;
        final PostDto post = new PostDto();
        post.id = 1L;
        post.title = "Test Post";

        final PostCommentDto comment1 = new PostCommentDto(1L, "First comment", post, user, LocalDateTime.now());
        final PostCommentDto comment2 = new PostCommentDto(2L, "Second comment", post, user, LocalDateTime.now());
        final List<PostCommentDto> expectedComments = Arrays.asList(comment1, comment2);

        when(this.commentClient.getAllComments(authHeader)).thenReturn(expectedComments);

        // When
        final List<PostCommentDto> result = this.postCommentService.getAllComments(authHeader);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(comment1, comment2);
        verify(this.commentClient).getAllComments(authHeader);
    }

    @Test
    void getAllComments_shouldThrowServiceException_whenAuthHeaderIsNull() {
        // When & Then
        assertThatThrownBy(() -> this.postCommentService.getAllComments(null))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }

    @Test
    void getAllComments_shouldThrowServiceException_whenProcessingExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.commentClient.getAllComments(authHeader))
                .thenThrow(new ProcessingException("Connection failed"));

        // When & Then
        assertThatThrownBy(() -> this.postCommentService.getAllComments(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Backend connection failed");
    }

    @Test
    void getAllComments_shouldThrowAuthenticationException_when401Error() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        final Response mockResponse = Response.status(401).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.commentClient.getAllComments(authHeader)).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.postCommentService.getAllComments(authHeader))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session expired");

        verify(this.authService).logout();
    }

    @Test
    void getCommentsByPost_shouldReturnComments_whenPostExists() {
        // Given
        final Long postId = 1L;
        final UserDto user = new UserDto("testuser", "test@example.com");
        user.id = 1L;
        final PostDto post = new PostDto();
        post.id = postId;

        final PostCommentDto comment = new PostCommentDto(1L, "Test comment", post, user, LocalDateTime.now());
        final List<PostCommentDto> expectedComments = Collections.singletonList(comment);

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.commentClient.getCommentsByPost(postId, "Basic dGVzdDp0ZXN0")).thenReturn(expectedComments);

        // When
        final List<PostCommentDto> result = this.postCommentService.getCommentsByPost(postId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).content).isEqualTo("Test comment");
        verify(this.commentClient).getCommentsByPost(postId, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void getCommentsByPost_shouldThrowAuthenticationException_whenNotAuthenticated() {
        // Given
        final Long postId = 1L;
        when(this.authService.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> this.postCommentService.getCommentsByPost(postId))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("User is not authenticated");
    }

    @Test
    void getCommentById_shouldReturnComment_whenCommentExists() {
        // Given
        final Long commentId = 1L;
        final UserDto user = new UserDto("testuser", "test@example.com");
        final PostDto post = new PostDto();
        post.id = 1L;
        final PostCommentDto expectedComment = new PostCommentDto(commentId, "Test comment", post, user,
                LocalDateTime.now());
        final Response mockResponse = Response.status(200).entity(expectedComment).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.commentClient.getComment(commentId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final PostCommentDto result = this.postCommentService.getCommentById(commentId);

        // Then
        assertThat(result.id).isEqualTo(commentId);
        assertThat(result.content).isEqualTo("Test comment");
    }

    @Test
    void getCommentById_shouldThrowServiceException_whenCommentNotFound() {
        // Given
        final Long commentId = 999L;
        final Response mockResponse = Response.status(404).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.commentClient.getComment(commentId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> this.postCommentService.getCommentById(commentId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error occurred");
    }

    @Test
    void createComment_shouldReturnCreatedComment_whenValidComment() {
        // Given
        final UserDto user = new UserDto("testuser", "test@example.com");
        final PostDto post = new PostDto();
        post.id = 1L;
        final PostCommentDto newComment = new PostCommentDto(null, "New comment", post, user, null);
        final PostCommentDto createdComment = new PostCommentDto(1L, "New comment", post, user, LocalDateTime.now());
        final Response mockResponse = Response.status(201).entity(createdComment).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.commentClient.createComment(newComment, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final PostCommentDto result = this.postCommentService.createComment(newComment);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.content).isEqualTo("New comment");
        verify(this.commentClient).createComment(newComment, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void createComment_shouldThrowServiceException_whenCreationFails() {
        // Given
        final PostCommentDto newComment = new PostCommentDto();
        newComment.content = "New comment";
        final Response mockResponse = Response.status(400).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.commentClient.createComment(newComment, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> this.postCommentService.createComment(newComment))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error occurred");
    }

    @Test
    void updateComment_shouldReturnUpdatedComment_whenValidComment() {
        // Given
        final UserDto user = new UserDto("testuser", "test@example.com");
        final PostDto post = new PostDto();
        post.id = 1L;
        final PostCommentDto commentToUpdate = new PostCommentDto(1L, "Updated comment", post, user,
                LocalDateTime.now());
        final Response mockResponse = Response.status(200).entity(commentToUpdate).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.commentClient.updateComment(1L, commentToUpdate, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final PostCommentDto result = this.postCommentService.updateComment(commentToUpdate);

        // Then
        assertThat(result.id).isEqualTo(1L);
        assertThat(result.content).isEqualTo("Updated comment");
        verify(this.commentClient).updateComment(1L, commentToUpdate, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void deleteComment_shouldReturnTrue_whenDeletionSuccessful() {
        // Given
        final Long commentId = 1L;
        final Response mockResponse = Response.status(204).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.commentClient.deleteComment(commentId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.postCommentService.deleteComment(commentId);

        // Then
        assertThat(result).isTrue();
        verify(this.commentClient).deleteComment(commentId, "Basic dGVzdDp0ZXN0");
    }

    @Test
    void deleteComment_shouldReturnFalse_whenDeletionFails() {
        // Given
        final Long commentId = 1L;
        final Response mockResponse = Response.status(400).build();

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.commentClient.deleteComment(commentId, "Basic dGVzdDp0ZXN0")).thenReturn(mockResponse);

        // When
        final boolean result = this.postCommentService.deleteComment(commentId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getCommentsByPost_shouldThrowServiceException_whenWebApplicationExceptionOccurs() {
        // Given
        final Long postId = 1L;
        final Response mockResponse = Response.status(500).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.commentClient.getCommentsByPost(postId, "Basic dGVzdDp0ZXN0")).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.postCommentService.getCommentsByPost(postId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Failed to get comments: HTTP 500 Internal Server Error");
    }

    @Test
    void getCommentsByPost_shouldThrowAuthenticationException_when401ErrorOnFetch() {
        // Given
        final Long postId = 1L;
        final Response mockResponse = Response.status(401).build();
        final WebApplicationException webException = new WebApplicationException(mockResponse);

        when(this.authService.isAuthenticated()).thenReturn(true);
        when(this.authService.getBasicAuthHeader()).thenReturn("Basic dGVzdDp0ZXN0");
        when(this.commentClient.getCommentsByPost(postId, "Basic dGVzdDp0ZXN0")).thenThrow(webException);

        // When & Then
        assertThatThrownBy(() -> this.postCommentService.getCommentsByPost(postId))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Session expired");
    }

    @Test
    void getAllComments_shouldThrowServiceException_whenUnexpectedExceptionOccurs() {
        // Given
        final String authHeader = "Basic dGVzdDp0ZXN0";
        when(this.commentClient.getAllComments(authHeader))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThatThrownBy(() -> this.postCommentService.getAllComments(authHeader))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Unexpected error");
    }
}
