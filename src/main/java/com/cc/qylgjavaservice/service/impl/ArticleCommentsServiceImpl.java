package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ArticleComments;
import com.cc.qylgjavaservice.mapper.ArticleCommentsMapper;
import com.cc.qylgjavaservice.service.ArticleCommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleCommentsServiceImpl extends ServiceImpl<ArticleCommentsMapper, ArticleComments> implements ArticleCommentsService {

    @Autowired
    private ArticleCommentsMapper articleCommentsMapper;

    @Override
    public Result<Long> articleAddComment(ArticleComments comment) {
        boolean success = this.save(comment);
        if (success){
            Long id=comment.getId();
            return Result.success(id);
        }
        return Result.fail(400,"插入错误");
    }
}
