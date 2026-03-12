package com.cc.qylgjavaservice.dto.OrderDTO;

import lombok.Data;
import java.util.List;

@Data
public class OrderCreateDTO {
    private List<OrderItemDTO> items;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    @Data
    public static class OrderItemDTO {
        private Long id; // 商品ID
        private Integer count; // 数量
        private Double price; // 单价
    }
}
