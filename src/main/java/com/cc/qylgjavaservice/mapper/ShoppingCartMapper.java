package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.qylgjavaservice.dto.ShoppingCartDTO.CartItems;
import com.cc.qylgjavaservice.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
    List<CartItems> selectCart(Long currentUserId);
}
