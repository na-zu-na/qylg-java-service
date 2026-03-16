package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.MyReviewDTO;
import com.cc.qylgjavaservice.dto.MyReviewPageVO;
import com.cc.qylgjavaservice.entity.CustomOrder;
import com.cc.qylgjavaservice.entity.Orders;
import com.cc.qylgjavaservice.entity.ProductReviews;
import com.cc.qylgjavaservice.mapper.CustomOrderMapper;
import com.cc.qylgjavaservice.mapper.OrdersMapper;
import com.cc.qylgjavaservice.mapper.ProductReviewMapper;
import com.cc.qylgjavaservice.service.ReviewService;
import com.cc.qylgjavaservice.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReviews> implements ReviewService {
    @Autowired
    private ProductReviewMapper reviewMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private CustomOrderMapper customOrderMapper;

    @Override
    public MyReviewPageVO getMyReviews(Integer page, Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();

        Page<MyReviewDTO> pageObj = new Page<>(page, pageSize);

        Page<MyReviewDTO> reviewPage = reviewMapper.selectMyReviews(pageObj, userId);

        // 已评价数量
        LambdaQueryWrapper<ProductReviews> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(ProductReviews::getUserId, userId);
        Integer commentCount = Math.toIntExact(reviewMapper.selectCount(reviewWrapper));

        // 已购买数量
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<CustomOrder> customOrderWrapper = new LambdaQueryWrapper<>();
        customOrderWrapper.eq(CustomOrder::getUserId,userId);
        orderWrapper.eq(Orders::getUserId, userId);
        Integer buyCountMass = Math.toIntExact(ordersMapper.selectCount(orderWrapper));
        Integer buyCountCustom=Math.toIntExact(customOrderMapper.selectCount(customOrderWrapper));
        Integer buyCount=buyCountCustom+buyCountMass;

        MyReviewPageVO vo = new MyReviewPageVO();

        vo.setCommentCount(commentCount);
        vo.setBuyCount(buyCount);
        vo.setPage(reviewPage);

        return vo;
    }
}
