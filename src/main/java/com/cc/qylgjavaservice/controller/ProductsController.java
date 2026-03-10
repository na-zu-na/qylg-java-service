package com.cc.qylgjavaservice.controller;


import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.productsDTO.CartOperationDTO;
import com.cc.qylgjavaservice.dto.productsDTO.MassProductsQueryDTO;
import com.cc.qylgjavaservice.dto.productsDTO.ProductsDTO;
import com.cc.qylgjavaservice.dto.productsDTO.ProductsReviewDTO;
import com.cc.qylgjavaservice.entity.Products;
import com.cc.qylgjavaservice.service.ProductsService;
import com.cc.qylgjavaservice.service.ShoppingCartService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop")
public class ProductsController {
    @Resource
    private ProductsService productsService;

    @GetMapping("/recommend")
    public Result<List<ProductsDTO>> getShopRecommend(){
        return productsService.getShopRecommend();
    }

    @GetMapping("/hot/products")
    public Result<List<Products>> getHotProducts(){
        return productsService.getHotProducts();
    }

    @GetMapping("/products/search")
    public Result<List<Products>> searchProducts(@RequestParam(value = "keyword") String keyword,
                                                 @RequestParam(value = "type",defaultValue = "all") String type){
        return productsService.searchProducts(keyword,type);
    }

    @GetMapping("/products/mass-list")
    public Result<List<Products>> getMassGoodsList(MassProductsQueryDTO dto) {
        return productsService.getgetMassGoodsList(dto);
    }

    @GetMapping("/products/detail")
    public Result<Products> getProductsDetail(@RequestParam Long id) {
        return productsService.getProductsDetail(id);
    }

    @GetMapping("/products/{id}/reviews")
    public Result<List<ProductsReviewDTO>> getProductsReview(@PathVariable Long id) {
        return productsService.getProductsReview(id);
    }
}
