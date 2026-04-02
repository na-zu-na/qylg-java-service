package com.cc.qylgjavaservice.dto.chatDTO;

import com.cc.qylgjavaservice.entity.Conversation;
import lombok.Data;

import java.util.List;

@Data
public class ChatSessionsListVO {
    private List<SessionDTO> conversations;

    private Stats stats;

    private Long current;

    private Long total;

    private Long size;

    @Data
    public static class Stats{
        private Long total;

        private Long onLine;

        private Long history;
    }
}
