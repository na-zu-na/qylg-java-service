package com.cc.qylgjavaservice.dto.productsDTO;

import lombok.Data;

import java.util.List;

@Data
public class CartOperationDTO {

    private List<Long> goodsId;

    /**
     * 操作数量
     * add: 增加的数量 (通常传 1)
     * change: 最终设定的数量
     * delete: 忽略此字段或传 0
     */
    private Integer count;

    private String type;
}