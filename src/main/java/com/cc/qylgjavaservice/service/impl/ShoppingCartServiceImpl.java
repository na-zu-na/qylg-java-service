package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.ShoppingCartDTO.CartItems;
import com.cc.qylgjavaservice.dto.ShoppingCartDTO.ShoppingCartDTO;
import com.cc.qylgjavaservice.dto.productsDTO.CartOperationDTO;
import com.cc.qylgjavaservice.entity.ShoppingCart;
import com.cc.qylgjavaservice.mapper.ShoppingCartMapper;
import com.cc.qylgjavaservice.service.ShoppingCartService;
import com.cc.qylgjavaservice.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Override
    public Result<Integer> operateCart(CartOperationDTO dto) {

        Long userId = UserContext.getCurrentUserId();
        List<Long> goodsIds = dto.getGoodsId(); // 现在是 List
        String type = dto.getType();
        Integer inputCount = dto.getCount();

        if (goodsIds == null || goodsIds.isEmpty()) {
            return Result.success(0);
        }

        // ================= 删除 =================
        if ("delete".equalsIgnoreCase(type)) {

            LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShoppingCart::getUserId, userId)
                    .in(ShoppingCart::getGoodsId, goodsIds);

            this.remove(wrapper);

        }

        // ================= 修改数量 =================
        else if ("change".equalsIgnoreCase(type)) {

            // 修改数量一般只针对单个商品
            Long goodsId = goodsIds.get(0);

            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId, userId)
                    .eq(ShoppingCart::getGoodsId, goodsId);

            ShoppingCart existItem = this.getOne(queryWrapper);

            if (inputCount <= 0) {

                // 数量为0删除
                if (existItem != null) {
                    this.removeById(existItem.getId());
                }

            } else {

                if (existItem == null) {

                    // 新增
                    ShoppingCart newItem = new ShoppingCart();
                    newItem.setUserId(userId);
                    newItem.setGoodsId(goodsId);
                    newItem.setCount(1);

                    this.save(newItem);

                } else {
                    if (inputCount==2147483647){
                        existItem.setCount(existItem.getCount()+1);
                    }
                    else {
                        // 更新数量
                        existItem.setCount(inputCount);
                    }
                    this.updateById(existItem);
                }
            }
        }

        // ================= 统计购物车数量 =================
        int proCount = calculateTotalCount(userId);

        return Result.success(proCount);
    }

    @Override
    public Result<ShoppingCartDTO> getCart() {
        Long currentUserId = UserContext.getCurrentUserId();
        List<CartItems> cartItems=shoppingCartMapper.selectCart(currentUserId);

        ShoppingCartDTO shoppingCartDTOS=new ShoppingCartDTO();
        shoppingCartDTOS.setCartItems(cartItems);
        shoppingCartDTOS.setProCount(calculateTotalCount(currentUserId));
        return Result.success(shoppingCartDTOS);
    }

    private int calculateTotalCount(Long userId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
                .select(ShoppingCart::getCount); // 只查询 count 列，优化性能

        List<ShoppingCart> cartList = this.list(wrapper);

        // 内存累加 (使用 mapToLong 防止极端情况溢出，最后转回 int)
        // 如果列表为空，sum() 默认返回 0
        long total = cartList.stream()
                .mapToLong(ShoppingCart::getCount)
                .sum();

        return (int) total;
    }
}
