package com.cc.qylgjavaservice.dto.productsDTO;

import com.cc.qylgjavaservice.entity.Products;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductsDTO{
    private Long id;

    private String type;

    private String title;

    private String cover;

    private BigDecimal price;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer totalSales;

    private Boolean hot;
}
