package com.SHIVA.puja.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarketplaceEvent {

    private String eventId;
    private String eventType;
    private String version;
    private LocalDateTime occurredAt;
    private Map<String, Object> payload;
}
