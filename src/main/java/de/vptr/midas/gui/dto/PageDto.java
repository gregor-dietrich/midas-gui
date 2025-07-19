package de.vptr.midas.gui.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PageDto {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("title")
    public String title;

    @JsonProperty("content")
    public String content;

    @JsonProperty("slug")
    public String slug;

    @JsonProperty("published")
    public Boolean published;

    @JsonProperty("created")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime created;

    @JsonProperty("lastEdit")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime lastEdit;

    public PageDto() {
        // Default constructor for Jackson
    }

    public PageDto(final Long id, final String title, final String content, final String slug, final Boolean published,
            final LocalDateTime created, final LocalDateTime lastEdit) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.slug = slug;
        this.published = published;
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

    public String getSlug() {
        return this.slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public Boolean getPublished() {
        return this.published;
    }

    public void setPublished(final Boolean published) {
        this.published = published;
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
        return "PageDto{" +
                "id=" + this.id +
                ", title='" + this.title + '\'' +
                ", slug='" + this.slug + '\'' +
                ", published=" + this.published +
                ", created=" + this.created +
                ", lastEdit=" + this.lastEdit +
                '}';
    }
}
