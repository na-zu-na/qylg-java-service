package com.cc.qylgjavaservice.dto.chatDTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationSessionsDTO{
    private Long id;
    private LocalDateTime createdAt;
    private String agent;
}