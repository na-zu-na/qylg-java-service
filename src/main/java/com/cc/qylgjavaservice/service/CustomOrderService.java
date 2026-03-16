package com.cc.qylgjavaservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.OrderDTO.CustomOrderCreateDTO;
import com.cc.qylgjavaservice.dto.OrderDTO.CustomOrderListDTO;
import com.cc.qylgjavaservice.dto.OrderDTO.CustomOrderListParamDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.CustomOrder;
import com.cc.qylgjavaservice.utils.UserContext;

public interface CustomOrderService {
    Result<Long> createCustomOrder(CustomOrderCreateDTO dto, Long currentUserId);

    Result<Page<CustomOrderListDTO>> customOrderList(CustomOrderListParamDTO dto);

    public Result<Long> receiveCustomOrder(Long id);
}
