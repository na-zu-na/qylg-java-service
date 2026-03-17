package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.ChatSessionVO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ChatMessage;

public interface ChatService {

    ChatMessage sendMessage(ChatMessage message);

    Result<ChatSessionVO> getOrCreateSession(Long senderId,Long receiverId,Long orderId) throws Exception;
}
