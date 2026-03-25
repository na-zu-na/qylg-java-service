package com.cc.qylgjavaservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.articleDTO.ArticleAdminDTO;
import com.cc.qylgjavaservice.dto.articleDTO.ArticleDTO;
import com.cc.qylgjavaservice.dto.articleDTO.ArticleDetailDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.Articles;

import java.util.List;

public interface ArticleService {
    Result<Page<ArticleDTO>> getArticles(int page, int pageSize);

    Result<List<ArticleDTO>> getHotArticle();

    Result<List<ArticleDTO>> getDiscoverList(int type);

    Result<ArticleDetailDTO> getArticleDetail(int id);

    Result<List<ArticleDTO>> getMyArticle(String sortKey, String timeOrder);

    Result<Boolean> delMyArticle(Long id);

    Result<Long> postArticle(Articles articles);

    List<ArticleDTO> searchArticle(String keyword);

    Result<ArticleAdminDTO> getAdminArticles(int page, int pageSize, Integer status, Integer category_id);

    Result<Void> updateArticleStatus(Long articleId, Integer status);
}
