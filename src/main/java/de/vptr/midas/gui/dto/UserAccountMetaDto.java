package de.vptr.midas.gui.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserAccountMetaDto {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("user")
    public UserDto user;

    @JsonProperty("account")
    public UserAccountDto account;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]")
    public LocalDateTime timestamp;

    public UserAccountMetaDto() {
        // Default constructor for Jackson
    }

    public UserAccountMetaDto(final UserDto user, final UserAccountDto account, final LocalDateTime timestamp) {
        this.user = user;
        this.account = account;
        this.timestamp = timestamp;
    }

    // Getter methods
    public Long getId() {
        return this.id;
    }

    public UserDto getUser() {
        return this.user;
    }

    public UserAccountDto getAccount() {
        return this.account;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    // Setter methods
    public void setId(final Long id) {
        this.id = id;
    }

    public void setUser(final UserDto user) {
        this.user = user;
    }

    public void setAccount(final UserAccountDto account) {
        this.account = account;
    }

    public void setTimestamp(final LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Convenience methods to get IDs (useful for forms and grids)
    public Long getUserId() {
        return this.user != null ? this.user.getId() : null;
    }

    public Long getAccountId() {
        return this.account != null ? this.account.getId() : null;
    }

    public String getUserName() {
        return this.user != null ? this.user.getUsername() : null;
    }

    public String getAccountName() {
        return this.account != null ? this.account.getName() : null;
    }

    @Override
    public String toString() {
        return "UserAccountMeta{" +
                "id=" + this.id +
                ", user=" + (this.user != null ? this.user.getUsername() : "null") +
                ", account=" + (this.account != null ? this.account.getName() : "null") +
                ", timestamp=" + this.timestamp +
                '}';
    }
}
