package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.Orders;
import com.cc.qylgjavaservice.entity.ProductReviews;
import com.cc.qylgjavaservice.mapper.OrdersMapper;
import com.cc.qylgjavaservice.mapper.ProductReviewsMapper;
import com.cc.qylgjavaservice.service.ProductReviewsService;
import com.cc.qylgjavaservice.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductReviewsServiceImpl extends ServiceImpl<ProductReviewsMapper, ProductReviews> implements ProductReviewsService {
    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    public Result<Long> addReview(ProductReviews productReviews) {
        Long currentUserId = UserContext.getCurrentUserId();
        Long orderId = productReviews.getOrderId();
        Orders orders = ordersMapper.selectById(orderId);
        if (orders!=null){
            productReviews.setUserId(currentUserId);
            boolean save = this.save(productReviews);
            if (save){
                return Result.success();
            }
            else {
                return Result.fail(500,"插入失败");
            }
        }
        else {
            return Result.fail(500,"不存在订单");
        }

    }
}
