package com.cc.qylgjavaservice.controller;

import com.cc.qylgjavaservice.dto.ChatSessionVO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ChatMessage;
import com.cc.qylgjavaservice.service.ChatService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Resource
    private ChatService chatService;

    @GetMapping("/init")
    public Result<ChatSessionVO> initSession(@RequestParam(required = false) Long senderId,
                                             @RequestParam(required = false) Long receiverId,
                                             @RequestParam(required = false) Long orderId) throws Exception {

        return chatService.getOrCreateSession(senderId,receiverId,orderId);
    }
}
