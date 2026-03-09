package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cc.qylgjavaservice.enums.ArticleStatus;
import com.cc.qylgjavaservice.utils.JsonbTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

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

    @TableField(value = "images",
            typeHandler = JsonbTypeHandler.class,
            jdbcType = JdbcType.OTHER)
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
