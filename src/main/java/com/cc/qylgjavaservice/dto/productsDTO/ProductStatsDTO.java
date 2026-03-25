package com.cc.qylgjavaservice.dto.productsDTO;

import lombok.Data;

@Data
public class ProductStatsDTO {

    // status=1
    private Long normal;

    // 商品总数
    private Long totalCount;

    // stock < 100
    private Integer lowStock;

    // total_sales > 500
    private Integer hot;
}
