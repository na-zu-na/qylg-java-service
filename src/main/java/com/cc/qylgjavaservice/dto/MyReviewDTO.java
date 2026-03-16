package com.cc.qylgjavaservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MyReviewDTO {

    private Long id;

    private String content;

    private Integer rate;

    private List<String> images;

    private Long publishedAt;

    private Long goodsId;

    private String title;

    private String cover;

    private BigDecimal price;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;
}
