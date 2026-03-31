package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.OrderDTO.AdminOrderListVO;
import com.cc.qylgjavaservice.dto.OrderDTO.OrderListDTO;
import com.cc.qylgjavaservice.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    Page<OrderListDTO> selectAdminOrderList(Page<OrderListDTO> orderListDTOPage, Integer status, String keyword);

    @Select("""
    select
        count(id) as total,
        sum(case when shipped is null then 1 else 0 end) as pendingShip,
        sum(case when closed is null then 1 else 0 end) as completed
    from orders
""")
    AdminOrderListVO.Stats selectStats();
}
