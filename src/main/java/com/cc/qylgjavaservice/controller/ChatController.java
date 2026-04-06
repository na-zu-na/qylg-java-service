package com.cc.qylgjavaservice.controller;

import com.cc.qylgjavaservice.dto.chatDTO.*;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.service.ChatService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Resource
    private ChatService chatService;

    @GetMapping("/session")
    public Result<ChatSessionVO> getSession(Long senderId, Long receiverId, Long orderId) {
        return chatService.getSession(senderId, receiverId, orderId);
    }

    @PostMapping("/session/create")
    public Result<ChatSessionVO> createSession(@RequestBody ChatInitDTO dto) throws Exception {
        return chatService.createSession(dto.getSenderId(), dto.getReceiverId(), dto.getOrderId());
    }

    @GetMapping("/service/sessions")
    public Result<ChatSessionsListVO> getSessionsList(@RequestParam(value = "page",defaultValue = "1") int page ,
                                                      @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                                      @RequestParam(required = false) Integer status,
                                                      @RequestParam(required = false) String keyword){
        return chatService.getSessionsList(page,pageSize,status,keyword);
    }

    @PostMapping("/service/sessions/{sessionId}/export")
    public void exportHtml(@PathVariable Long sessionId,
                                   HttpServletResponse response){
        chatService.exportHtml(response,sessionId);
    }

    @GetMapping("/service/staff")
    public Result<ChatStaffVO> getStaff(@RequestParam(value = "page",defaultValue = "1") int page ,
                                        @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                        @RequestParam(required = false) String keyword){
        return chatService.getStaff(page,pageSize,keyword);
    }

    @PatchMapping("/service/staff/{agentId}/availability")
    public Result<Void> updateAcceptStatus(@PathVariable Long agentId,
                                           @RequestParam Boolean canAccept) {
        return chatService.setStaffAcceptStatus(agentId, canAccept);
    }

    @GetMapping("/service/workbench")
    public Result<List<ChatMySessions>> getWorkBench(){
        return chatService.getWorkBench();
    }

    @PostMapping("/service/sessions/{sessionId}/read")
    public Result<Void> readMessage(@PathVariable Long sessionId){
        return chatService.readMessage(sessionId);
    }

}
