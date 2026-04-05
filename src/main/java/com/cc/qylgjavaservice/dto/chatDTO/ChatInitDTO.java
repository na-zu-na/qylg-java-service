package com.cc.qylgjavaservice.dto.chatDTO;

import lombok.Data;

@Data
public class ChatInitDTO {
    private Long senderId;
    private Long receiverId;
    private Long orderId;
}