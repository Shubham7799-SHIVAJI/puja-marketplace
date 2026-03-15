package com.SHIVA.puja.service;

import java.util.Map;

public interface MarketplaceEventPublisher {

    void publish(String topic, String eventType, Map<String, Object> payload);
}
