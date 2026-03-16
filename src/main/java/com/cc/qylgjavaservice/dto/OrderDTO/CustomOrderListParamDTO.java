package com.cc.qylgjavaservice.dto.OrderDTO;

import lombok.Data;

@Data
public class CustomOrderListParamDTO {
    private Integer status;
    private Integer page;
    private Integer size;
    private String key;
}
