package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.articleDTO.ArticleDocument;
import com.cc.qylgjavaservice.entity.Articles;

public interface ArticleSyncService {
    /**
     * 全量同步（初始化用）
     */
    public void syncAll();

    /**
     * 单个同步（新增/修改时调用）
     */
    public void syncOne(Articles articles);

    /**
     * 删除同步
     */
    public void delete(Long id);

    /**
     * Products → ES
     */
    public ArticleDocument toDoc(Articles articles);

}
