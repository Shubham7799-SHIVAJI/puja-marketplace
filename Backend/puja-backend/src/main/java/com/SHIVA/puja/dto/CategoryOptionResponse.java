package com.SHIVA.puja.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryOptionResponse {

    private Long id;
    private String name;
    private String slug;
    private Long productCount;
}