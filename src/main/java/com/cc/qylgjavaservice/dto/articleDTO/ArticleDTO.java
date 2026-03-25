package com.cc.qylgjavaservice.dto.articleDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
public class ArticleDTO {
    private Long id;

    private String title;

    private String content;

    private Long authorId;

    private String avatarUrl;

    private List<String> images;

    private int status;

    private String authorName; // 联查得到

    private Long publishedAt;

    public int commentCount;

    public int likeCount;

    public boolean isLiked;

    public Integer categoryId;

    public Integer hot;

    public Integer recommended;

    public LocalDateTime createdAt;

    public LocalDateTime updatedAt;

}
