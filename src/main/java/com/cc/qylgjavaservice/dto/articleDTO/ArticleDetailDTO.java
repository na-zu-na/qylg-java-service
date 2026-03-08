package com.cc.qylgjavaservice.dto.articleDTO;

import lombok.Data;

import java.util.List;

@Data
public class ArticleDetailDTO {
    private ArticleDTO articleDTO;
    private List<CommentsDTO> commentsDTO;
}
