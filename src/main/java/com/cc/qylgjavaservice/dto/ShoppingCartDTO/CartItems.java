package com.cc.qylgjavaservice.dto.ShoppingCartDTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItems {
    private Long id;

    private Long userId;

    private Long goodsId;

    private Integer count;

    private String title;

    private String cover;

    private BigDecimal price;
}
