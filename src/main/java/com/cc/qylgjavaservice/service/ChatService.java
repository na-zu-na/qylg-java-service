package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.chatDTO.*;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ChatMessage;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface ChatService {

    ChatMessage sendMessage(ChatMessage message) throws Exception;

    Result<ChatSessionVO> getSession(Long senderId, Long receiverId, Long orderId);

    Result<ChatSessionVO> createSession(Long senderId,Long receiverId,Long orderId) throws Exception;

    void ackMessage(Long messageId);

    void addAckRetryTask(Long id, int i);

    Result<ChatSessionsListVO> getSessionsList(int page, int pageSize, Integer status, String keyword);

    void exportHtml(HttpServletResponse response, Long sessionId);

    Result<ChatStaffVO> getStaff(int page, int pageSize, String keyword);

    Result<Void> setStaffAcceptStatus(Long agentId, Boolean canAccept);

    Result<List<ChatMySessions>> getWorkBench();
}
