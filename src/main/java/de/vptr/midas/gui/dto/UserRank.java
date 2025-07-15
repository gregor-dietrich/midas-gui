package de.vptr.midas.gui.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRank {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("name")
    public String name;

    // Post permissions
    @JsonProperty("postAdd")
    public Boolean postAdd = false;

    @JsonProperty("postDelete")
    public Boolean postDelete = false;

    @JsonProperty("postEdit")
    public Boolean postEdit = false;

    // Post category permissions
    @JsonProperty("postCategoryAdd")
    public Boolean postCategoryAdd = false;

    @JsonProperty("postCategoryDelete")
    public Boolean postCategoryDelete = false;

    @JsonProperty("postCategoryEdit")
    public Boolean postCategoryEdit = false;

    // Post comment permissions
    @JsonProperty("postCommentAdd")
    public Boolean postCommentAdd = false;

    @JsonProperty("postCommentDelete")
    public Boolean postCommentDelete = false;

    @JsonProperty("postCommentEdit")
    public Boolean postCommentEdit = false;

    // User permissions
    @JsonProperty("userAdd")
    public Boolean userAdd = false;

    @JsonProperty("userDelete")
    public Boolean userDelete = false;

    @JsonProperty("userEdit")
    public Boolean userEdit = false;

    // User group permissions
    @JsonProperty("userGroupAdd")
    public Boolean userGroupAdd = false;

    @JsonProperty("userGroupDelete")
    public Boolean userGroupDelete = false;

    @JsonProperty("userGroupEdit")
    public Boolean userGroupEdit = false;

    // User rank permissions
    @JsonProperty("userRankAdd")
    public Boolean userRankAdd = false;

    @JsonProperty("userRankDelete")
    public Boolean userRankDelete = false;

    @JsonProperty("userRankEdit")
    public Boolean userRankEdit = false;

    public UserRank() {
        // Default constructor for Jackson
    }

    public UserRank(final String name) {
        this.name = name;
    }

    // Getter methods
    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    // Post permissions getters
    public Boolean getPostAdd() {
        return this.postAdd;
    }

    public Boolean getPostDelete() {
        return this.postDelete;
    }

    public Boolean getPostEdit() {
        return this.postEdit;
    }

    // Post category permissions getters
    public Boolean getPostCategoryAdd() {
        return this.postCategoryAdd;
    }

    public Boolean getPostCategoryDelete() {
        return this.postCategoryDelete;
    }

    public Boolean getPostCategoryEdit() {
        return this.postCategoryEdit;
    }

    // Post comment permissions getters
    public Boolean getPostCommentAdd() {
        return this.postCommentAdd;
    }

    public Boolean getPostCommentDelete() {
        return this.postCommentDelete;
    }

    public Boolean getPostCommentEdit() {
        return this.postCommentEdit;
    }

    // User permissions getters
    public Boolean getUserAdd() {
        return this.userAdd;
    }

    public Boolean getUserDelete() {
        return this.userDelete;
    }

    public Boolean getUserEdit() {
        return this.userEdit;
    }

    // User group permissions getters
    public Boolean getUserGroupAdd() {
        return this.userGroupAdd;
    }

    public Boolean getUserGroupDelete() {
        return this.userGroupDelete;
    }

    public Boolean getUserGroupEdit() {
        return this.userGroupEdit;
    }

    // User rank permissions getters
    public Boolean getUserRankAdd() {
        return this.userRankAdd;
    }

    public Boolean getUserRankDelete() {
        return this.userRankDelete;
    }

    public Boolean getUserRankEdit() {
        return this.userRankEdit;
    }

    // Setter methods
    public void setId(final Long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // Post permissions setters
    public void setPostAdd(final Boolean postAdd) {
        this.postAdd = postAdd;
    }

    public void setPostDelete(final Boolean postDelete) {
        this.postDelete = postDelete;
    }

    public void setPostEdit(final Boolean postEdit) {
        this.postEdit = postEdit;
    }

    // Post category permissions setters
    public void setPostCategoryAdd(final Boolean postCategoryAdd) {
        this.postCategoryAdd = postCategoryAdd;
    }

    public void setPostCategoryDelete(final Boolean postCategoryDelete) {
        this.postCategoryDelete = postCategoryDelete;
    }

    public void setPostCategoryEdit(final Boolean postCategoryEdit) {
        this.postCategoryEdit = postCategoryEdit;
    }

    // Post comment permissions setters
    public void setPostCommentAdd(final Boolean postCommentAdd) {
        this.postCommentAdd = postCommentAdd;
    }

    public void setPostCommentDelete(final Boolean postCommentDelete) {
        this.postCommentDelete = postCommentDelete;
    }

    public void setPostCommentEdit(final Boolean postCommentEdit) {
        this.postCommentEdit = postCommentEdit;
    }

    // User permissions setters
    public void setUserAdd(final Boolean userAdd) {
        this.userAdd = userAdd;
    }

    public void setUserDelete(final Boolean userDelete) {
        this.userDelete = userDelete;
    }

    public void setUserEdit(final Boolean userEdit) {
        this.userEdit = userEdit;
    }

    // User group permissions setters
    public void setUserGroupAdd(final Boolean userGroupAdd) {
        this.userGroupAdd = userGroupAdd;
    }

    public void setUserGroupDelete(final Boolean userGroupDelete) {
        this.userGroupDelete = userGroupDelete;
    }

    public void setUserGroupEdit(final Boolean userGroupEdit) {
        this.userGroupEdit = userGroupEdit;
    }

    // User rank permissions setters
    public void setUserRankAdd(final Boolean userRankAdd) {
        this.userRankAdd = userRankAdd;
    }

    public void setUserRankDelete(final Boolean userRankDelete) {
        this.userRankDelete = userRankDelete;
    }

    public void setUserRankEdit(final Boolean userRankEdit) {
        this.userRankEdit = userRankEdit;
    }

    // Convenience methods for checking permissions
    public boolean canAddPost() {
        return this.postAdd != null && this.postAdd;
    }

    public boolean canDeletePost() {
        return this.postDelete != null && this.postDelete;
    }

    public boolean canEditPost() {
        return this.postEdit != null && this.postEdit;
    }

    public boolean canAddUser() {
        return this.userAdd != null && this.userAdd;
    }

    public boolean canDeleteUser() {
        return this.userDelete != null && this.userDelete;
    }

    public boolean canEditUser() {
        return this.userEdit != null && this.userEdit;
    }

    public boolean canManageUserRanks() {
        return (this.userRankAdd != null && this.userRankAdd) ||
                (this.userRankDelete != null && this.userRankDelete) ||
                (this.userRankEdit != null && this.userRankEdit);
    }

    public boolean hasAnyPermission() {
        return this.canAddPost() || this.canDeletePost() || this.canEditPost() ||
                this.canAddUser() || this.canDeleteUser() || this.canEditUser() ||
                this.canManageUserRanks();
    }

    @Override
    public String toString() {
        return "UserRank{" +
                "id=" + this.id +
                ", name='" + this.name + '\'' +
                ", postAdd=" + this.postAdd +
                ", postDelete=" + this.postDelete +
                ", postEdit=" + this.postEdit +
                ", userAdd=" + this.userAdd +
                ", userDelete=" + this.userDelete +
                ", userEdit=" + this.userEdit +
                '}';
    }
}
