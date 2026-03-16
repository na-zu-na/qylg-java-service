package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.MyReviewDTO;
import com.cc.qylgjavaservice.entity.ProductReviews;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReviews> {
    Page<MyReviewDTO> selectMyReviews(
            Page<MyReviewDTO> page,
            @Param("userId") Long userId
    );
}
