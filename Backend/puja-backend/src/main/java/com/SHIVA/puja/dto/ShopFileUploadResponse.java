package com.SHIVA.puja.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopFileUploadResponse {

    private String registrationId;
    private String shopUniqueId;
    private String fieldName;
    private String fileName;
    private String filePath;
    private String contentType;
    private long size;
}