package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.articleDTO.CommentsDTO;
import com.cc.qylgjavaservice.dto.articleDTO.ArticleDTO;
import com.cc.qylgjavaservice.entity.Articles;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Articles> {
    Page<ArticleDTO> selectArticlePage(Page<ArticleDTO> page);

    List<ArticleDTO> selectHotArticle();

    List<ArticleDTO> selectDiscoverList(@Param("type") int type);

    ArticleDTO selectArticleDetail(@Param("id") int id);

    List<CommentsDTO> selectArticleComments(@Param("id") int id);

    List<ArticleDTO> selectMyArticle(@Param("id") Long id,@Param("sortKey") String sortKey, @Param("timeOrder") String timeOrder);
}
