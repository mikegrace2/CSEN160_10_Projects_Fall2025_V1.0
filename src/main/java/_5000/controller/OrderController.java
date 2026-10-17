package _5000.controller;

import _5000.dto.CreateOrderRequest;
import _5000.dto.OrderResponse;
import _5000.dto.UserProfileSummaryResponse;
import _5000.model.Order;
import _5000.service.OrderService;
import _5000.service.UserInsightsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserInsightsService userInsightsService;

    @PostMapping
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Username", required = false) String usernameHeader) {
        try {
            // Get user ID from header (set by nginx after token validation)
            Long userId;
            if (userIdHeader != null) {
                userId = Long.parseLong(userIdHeader);
            } else {
                return ResponseEntity.status(401).body("User authentication required");
            }
            
            // Validate that request userId (if provided) matches authenticated user
            if (request.getUserId() != null && !request.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body("Cannot create order for different user");
            }
            
            // Use authenticated user's ID
            request.setUserId(userId);
            
            // Use username from header if available
            String username = usernameHeader != null ? usernameHeader : "Unknown";
            
            Order order = orderService.createOrder(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(new OrderResponse(order));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body("Invalid user ID in header");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/from-cart")
    public ResponseEntity<?> createOrderFromCart(
            @RequestParam(value = "clearCart", defaultValue = "true") boolean clearCart,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Username", required = false) String usernameHeader,
            @RequestHeader(value = "Authorization", required = false) String authToken) {
        try {
            // Get user ID from header (set by nginx after token validation)
            Long userId;
            if (userIdHeader != null) {
                userId = Long.parseLong(userIdHeader);
            } else {
                return ResponseEntity.status(401).body("User authentication required");
            }
            
            // Use username from header if available
            String username = usernameHeader != null ? usernameHeader : "Unknown";
            
            Order order = orderService.createOrderFromCart(userId, username, authToken, clearCart);
            return ResponseEntity.status(HttpStatus.CREATED).body(new OrderResponse(order));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body("Invalid user ID in header");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/profile/summary")
    public ResponseEntity<?> getUserProfileSummary(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-Username", required = false) String usernameHeader) {
        try {
            if (userIdHeader == null) {
                return ResponseEntity.status(401).body("User authentication required");
            }
            Long userId = Long.parseLong(userIdHeader);

            UserProfileSummaryResponse summary = userInsightsService.buildUserProfileSummary(userId);
            if (summary.getUsername() == null && usernameHeader != null) {
                summary.setUsername(usernameHeader);
            }
            return ResponseEntity.ok(summary);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body("Invalid user ID in header");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        // Validate that user can only access their own orders
        if (userIdHeader != null) {
            Long authenticatedUserId = Long.parseLong(userIdHeader);
            if (!userId.equals(authenticatedUserId)) {
                return ResponseEntity.status(403).body("Cannot access orders for different user");
            }
        }
        
        List<Order> orders = orderService.getOrdersByUserId(userId);
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{orderId:\\d+}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(new OrderResponse(order));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

