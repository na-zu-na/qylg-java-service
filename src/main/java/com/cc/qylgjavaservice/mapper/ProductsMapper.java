package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.qylgjavaservice.dto.productsDTO.ProductsReviewDTO;
import com.cc.qylgjavaservice.entity.Products;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductsMapper extends BaseMapper<Products> {
    List<ProductsReviewDTO> getProductsReview(Long id);
}
