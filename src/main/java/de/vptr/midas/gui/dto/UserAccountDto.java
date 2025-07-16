package de.vptr.midas.gui.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserAccountDto {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("name")
    public String name;

    public UserAccountDto() {
        // Default constructor for Jackson
    }

    public UserAccountDto(final String name) {
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "id=" + this.id +
                ", name='" + this.name + '\'' +
                '}';
    }
}
