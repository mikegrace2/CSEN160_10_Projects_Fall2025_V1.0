package _5000.dto;

public class RecommendedProductResponse {
    private String productId;
    private String productName;
    private String suggestedVariant;
    private String reason;

    public RecommendedProductResponse() {
    }

    public RecommendedProductResponse(String productId, String productName, String suggestedVariant, String reason) {
        this.productId = productId;
        this.productName = productName;
        this.suggestedVariant = suggestedVariant;
        this.reason = reason;
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

    public String getSuggestedVariant() {
        return suggestedVariant;
    }

    public void setSuggestedVariant(String suggestedVariant) {
        this.suggestedVariant = suggestedVariant;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

