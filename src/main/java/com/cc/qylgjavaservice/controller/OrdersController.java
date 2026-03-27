package com.cc.qylgjavaservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.OrderDTO.*;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ProductReviews;
import com.cc.qylgjavaservice.service.CustomOrderService;
import com.cc.qylgjavaservice.service.OrdersService;
import com.cc.qylgjavaservice.service.ProductReviewsService;
import com.cc.qylgjavaservice.utils.UserContext;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrdersController {
    @Resource
    private OrdersService ordersService;

    @Resource
    private CustomOrderService customOrderService;

    @Resource
    private ProductReviewsService productReviewsService;

    @PostMapping("/create")
    public Result<Long> createOrder(@RequestBody OrderCreateDTO dto) {
            return ordersService.createOrder(dto);
    }

    @PostMapping("/list")
    public Result<Page<OrderListDTO>> getOrderList(@RequestBody OrderQueryDTO orderQueryDTO) {
        return ordersService.getOrderList(orderQueryDTO);
    }

    @GetMapping("/receive")
    public Result<Long> receiveOrder(@RequestParam  Long id){
        return ordersService.receiveOrder(id);
    }

    @GetMapping("/detail")
    public Result<OrderListDTO> orderDetail(@RequestParam  Long id){
        return ordersService.orderDetail(id);
    }

    @PostMapping("/reviews")
    public Result<Long> addReview(@RequestBody ProductReviews productReviews){
        return productReviewsService.addReview(productReviews);
    }

    @PostMapping("/custom/create")
    public Result<Long> createCustomOrder(@RequestBody CustomOrderCreateDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();

        return customOrderService.createCustomOrder(dto, currentUserId);
    }

    @PostMapping("/custom/list")
    public Result<Page<CustomOrderListDTO>> customOrderList(@RequestBody CustomOrderListParamDTO dto) {
        return customOrderService.customOrderList(dto);
    }

    @GetMapping("/custom/receive")
    public Result<Long> receiveCustomOrder(@RequestParam Long id){
        return customOrderService.receiveCustomOrder(id);
    }

    @GetMapping("/custom/confirm")
    public Result<Void> customDesignConfirm(@RequestParam  Long id){
        return customOrderService.customDesignConfirm(id);
    }
}