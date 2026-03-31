package com.cc.qylgjavaservice.dto.OrderDTO;

import com.cc.qylgjavaservice.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class AdminOrderListVO {
    private List<OrderListDTO> orderList;

    private Stats stats;

    private Long current;

    private Long total;

    private Long size;

    @Data
    public static class Stats{
        private Long total;

        private Long pendingShip;

        private Long completed;
    }
}
