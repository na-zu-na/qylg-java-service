package com.cc.qylgjavaservice.dto.chatDTO;

import com.cc.qylgjavaservice.dto.OrderDTO.CustomOrderListDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMySessions {
    private String id;

    private Long userId;
    private String userName;

    //0表示离线，1表示在线
    private Integer onlineStatus;

    @JsonFormat(pattern = "YYYY-MM-DD hh-mm-ss")
    private LocalDateTime lastMessageTime;
    @JsonFormat(pattern = "YYYY-MM-DD hh-mm-ss")
    private LocalDateTime createdAt;

    private String lastMessage;

    private Long orderId;

    private Long currentCsId;

    private String currentCsName;

    private CustomOrderListDTO customOrderListDTO;
}
