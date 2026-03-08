package com.cc.qylgjavaservice.dto.articleDTO;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommentsDTO {
    private int id;

    private int userId;

    private String nickName;

    private String avatarUrl;

    private String content;

    private Long replyToUserId;

    private String replyToUserName;

    private int parentId;

    private long createdAt;

    //存放子评论
    private List<CommentsDTO> replies = new ArrayList<>();
}
