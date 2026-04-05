package com.cc.qylgjavaservice.dto.chatDTO;

import com.cc.qylgjavaservice.entity.ChatMessage;
import com.cc.qylgjavaservice.entity.Users;
import lombok.Data;

import java.util.List;

@Data
public class ChatSessionVO {
    private String conversationId;
    private List<ChatMessage> messages;
    private Users TargetUserInfo;
    private Long receiverId;
}
