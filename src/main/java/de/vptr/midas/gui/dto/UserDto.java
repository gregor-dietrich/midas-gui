package de.vptr.midas.gui.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDto {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("username")
    public String username;

    @JsonProperty("password")
    public String password;

    @JsonProperty("salt")
    public String salt;

    @JsonProperty("rank")
    public UserRankDto rank;

    @JsonProperty("email")
    public String email;

    @JsonProperty("banned")
    public Boolean banned;

    @JsonProperty("activated")
    public Boolean activated;

    @JsonProperty("activationKey")
    public String activationKey;

    @JsonProperty("lastIp")
    public String lastIp;

    @JsonProperty("created")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]")
    public LocalDateTime created;

    @JsonProperty("lastLogin")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]")
    public LocalDateTime lastLogin;

    public UserDto() {
        // Default constructor for Jackson
    }

    public UserDto(final String username, final String email) {
        this.username = username;
        this.email = email;
    }

    // Getter methods
    public Long getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getSalt() {
        return this.salt;
    }

    public UserRankDto getRank() {
        return this.rank;
    }

    public String getEmail() {
        return this.email;
    }

    public Boolean getBanned() {
        return this.banned;
    }

    public Boolean getActivated() {
        return this.activated;
    }

    public String getActivationKey() {
        return this.activationKey;
    }

    public String getLastIp() {
        return this.lastIp;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public LocalDateTime getLastLogin() {
        return this.lastLogin;
    }

    // Setter methods
    public void setId(final Long id) {
        this.id = id;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setSalt(final String salt) {
        this.salt = salt;
    }

    public void setRank(final UserRankDto rank) {
        this.rank = rank;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setBanned(final Boolean banned) {
        this.banned = banned;
    }

    public void setActivated(final Boolean activated) {
        this.activated = activated;
    }

    public void setActivationKey(final String activationKey) {
        this.activationKey = activationKey;
    }

    public void setLastIp(final String lastIp) {
        this.lastIp = lastIp;
    }

    public void setCreated(final LocalDateTime created) {
        this.created = created;
    }

    public void setLastLogin(final LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    // Convenience methods
    public boolean isActivated() {
        return this.activated != null && this.activated;
    }

    public boolean isBanned() {
        return this.banned != null && this.banned;
    }

    public String getRankName() {
        return this.rank != null ? this.rank.getName() : null;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + this.id +
                ", username='" + this.username + '\'' +
                ", email='" + this.email + '\'' +
                ", banned=" + this.banned +
                ", activated=" + this.activated +
                ", rank=" + (this.rank != null ? this.rank.getName() : "null") +
                ", created=" + this.created +
                ", lastLogin=" + this.lastLogin +
                '}';
    }
}
