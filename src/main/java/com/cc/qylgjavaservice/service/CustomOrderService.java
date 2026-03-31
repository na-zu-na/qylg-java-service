package com.cc.qylgjavaservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.OrderDTO.*;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.CustomOrder;
import com.cc.qylgjavaservice.utils.UserContext;

import java.math.BigDecimal;

public interface CustomOrderService {
    Result<Long> createCustomOrder(CustomOrderCreateDTO dto, Long currentUserId);

    Result<Page<CustomOrderListDTO>> customOrderList(CustomOrderListParamDTO dto);

    public Result<Long> receiveCustomOrder(Long id);

    Result<Void> customDesignConfirm(Long id);

    Result<AdminCustomOrderListVO> getAdminCustomOrderList(int page, int pageSize, Integer status, String keyword);

    Result<String> orderAssign(Long workerId, Long customOrderId);

    Result<AdminCustomOrderDetailVO> getAdminCustomOrderDetail(Long customOrderId);

    Result<Void> updateOrderStatus(Long customOrderId, Integer status);

    Result<Void> updateCustomPic(Long customOrderId, CustomPicDTO url);

    Result<Void> updateCustomQuote(Long customOrderId, BigDecimal quote);
}
