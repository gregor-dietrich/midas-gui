package de.vptr.midas.gui.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserAccount {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("userAccountMetas")
    public List<UserAccountMeta> userAccountMetas;

    @JsonProperty("outgoingPayments")
    public List<UserPayment> outgoingPayments;

    @JsonProperty("incomingPayments")
    public List<UserPayment> incomingPayments;

    public UserAccount() {
        // Default constructor for Jackson
    }

    public UserAccount(final String name) {
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public List<UserAccountMeta> getUserAccountMetas() {
        return this.userAccountMetas;
    }

    public List<UserPayment> getOutgoingPayments() {
        return this.outgoingPayments;
    }

    public List<UserPayment> getIncomingPayments() {
        return this.incomingPayments;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setUserAccountMetas(final List<UserAccountMeta> userAccountMetas) {
        this.userAccountMetas = userAccountMetas;
    }

    public void setOutgoingPayments(final List<UserPayment> outgoingPayments) {
        this.outgoingPayments = outgoingPayments;
    }

    public void setIncomingPayments(final List<UserPayment> incomingPayments) {
        this.incomingPayments = incomingPayments;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "id=" + this.id +
                ", name='" + this.name + '\'' +
                ", userAccountMetas=" + (this.userAccountMetas != null ? this.userAccountMetas.size() : 0) + " items" +
                ", outgoingPayments=" + (this.outgoingPayments != null ? this.outgoingPayments.size() : 0) + " items" +
                ", incomingPayments=" + (this.incomingPayments != null ? this.incomingPayments.size() : 0) + " items" +
                '}';
    }
}
