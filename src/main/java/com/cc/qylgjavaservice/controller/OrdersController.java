package com.cc.qylgjavaservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.OrderDTO.OrderCreateDTO;
import com.cc.qylgjavaservice.dto.OrderDTO.OrderListDTO;
import com.cc.qylgjavaservice.dto.OrderDTO.OrderQueryDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.service.OrdersService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrdersController {
    @Resource
    private OrdersService ordersService;

    @PostMapping("/create")
    public Result<Long> createOrder(@RequestBody OrderCreateDTO dto) {
            return ordersService.createOrder(dto);
    }

    @PostMapping("/list")
    public Result<Page<OrderListDTO>> getOrderList(@RequestBody OrderQueryDTO orderQueryDTO) {
        return ordersService.getOrderList(orderQueryDTO);
    }
}