package com.cc.qylgjavaservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.OrderDTO.OrderCreateDTO;
import com.cc.qylgjavaservice.dto.OrderDTO.OrderListDTO;
import com.cc.qylgjavaservice.dto.OrderDTO.OrderQueryDTO;
import com.cc.qylgjavaservice.dto.Result;

import java.util.List;

public interface OrdersService {
    Result<Long> createOrder(OrderCreateDTO dto);

    Result<Page<OrderListDTO>> getOrderList(OrderQueryDTO orderQueryDTO);
}
