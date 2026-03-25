package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.productsDTO.*;
import com.cc.qylgjavaservice.entity.Products;

import java.util.List;

public interface ProductsService {
    Result<List<ProductsDTO>> getShopRecommend();

    Result<List<ProductsDTO>> getHotProducts();

    Result<List<Products>> searchProducts(String keyword, String type,String sorKey, String sortOrder);

    Result<List<Products>> getMassGoodsList(MassProductsQueryDTO dto);

    Result<Products> getProductsDetail(Long id);

    Result<List<ProductsReviewDTO>> getProductsReview(Long id);

    Result<List<Products>> getCustomGoodsList(MassProductsQueryDTO dto);

    Result<ProductsCustomDetailDTO> getProductCustomsDetail(Long id);

    Result<ProductsAdminDTO> getAdminProMass(int page, int pageSize, Integer status, String keyword);

    Result<Void> updateProductStatus(Long id, Integer status);

    Result<Long> addMassProduct(Products dto);

    Result<Products> getMassProductDetail(Long productId);

    Result<Long> editMassProduct(Products dto);
}
