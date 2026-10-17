package _5000.dto;

import _5000.model.OrderItem;

public class OrderItemResponse {
    private String productId;
    private String productName;
    private Integer quantity;
    private Double price;

    public OrderItemResponse() {
    }

    public OrderItemResponse(OrderItem item) {
        this.productId = item.getProductId();
        this.productName = item.getProductName();
        this.quantity = item.getQuantity();
        this.price = item.getPrice();
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}

