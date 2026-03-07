package com.cc.qylgjavaservice.service.impl;

import com.cc.qylgjavaservice.dto.ArticleLikeDTO;
import com.cc.qylgjavaservice.dto.Result;

public interface ArticleLikeService {
    Result<ArticleLikeDTO> toggleLike(Long articleId, Long userId, String action);

}
