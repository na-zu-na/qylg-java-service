package com.cc.qylgjavaservice.dto.productsDTO;

import lombok.Data;

import java.util.List;

@Data
public class ProductsCustomAdminDTO {
    private List<ProductsCustomDTO> productsCustomDTOS;

    private ProductStatsDTO productStatsDTO;

    private Long current;

    private Long total;

    private Long size;
}
