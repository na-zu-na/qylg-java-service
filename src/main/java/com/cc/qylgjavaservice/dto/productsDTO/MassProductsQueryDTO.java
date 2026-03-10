package com.cc.qylgjavaservice.dto.productsDTO;

import lombok.Data;

@Data
public class MassProductsQueryDTO {
    private String keyword;

    private String sortKey = "default";

    private String priceOrder = "asc";

    private Integer page = 1;
    private Integer pageSize = 20;
}
