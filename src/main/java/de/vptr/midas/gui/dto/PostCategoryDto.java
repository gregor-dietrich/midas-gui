package de.vptr.midas.gui.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostCategoryDto {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("parent")
    public PostCategoryDto parent;

    @JsonProperty("children")
    public List<PostCategoryDto> children;

    public PostCategoryDto() {
        // Default constructor for Jackson
    }

    public PostCategoryDto(final Long id, final String name, final PostCategoryDto parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
    }

    // Getters and setters
    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public PostCategoryDto getParent() {
        return this.parent;
    }

    public void setParent(final PostCategoryDto parent) {
        this.parent = parent;
    }

    public List<PostCategoryDto> getChildren() {
        return this.children;
    }

    public void setChildren(final List<PostCategoryDto> children) {
        this.children = children;
    }

    public boolean isRootCategory() {
        return this.parent == null;
    }

    @Override
    public String toString() {
        return "PostCategoryDto{" +
                "id=" + this.id +
                ", name='" + this.name + '\'' +
                ", parent=" + (this.parent != null ? this.parent.id : null) +
                '}';
    }
}
