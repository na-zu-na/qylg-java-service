package com.cc.qylgjavaservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.OrderDTO.*;
import com.cc.qylgjavaservice.dto.Result;

import java.util.List;

public interface OrdersService {
    Result<Long> createOrder(OrderCreateDTO dto);

    Result<Page<OrderListDTO>> getOrderList(OrderQueryDTO orderQueryDTO);

    Result<Long> receiveOrder(Long id);

    Result<OrderListDTO> orderDetail(Long id);

    Result<Void> updateOrderStatus(Long orderId, Integer status);

    Result<AdminOrderListVO> getAdminOrderList(int page, int pageSize, Integer status, String keyword);
}
