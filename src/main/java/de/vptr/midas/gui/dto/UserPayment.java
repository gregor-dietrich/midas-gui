package de.vptr.midas.gui.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserPayment {

    @JsonProperty("id")
    public Long id;

    @JsonProperty("userId")
    public Long userId;

    @JsonProperty("sourceId")
    public Long sourceId;

    @JsonProperty("targetId")
    public Long targetId;

    @JsonProperty("amount")
    public BigDecimal amount;

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate date;

    @JsonProperty("comment")
    public String comment;

    @JsonProperty("created")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime created;

    @JsonProperty("lastEdit")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime lastEdit;

    public UserPayment() {
        // Default constructor for Jackson
    }

    public UserPayment(final Long userId, final Long sourceId, final Long targetId, final BigDecimal amount,
            final LocalDate date, final String comment, final LocalDateTime created, final LocalDateTime lastEdit) {
        this.userId = userId;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.amount = amount;
        this.date = date;
        this.comment = comment;
        this.created = created;
        this.lastEdit = lastEdit;
    }

    public Long getId() {
        return this.id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public Long getSourceId() {
        return this.sourceId;
    }

    public Long getTargetId() {
        return this.targetId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public String getComment() {
        return this.comment;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public LocalDateTime getLastEdit() {
        return this.lastEdit;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    public void setSourceId(final Long sourceAccountId) {
        this.sourceId = sourceAccountId;
    }

    public void setTargetId(final Long targetAccountId) {
        this.targetId = targetAccountId;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public void setDate(final LocalDate paymentDate) {
        this.date = paymentDate;
    }

    public void setComment(final String description) {
        this.comment = description;
    }

    public void setCreated(final LocalDateTime created) {
        this.created = created;
    }

    public void setLastEdit(final LocalDateTime lastEdit) {
        this.lastEdit = lastEdit;
    }

    @Override
    public String toString() {
        return "UserPayment{" +
                "id=" + this.id +
                ", userId=" + this.userId +
                ", sourceId=" + this.sourceId +
                ", targetId=" + this.targetId +
                ", amount=" + this.amount +
                ", date=" + this.date +
                ", comment='" + this.comment + '\'' +
                ", created=" + this.created +
                ", lastEdit=" + this.lastEdit +
                '}';
    }
}
