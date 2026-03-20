package com.cc.qylgjavaservice.search;

import com.cc.qylgjavaservice.dto.articleDTO.ArticleDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ArticleEsRepository extends ElasticsearchRepository<ArticleDocument, Long> {
}
