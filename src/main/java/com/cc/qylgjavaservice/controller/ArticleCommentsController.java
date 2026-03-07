package com.cc.qylgjavaservice.controller;

import com.cc.qylgjavaservice.dto.ArticleDetailDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ArticleComments;
import com.cc.qylgjavaservice.service.ArticleCommentsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
public class ArticleCommentsController {
    @Resource
    private ArticleCommentsService commentsService;

    @PostMapping("/articles/comments")
    public Result<Long> articleAddComment(@RequestBody ArticleComments comment){
        comment.setStatus(1);
        return commentsService.articleAddComment(comment);
    }
}
