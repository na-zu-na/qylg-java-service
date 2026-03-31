package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.text.DateFormat;
import java.time.LocalDateTime;

@Data
@TableName("conversations")
public class Conversation {

    private Long id;

    private Integer type;

    private Long orderId;

    private Long currentCsId;

    private String lastMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}