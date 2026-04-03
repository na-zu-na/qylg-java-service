package com.cc.qylgjavaservice.controller;

import com.cc.qylgjavaservice.dto.chatDTO.ChatSessionVO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.chatDTO.ChatSessionsListVO;
import com.cc.qylgjavaservice.dto.chatDTO.ChatStaffVO;
import com.cc.qylgjavaservice.service.ChatService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

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

}
