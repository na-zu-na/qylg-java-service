package com.cc.qylgjavaservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.OrderDTO.*;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.productsDTO.ProductsCustomAdminDTO;
import com.cc.qylgjavaservice.dto.userDTO.AdminUserListDTO;
import com.cc.qylgjavaservice.entity.ProductReviews;
import com.cc.qylgjavaservice.entity.Users;
import com.cc.qylgjavaservice.enums.UserRole;
import com.cc.qylgjavaservice.service.CustomOrderService;
import com.cc.qylgjavaservice.service.OrdersService;
import com.cc.qylgjavaservice.service.ProductReviewsService;
import com.cc.qylgjavaservice.service.UserService;
import com.cc.qylgjavaservice.utils.UserContext;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @Resource
    private UserService userService;

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

    @GetMapping("/admin/orders")
    public Result<AdminOrderListVO> getAdminOrderList(@RequestParam(value = "page",defaultValue = "1") int page ,
                                                            @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                                            @RequestParam(required = false) Integer status,
                                                            @RequestParam(required = false) String keyword){
        return ordersService.getAdminOrderList(page,pageSize,status,keyword);
    }

    @PutMapping("/admin/{orderId}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long orderId,
                                                    @RequestParam Integer status) {
        return ordersService.updateOrderStatus(orderId, status);
    }

    @GetMapping("/admin/custom-orders")
    public Result<AdminCustomOrderListVO> getAdminCustomOrderList(@RequestParam(value = "page",defaultValue = "1") int page ,
                                                      @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                                      @RequestParam(required = false) Integer status,
                                                      @RequestParam(required = false) String keyword){
        return customOrderService.getAdminCustomOrderList(page,pageSize,status,keyword);
    }

    @GetMapping("/admin/get-charge")
    public Result<List<Users>> getCharge(){
        return userService.getCharge();
    }

    @PostMapping("/admin/custom-orders/{customOrderId}/assign")
    public Result<String> orderAssign(@RequestParam Long workerId,
                                      @PathVariable Long customOrderId){
        return customOrderService.orderAssign(workerId,customOrderId);
    }

    @GetMapping("/admin/custom-orders/{customOrderId}")
    public Result<AdminCustomOrderDetailVO> getAdminCustomOrderDetail(@PathVariable Long customOrderId){
        return customOrderService.getAdminCustomOrderDetail(customOrderId);
    }

    @PutMapping("/admin/custom-orders/{customOrderId}/status")
    public Result<Void> updateCustomOrderStatus(@PathVariable Long customOrderId,
                                          @RequestParam Integer status) {
        return customOrderService.updateOrderStatus(customOrderId, status);
    }

    @PostMapping("/admin/custom-orders/{customOrderId}/pic")
    public Result<Void> updateCustomPic(@PathVariable Long customOrderId,
                                        @RequestBody CustomPicDTO pic){
        return  customOrderService.updateCustomPic(customOrderId, pic);
    }

    @PostMapping("/admin/custom-orders/{customOrderId}/quote")
    public Result<Void> updateCustomQuote(@PathVariable Long customOrderId,
                                          @RequestParam BigDecimal quote){
        return  customOrderService.updateCustomQuote(customOrderId, quote);
    }
}