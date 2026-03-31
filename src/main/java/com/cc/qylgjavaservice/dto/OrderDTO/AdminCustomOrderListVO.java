package com.cc.qylgjavaservice.dto.OrderDTO;

import lombok.Data;

import java.util.List;

@Data
public class AdminCustomOrderListVO {
    private List<CustomOrderListDTO> orderList;

    private Stats stats;

    private Long current;

    private Long total;

    private Long size;

    @Data
    public static class Stats{
        private Long total;

        private Long pendingAssign;

        private Long inProgress;

        private Long completed;
    }
}
