package _5000.service;

import _5000.dto.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private static final String USER_REGISTERED_TOPIC = "user-registered";

    @Autowired
    private OrderService orderService;

    @KafkaListener(topics = USER_REGISTERED_TOPIC, groupId = "ordering-service")
    public void consumeUserRegisteredEvent(UserRegisteredEvent event) {
        logger.info("Received user registered event: userId={}, username={}", event.getUserId(), event.getUsername());
        
        // Cache user information for order creation
        orderService.updateUserCache(event.getUserId(), event.getUsername());
        
        logger.info("User information cached for order service: userId={}, username={}", event.getUserId(), event.getUsername());
    }
}

