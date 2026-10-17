package _5000.service;

import _5000.dto.CartResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CartServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${cart.service.url:http://localhost:8082}")
    private String cartServiceUrl;

    public CartResponse getCart(Long userId, String authToken) {
        String url = cartServiceUrl + "/api/cart";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        if (authToken != null) {
            headers.set("Authorization", authToken);
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<CartResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, CartResponse.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch cart from cart service: " + e.getMessage());
        }
    }

    public void clearCart(Long userId, String authToken) {
        String url = cartServiceUrl + "/api/cart";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        if (authToken != null) {
            headers.set("Authorization", authToken);
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear cart: " + e.getMessage());
        }
    }
}

