package de.vptr.midas.gui.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserGroupDto {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("userCount")
    public Long userCount;

    @JsonProperty("users")
    public List<UserDto> users;

    @JsonProperty("created")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]")
    public LocalDateTime created;

    public UserGroupDto() {
        // Default constructor for Jackson
    }

    public UserGroupDto(final Long id, final String name) {
        this.id = id;
        this.name = name;
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

    public Long getUserCount() {
        return this.userCount;
    }

    public void setUserCount(final Long userCount) {
        this.userCount = userCount;
    }

    public List<UserDto> getUsers() {
        return this.users;
    }

    public void setUsers(final List<UserDto> users) {
        this.users = users;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public void setCreated(final LocalDateTime created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "UserGroupDto{" +
                "id=" + this.id +
                ", name='" + this.name + '\'' +
                ", userCount=" + this.userCount +
                ", created=" + this.created +
                '}';
    }
}
