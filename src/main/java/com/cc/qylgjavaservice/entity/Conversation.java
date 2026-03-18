package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("conversations")
public class Conversation {

    private Long id;

    private Integer type;

    private Long orderId;

    private Long currentCsId;

    private String lastMessage;

    private LocalDateTime lastMessageTime;

}