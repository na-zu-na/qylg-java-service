package com.cc.qylgjavaservice.dto.productsDTO.productsSettings;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductsSettingsDTO {
    private Long id;

    private String name;

    private String description;

    private Integer status;

    private String type;
}
