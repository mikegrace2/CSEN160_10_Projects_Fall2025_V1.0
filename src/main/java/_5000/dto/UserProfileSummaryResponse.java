package _5000.dto;

import java.util.List;

public class UserProfileSummaryResponse {
    private Long userId;
    private String username;
    private int totalOrders;
    private int totalItems;
    private Double lifetimeValue;
    private Double averageOrderValue;
    private String favoriteProduct;
    private String lastOrderDate;
    private String aiSummary;
    private List<String> insights;
    private List<RecommendedProductResponse> recommendations;

    public UserProfileSummaryResponse() {
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

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public Double getLifetimeValue() {
        return lifetimeValue;
    }

    public void setLifetimeValue(Double lifetimeValue) {
        this.lifetimeValue = lifetimeValue;
    }

    public Double getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(Double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public String getFavoriteProduct() {
        return favoriteProduct;
    }

    public void setFavoriteProduct(String favoriteProduct) {
        this.favoriteProduct = favoriteProduct;
    }

    public String getLastOrderDate() {
        return lastOrderDate;
    }

    public void setLastOrderDate(String lastOrderDate) {
        this.lastOrderDate = lastOrderDate;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public List<String> getInsights() {
        return insights;
    }

    public void setInsights(List<String> insights) {
        this.insights = insights;
    }

    public List<RecommendedProductResponse> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendedProductResponse> recommendations) {
        this.recommendations = recommendations;
    }
}

