package com.cc.qylgjavaservice.dto.OrderDTO;

import com.cc.qylgjavaservice.dto.ConversationSessionsDTO;
import com.cc.qylgjavaservice.entity.Conversation;
import lombok.Data;

import java.util.List;

@Data
public class AdminCustomOrderDetailVO {
    private CustomOrderListDTO orderList;

    private Conversation conversation;
}
