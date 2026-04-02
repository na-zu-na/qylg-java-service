package com.cc.qylgjavaservice.dto.OrderDTO;

import com.cc.qylgjavaservice.entity.Conversation;
import lombok.Data;

@Data
public class AdminCustomOrderDetailVO {
    private CustomOrderListDTO orderList;

    private Conversation conversation;
}
