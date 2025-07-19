package de.vptr.midas.gui.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PostDto {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("title")
    public String title;

    @JsonProperty("content")
    public String content;

    @JsonProperty("user")
    public UserDto user;

    @JsonProperty("category")
    public PostCategoryDto category;

    @JsonProperty("published")
    public Boolean published;

    @JsonProperty("commentable")
    public Boolean commentable;

    @JsonProperty("created")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]")
    public LocalDateTime created;

    @JsonProperty("lastEdit")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]")
    public LocalDateTime lastEdit;

    public PostDto() {
        // Default constructor for Jackson
    }

    public PostDto(final Long id, final String title, final String content, final UserDto user,
            final PostCategoryDto category,
            final Boolean published, final Boolean commentable, final LocalDateTime created,
            final LocalDateTime lastEdit) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.user = user;
        this.category = category;
        this.published = published;
        this.commentable = commentable;
        this.created = created;
        this.lastEdit = lastEdit;
    }

    // Getters and setters
    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public UserDto getUser() {
        return this.user;
    }

    public void setUser(final UserDto user) {
        this.user = user;
    }

    public PostCategoryDto getCategory() {
        return this.category;
    }

    public void setCategory(final PostCategoryDto category) {
        this.category = category;
    }

    public Boolean getPublished() {
        return this.published;
    }

    public void setPublished(final Boolean published) {
        this.published = published;
    }

    public Boolean getCommentable() {
        return this.commentable;
    }

    public void setCommentable(final Boolean commentable) {
        this.commentable = commentable;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public void setCreated(final LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getLastEdit() {
        return this.lastEdit;
    }

    public void setLastEdit(final LocalDateTime lastEdit) {
        this.lastEdit = lastEdit;
    }

    @Override
    public String toString() {
        return "PostDto{" +
                "id=" + this.id +
                ", title='" + this.title + '\'' +
                ", content='" + this.content + '\'' +
                ", user=" + this.user +
                ", category=" + this.category +
                ", published=" + this.published +
                ", commentable=" + this.commentable +
                ", created=" + this.created +
                ", lastEdit=" + this.lastEdit +
                '}';
    }
}
