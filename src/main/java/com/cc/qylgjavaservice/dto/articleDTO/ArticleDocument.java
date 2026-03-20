package com.cc.qylgjavaservice.dto.articleDTO;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.util.List;

@Data
@Document(indexName = "articles_index")
public class ArticleDocument {
    private Long id;

    private String title;

    private String content;

    private Long authorId;

    private Integer categoryId;

    private String status;

    private Long publishedAt;

    private List<String> images;

    private Completion suggest;
}
