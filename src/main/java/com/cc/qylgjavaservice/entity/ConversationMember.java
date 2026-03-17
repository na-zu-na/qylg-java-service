package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("conversation_members")
public class ConversationMember {
    private Long id;
    private Long conversation_id;
    private int role;
    private Long user_id;
    private int unread_count;
    private Long last_read_message_id;
}
