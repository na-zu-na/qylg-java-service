package com.cc.qylgjavaservice.dto.articleDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
public class ArticleAdminDTO {
    private List<ArticleDTO> articleDTO;

    private Long normal;

    private Long disable;

    private Long current;

    private Long total;

    private Long size;
}
