package com.SHIVA.puja.serviceimpl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.SHIVA.puja.dto.MarketplaceEvent;
import com.SHIVA.puja.service.MarketplaceEventPublisher;

@Service
public class MarketplaceEventPublisherImpl implements MarketplaceEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketplaceEventPublisherImpl.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public MarketplaceEventPublisherImpl(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String topic, String eventType, Map<String, Object> payload) {
        MarketplaceEvent event = MarketplaceEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .version("v1")
                .occurredAt(LocalDateTime.now())
                .payload(payload)
                .build();

        try {
            String body = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.getEventId(), body);
        } catch (JsonProcessingException exception) {
            LOGGER.error("Failed to serialize event {}", eventType, exception);
        } catch (RuntimeException exception) {
            LOGGER.warn("Kafka publish skipped for topic {} due to runtime failure: {}", topic, exception.getMessage());
        }
    }
}
