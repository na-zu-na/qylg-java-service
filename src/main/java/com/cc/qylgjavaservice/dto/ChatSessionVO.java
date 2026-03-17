package com.cc.qylgjavaservice.dto;

import com.cc.qylgjavaservice.entity.ChatMessage;
import com.cc.qylgjavaservice.entity.Users;
import lombok.Data;

import java.util.List;

@Data
public class ChatSessionVO {
    private String ConversationId;
    private List<ChatMessage> messages;
    private Users TargetUserInfo;
    private Long receiverId;
}
