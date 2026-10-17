package _5000.service;

import _5000.dto.RecommendedProductResponse;
import _5000.dto.UserProfileSummaryResponse;
import _5000.model.Order;
import _5000.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserInsightsService {

    @Autowired
    private OrderService orderService;

    private record ProductStats(String productId, String productName, int quantity, double spend) {
        ProductStats add(int qty, double price) {
            return new ProductStats(productId, productName, quantity + qty, spend + price);
        }
    }

    public UserProfileSummaryResponse buildUserProfileSummary(Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        if (orders.isEmpty()) {
            throw new RuntimeException("No order history found for user: " + userId);
        }

        String username = orders.get(0).getUsername();
        int totalOrders = orders.size();

        int totalItems = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .mapToInt(OrderItem::getQuantity)
                .sum();

        double lifetimeValue = orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        double averageOrderValue = totalOrders > 0 ? lifetimeValue / totalOrders : 0.0;

        Order latestOrder = orders.stream()
                .max(Comparator.comparing(Order::getCreatedAt))
                .orElse(null);

        String lastOrderDate = latestOrder != null
                ? latestOrder.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm", Locale.US))
                : "No orders yet";

        Map<String, ProductStats> productStats = new HashMap<>();
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                String key = item.getProductId() != null ? item.getProductId() : item.getProductName();
                ProductStats stats = productStats.getOrDefault(key,
                        new ProductStats(item.getProductId(), item.getProductName(), 0, 0.0));
                double spend = item.getPrice() * item.getQuantity();
                productStats.put(key, stats.add(item.getQuantity(), spend));
            }
        }

        LinkedHashMap<String, ProductStats> topProducts = productStats.entrySet().stream()
                .sorted((a, b) -> {
                    int quantityComparison = Integer.compare(b.getValue().quantity(), a.getValue().quantity());
                    if (quantityComparison != 0) {
                        return quantityComparison;
                    }
                    return Double.compare(b.getValue().spend(), a.getValue().spend());
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        ProductStats favoriteProduct = topProducts.values().stream().findFirst().orElse(null);

        List<String> insights = buildInsights(totalOrders, totalItems, lifetimeValue, averageOrderValue, favoriteProduct);
        List<RecommendedProductResponse> recommendations = buildRecommendations(topProducts);

        String aiSummary = buildSummary(username, totalOrders, lifetimeValue, favoriteProduct, lastOrderDate);

        UserProfileSummaryResponse response = new UserProfileSummaryResponse();
        response.setUserId(userId);
        response.setUsername(username);
        response.setTotalOrders(totalOrders);
        response.setTotalItems(totalItems);
        response.setLifetimeValue(round(lifetimeValue));
        response.setAverageOrderValue(round(averageOrderValue));
        response.setFavoriteProduct(favoriteProduct != null ? favoriteProduct.productName() : null);
        response.setLastOrderDate(lastOrderDate);
        response.setAiSummary(aiSummary);
        response.setInsights(insights);
        response.setRecommendations(recommendations);
        return response;
    }

    private List<String> buildInsights(int totalOrders, int totalItems, double lifetimeValue,
                                       double averageOrderValue, ProductStats favoriteProduct) {
        List<String> insights = new ArrayList<>();
        if (totalOrders > 5) {
            insights.add("Loyal shopper: placed " + totalOrders + " orders");
        }
        if (lifetimeValue > 1000) {
            insights.add("High lifetime value: $" + round(lifetimeValue));
        }
        if (averageOrderValue > 150) {
            insights.add("Prefers premium baskets (avg $" + round(averageOrderValue) + ")");
        }
        if (favoriteProduct != null) {
            insights.add("Frequently buys \"" + favoriteProduct.productName() + "\" (" + favoriteProduct.quantity() + " items total)");
        }
        if (insights.isEmpty()) {
            insights.add("New shopper: great opportunity to re-engage!");
        }
        insights.add("Total items purchased: " + totalItems);
        return insights;
    }

    private List<RecommendedProductResponse> buildRecommendations(Map<String, ProductStats> topProducts) {
        List<RecommendedProductResponse> recommendations = new ArrayList<>();
        List<ProductStats> topList = new ArrayList<>(topProducts.values());

        if (!topList.isEmpty()) {
            ProductStats fav = topList.get(0);
            recommendations.add(new RecommendedProductResponse(
                    fav.productId(),
                    fav.productName(),
                    fav.productName() + " Plus",
                    "Complimentary upgrade for their favorite item"));
        }

        if (topList.size() > 1) {
            ProductStats second = topList.get(1);
            recommendations.add(new RecommendedProductResponse(
                    second.productId(),
                    second.productName(),
                    second.productName() + " Bundle",
                    "Bundles well with their frequent purchases"));
        }

        return recommendations;
    }

    private String buildSummary(String username, int totalOrders, double lifetimeValue,
                                ProductStats favoriteProduct, String lastOrderDate) {
        StringBuilder summary = new StringBuilder();
        summary.append(username != null ? username : "This shopper");
        summary.append(" has placed ").append(totalOrders).append(totalOrders == 1 ? " order" : " orders");
        summary.append(" with $").append(round(lifetimeValue)).append(" in total spend. ");
        if (favoriteProduct != null) {
            summary.append("They gravitate toward ").append(favoriteProduct.productName()).append(". ");
        }
        summary.append("Last seen on ").append(lastOrderDate).append(". Consider nudging with fresh drops or loyalty perks.");
        return summary.toString();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

