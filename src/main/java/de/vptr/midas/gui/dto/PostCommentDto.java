package de.vptr.midas.gui.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PostCommentDto {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("content")
    public String content;

    @JsonProperty("post")
    public PostDto post;

    @JsonProperty("user")
    public UserDto user;

    @JsonProperty("created")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]")
    public LocalDateTime created;

    public PostCommentDto() {
        // Default constructor for Jackson
    }

    public PostCommentDto(final Long id, final String content, final PostDto post, final UserDto user,
            final LocalDateTime created) {
        this.id = id;
        this.content = content;
        this.post = post;
        this.user = user;
        this.created = created;
    }

    // Getters and setters
    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public PostDto getPost() {
        return this.post;
    }

    public void setPost(final PostDto post) {
        this.post = post;
    }

    public UserDto getUser() {
        return this.user;
    }

    public void setUser(final UserDto user) {
        this.user = user;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public void setCreated(final LocalDateTime created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "PostCommentDto{" +
                "id=" + this.id +
                ", content='" + this.content + '\'' +
                ", post=" + (this.post != null ? this.post.id : null) +
                ", user=" + (this.user != null ? this.user.username : null) +
                ", created=" + this.created +
                '}';
    }
}
