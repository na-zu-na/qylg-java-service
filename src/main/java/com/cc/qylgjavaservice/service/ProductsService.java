package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.productsDTO.ProductsDTO;
import com.cc.qylgjavaservice.entity.Products;

import java.util.List;

public interface ProductsService {
    Result<List<ProductsDTO>> getShopRecommend();

    Result<List<Products>> getHotProducts();

    Result<List<Products>> searchProducts(String keyword, String type);
}
