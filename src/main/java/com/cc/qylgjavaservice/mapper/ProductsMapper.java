package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.productsDTO.ProductStatsDTO;
import com.cc.qylgjavaservice.dto.productsDTO.ProductsCustomDetailDTO;
import com.cc.qylgjavaservice.dto.productsDTO.ProductsDTO;
import com.cc.qylgjavaservice.dto.productsDTO.ProductsReviewDTO;
import com.cc.qylgjavaservice.entity.Products;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductsMapper extends BaseMapper<Products> {
    List<ProductsReviewDTO> getProductsReview(Long id);

    ProductsCustomDetailDTO getProductCustomsDetail(Long id);

    Page<ProductsDTO> selectAdminProductsPage(Page<ProductsDTO> productsDTOPage, Integer status, String keyword);

    ProductStatsDTO selectProductStats();
}
