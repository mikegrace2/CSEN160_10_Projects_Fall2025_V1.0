package _5000.dto;

import _5000.model.OrderStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

public class OrderCreatedEvent implements Serializable {
    private Long orderId;
    private Long userId;
    private String username;
    private Double totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(Long orderId, Long userId, String username, Double totalAmount, OrderStatus status, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.username = username;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

