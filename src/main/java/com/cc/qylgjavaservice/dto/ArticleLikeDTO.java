package com.cc.qylgjavaservice.dto;

import lombok.Data;

@Data
public class ArticleLikeDTO {
    private Long likeCount;
    private boolean isLiked;
}
