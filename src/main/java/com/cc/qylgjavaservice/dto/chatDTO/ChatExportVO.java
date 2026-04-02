package com.cc.qylgjavaservice.dto.chatDTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatExportVO {
    /**
     * 是否自己发送
     */
    private Boolean self;

    /**
     * 发送者名称
     */
    private String senderName;

    /**
     * 消息内容（已处理）
     */
    private String content;

    /**
     * 发送时间
     */
    private String time;
}
