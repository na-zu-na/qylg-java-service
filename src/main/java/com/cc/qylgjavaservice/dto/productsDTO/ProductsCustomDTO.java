package com.cc.qylgjavaservice.dto.productsDTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductsCustomDTO {
    private Long id;

    private String type;

    private String title;

    private String cover;

    private BigDecimal price;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer makeTime;

    private Integer totalSales;

    private Boolean hot;

    private int status;

    private String purpose;

    private String anchor;

    private Long stock;

    private LocalDateTime createdAt;
}
