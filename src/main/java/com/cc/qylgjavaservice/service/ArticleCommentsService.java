package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ArticleComments;

public interface ArticleCommentsService {
    Result<Long> articleAddComment(ArticleComments comment);
}
