package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "article_comments",autoResultMap = true)
public class ArticleComments {
    @TableId(value = "id",type= IdType.AUTO)
    private Long id;

    @TableField("article_id")
    private Long articleId;

    @TableField("user_id")
    private Long userId;

    @TableField("root_id")
    private Long rootId;

    @TableField("parent_id")
    private Long parentId;

    @TableField("content")
    private String content;

    @TableField("reply_to_user_id")
    private Long replyToUserId;

    @TableField(value = "status")
    private int status;

    @TableField("created_at")
    private Long createdAt;

}
