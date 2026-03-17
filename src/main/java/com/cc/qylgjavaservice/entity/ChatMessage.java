package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_messages")
public class ChatMessage {

    private Long id;

    private Long conversationId;

    private Long senderId;

    private Long receiverId;

    private String content;

    private String msgType;

    private Integer status;

    private LocalDateTime createdAt;

}