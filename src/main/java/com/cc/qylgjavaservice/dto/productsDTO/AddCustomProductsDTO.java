package com.cc.qylgjavaservice.dto.productsDTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AddCustomProductsDTO {
    private Long id;

    private String type;

    private String title;

    private String cover;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private String anchor;

    private String description;

    private List<String> images;

    private String purpose;

    private Integer stock;

    private String sizeType;

    private Integer makeTime;

    private List<Long> linkedStyles;

    private List<Long> linkedMaterials;
}
