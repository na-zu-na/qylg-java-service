package com.cc.qylgjavaservice.dto.productsDTO;

import com.cc.qylgjavaservice.entity.Products;
import lombok.Data;

import java.util.List;

@Data
public class ProductsAdminDTO {
    private List<ProductsDTO> productsDTOS;

    private ProductStatsDTO productStatsDTO;

    private Long current;

    private Long total;

    private Long size;
}
