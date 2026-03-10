package com.cc.qylgjavaservice.dto.productsDTO;

import lombok.Data;

import java.util.List;

@Data
public class ProductsReviewDTO {
    private Long id;
    private String nickName;
    private String avatarUrl;
    private String content;
    private Integer rate;
    private List<String> images;
    private Long publishedAt;
}
