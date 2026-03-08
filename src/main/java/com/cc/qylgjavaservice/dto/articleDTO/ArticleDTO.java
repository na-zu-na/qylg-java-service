package com.cc.qylgjavaservice.dto.articleDTO;

import lombok.Data;

import java.util.List;


@Data
public class ArticleDTO {
    private Long id;

    private String title;

    private String content;

    private Long authorId;

    private String avatarUrl;

    private List<String> images;

    private String authorName; // 联查得到

    private Long publishedAt;

    public int commentCount;

    public int likeCount;

    public boolean isLiked;

    public int category;

}
