package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.OrderDTO.CustomOrderListDTO;
import com.cc.qylgjavaservice.entity.CustomOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomOrderMapper extends BaseMapper<CustomOrder> {
    Page<CustomOrderListDTO> selectCustomOrderList(Page<CustomOrderListDTO> customOrderListDTOPage,Integer status,Long currentUserId,String key);
}
