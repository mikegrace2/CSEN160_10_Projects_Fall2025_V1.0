package _5000.service;

import _5000.dto.OrderCreatedEvent;
import _5000.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String ORDER_CREATED_TOPIC = "order-created";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreatedEvent(Order order) {
        try {
            OrderCreatedEvent event = new OrderCreatedEvent(
                    order.getId(),
                    order.getUserId(),
                    order.getUsername(),
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getCreatedAt()
            );

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(ORDER_CREATED_TOPIC, event);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Sent order created event with offset=[{}]", result.getRecordMetadata().offset());
                } else {
                    logger.error("Unable to send order created event=[{}] due to : {}", event, ex.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error publishing order created event", e);
        }
    }
}

