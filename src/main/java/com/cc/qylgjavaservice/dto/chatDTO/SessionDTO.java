package com.cc.qylgjavaservice.dto.chatDTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionDTO {
    private String id;

    private Integer type;

    private Long orderId;

    private Long currentCsId;

    private String currentCsName;

    private Long userId;

    private String userName;

    private String lastMessage;

    private boolean isOnline;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}
