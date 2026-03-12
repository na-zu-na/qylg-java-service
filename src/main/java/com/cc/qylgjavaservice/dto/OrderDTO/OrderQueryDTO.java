package com.cc.qylgjavaservice.dto.OrderDTO;

import lombok.Data;

@Data
public class OrderQueryDTO {
    private Integer status;

    private String keyword;

    private Long page;

    private Long pageSize;
}
