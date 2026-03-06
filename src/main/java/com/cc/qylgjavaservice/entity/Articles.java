package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.cc.qylgjavaservice.enums.ArticleStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "articles",autoResultMap = true)
public class Articles {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("author_id")
    private Long authorId;

    @TableField("category_id")
    private Integer categoryId;

    @TableField("content")
    private String content;

    @TableField(value = "images",typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    @TableField("status")
    private ArticleStatus status= ArticleStatus.PUBLISH;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @TableField("published_at")
    private Long publishedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
