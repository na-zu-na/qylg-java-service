package com.cc.qylgjavaservice.dto.articleDTO;

import lombok.Data;

@Data
public class ArticleLikeDTO {
    private Long likeCount;
    private boolean isLiked;
}
