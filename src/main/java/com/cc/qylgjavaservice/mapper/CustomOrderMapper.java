package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.OrderDTO.AdminCustomOrderListVO;
import com.cc.qylgjavaservice.dto.OrderDTO.CustomOrderListDTO;
import com.cc.qylgjavaservice.entity.CustomOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomOrderMapper extends BaseMapper<CustomOrder> {
    Page<CustomOrderListDTO> selectCustomOrderList(Page<CustomOrderListDTO> customOrderListDTOPage,Integer status,Long currentUserId,String key);

    Page<CustomOrderListDTO> selectAdminOrderList(Page<CustomOrderListDTO> dtoPage, Integer status, String key, boolean isPending);

    @Select("""
    select
        count(id) as total,
        sum(case when worker_id is null then 1 else 0 end) as pendingAssign,
        sum(case when status=0 then 1 else 0 end) as inProgress,
        sum(case when status=2 then 1 else 0 end) as completed
    from custom_orders
""")
    AdminCustomOrderListVO.Stats selectStats();

    CustomOrderListDTO selectAdminCustomOrderDetail(Long customOrderId);

    List<CustomOrderListDTO> selectByOrderIds(@Param("list") List<Long> list);
}
