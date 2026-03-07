package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cc.qylgjavaservice.dto.ArticleLikeDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ArticleLike;
import com.cc.qylgjavaservice.entity.Articles;
import com.cc.qylgjavaservice.mapper.ArticleLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ArticleLikeServiceImpl implements ArticleLikeService {

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    @Override
    @Transactional
    public Result<ArticleLikeDTO> toggleLike(Long articleId, Long userId, String action) {

        QueryWrapper<ArticleLike> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId)
                .eq("user_id", userId);

        ArticleLike like = articleLikeMapper.selectOne(wrapper);

        boolean isLiked;

        if ("cancel".equals(action)) {
            if (like != null) {
                articleLikeMapper.deleteById(like.getId());
            }

            isLiked = false;

        } else { // toggle

            if (like == null) {
                ArticleLike newLike = new ArticleLike();
                newLike.setArticleId(articleId);
                newLike.setUserId(userId);
                newLike.setCreatedAt(LocalDateTime.now());

                articleLikeMapper.insert(newLike);

                isLiked = true;

            } else {

                articleLikeMapper.deleteById(like.getId());
                isLiked = false;
            }
        }

        Long likeCount=articleLikeMapper.selectCount(new LambdaQueryWrapper<ArticleLike>().eq(ArticleLike::getArticleId,articleId));

        ArticleLikeDTO articleLikeDTO=new ArticleLikeDTO();
        articleLikeDTO.setLikeCount(likeCount);
        articleLikeDTO.setLiked(isLiked);

        return Result.success(articleLikeDTO);
    }
}