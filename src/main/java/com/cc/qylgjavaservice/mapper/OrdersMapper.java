package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.OrderDTO.OrderListDTO;
import com.cc.qylgjavaservice.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
    Page<OrderListDTO> selectOrderList(
            Page<OrderListDTO> page,
            @Param("userId") Long userId,
            @Param("status") Integer status,
            @Param("keyword") String keyword
    );
}
