package _5000.service;

import _5000.dto.CartResponse;
import _5000.dto.CreateOrderRequest;
import _5000.dto.OrderItemRequest;
import _5000.model.Order;
import _5000.model.OrderItem;
import _5000.model.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private CartServiceClient cartServiceClient;

    // In-memory storage
    private static final Map<Long, Order> orderMap = new ConcurrentHashMap<>();
    private static final Map<Long, List<Order>> userOrdersMap = new ConcurrentHashMap<>();
    private static final Map<String, String> userIdToUsernameMap = new ConcurrentHashMap<>();
    private static final AtomicLong idCounter = new AtomicLong(0);

    public Order createOrder(CreateOrderRequest request, String username) {
        // Username is now passed from the controller (from nginx headers)

        // Convert OrderItemRequest to OrderItem
        List<OrderItem> items = request.getItems().stream()
                .map(itemRequest -> new OrderItem(
                        itemRequest.getProductId(),
                        itemRequest.getProductName(),
                        itemRequest.getQuantity(),
                        itemRequest.getPrice()
                ))
                .collect(Collectors.toList());

        // Calculate total amount
        Double totalAmount = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // Create order
        long newId = idCounter.incrementAndGet();
        Order order = new Order(
                newId,
                request.getUserId(),
                username,
                items,
                totalAmount,
                OrderStatus.PENDING
        );

        // Save order
        orderMap.put(order.getId(), order);
        userOrdersMap.computeIfAbsent(order.getUserId(), k -> new java.util.ArrayList<>()).add(order);

        // Publish order created event to Kafka
        kafkaProducerService.publishOrderCreatedEvent(order);

        return order;
    }

    public Order getOrderById(Long orderId) {
        Order order = orderMap.get(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }
        return order;
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return userOrdersMap.getOrDefault(userId, new java.util.ArrayList<>());
    }

    public List<Order> getAllOrders() {
        return orderMap.values().stream().collect(Collectors.toList());
    }

    public void updateUserCache(Long userId, String username) {
        userIdToUsernameMap.put(userId.toString(), username);
    }

    public Order createOrderFromCart(Long userId, String username, String authToken, boolean clearCart) {
        // Fetch cart from cart service
        CartResponse cartResponse = cartServiceClient.getCart(userId, authToken);
        
        if (cartResponse == null || cartResponse.getItems() == null || cartResponse.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Convert cart items to order items
        List<OrderItemRequest> orderItems = cartResponse.getItems().stream()
                .map(cartItem -> {
                    OrderItemRequest itemRequest = new OrderItemRequest();
                    itemRequest.setProductId(cartItem.getProductId());
                    itemRequest.setProductName(cartItem.getProductName());
                    itemRequest.setQuantity(cartItem.getQuantity());
                    itemRequest.setPrice(cartItem.getPrice());
                    return itemRequest;
                })
                .collect(Collectors.toList());

        // Create order request
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setItems(orderItems);

        // Create order
        Order order = createOrder(orderRequest, username);

        // Cart will be automatically cleared via Kafka order-created event
        // This provides synchronous clearing, but Kafka will also handle it asynchronously
        if (clearCart) {
            try {
                cartServiceClient.clearCart(userId, authToken);
            } catch (Exception e) {
                // Log error but don't fail the order creation
                // Cart will still be cleared via Kafka event
                System.err.println("Failed to clear cart via REST (will be cleared via Kafka): " + e.getMessage());
            }
        }

        return order;
    }
}

