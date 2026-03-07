package com.cc.qylgjavaservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.ArticleDTO;
import com.cc.qylgjavaservice.dto.ArticleDetailDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ArticleComments;

import javax.xml.stream.events.Comment;
import java.util.List;

public interface ArticleService {
    Result<Page<ArticleDTO>> getArticles(int page, int pageSize);

    Result<List<ArticleDTO>> getHotArticle();

    Result<List<ArticleDTO>> getDiscoverList(int type);

    Result<ArticleDetailDTO> getArticleDetail(int id);
}
