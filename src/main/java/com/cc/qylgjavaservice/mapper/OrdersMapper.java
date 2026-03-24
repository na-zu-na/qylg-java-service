package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.OrderDTO.OrderListDTO;
import com.cc.qylgjavaservice.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
    Page<OrderListDTO> selectOrderList(
            Page<OrderListDTO> page,
            @Param("userId") Long userId,
            @Param("status") Integer status,
            @Param("keyword") String keyword
    );

    OrderListDTO selectOrderDetail(Long id);

    @Select("""
    SELECT COALESCE(SUM(total_price), 0)
    FROM orders
    WHERE user_id = #{userId}
""")
    Long sumTotalSpend(Long userId);
}
