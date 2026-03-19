package com.cc.qylgjavaservice.controller;


import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.productsDTO.*;
import com.cc.qylgjavaservice.entity.Products;
import com.cc.qylgjavaservice.service.ProductSyncService;
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

    @Resource
    private ProductSyncService productSyncService;

    @GetMapping("/recommend")
    public Result<List<ProductsDTO>> getShopRecommend(){
        return productsService.getShopRecommend();
    }

    @GetMapping("/hot/products")
    public Result<List<ProductsDTO>> getHotProducts(){
        return productsService.getHotProducts();
    }

    @GetMapping("/products/search")
    public Result<List<Products>> searchProducts(@RequestParam(value = "keyword") String keyword,
                                                 @RequestParam(value = "type",defaultValue = "all") String type,
                                                 @RequestParam(defaultValue = "totalSales",required = false)String sortKey,
                                                 @RequestParam(defaultValue = "desc",required = false)String sortOrder){
        return productsService.searchProducts(keyword,type,sortKey,sortOrder);
    }

    @GetMapping("/products/mass-list")
    public Result<List<Products>> getMassGoodsList(MassProductsQueryDTO dto) {
        return productsService.getMassGoodsList(dto);
    }

    @GetMapping("/products/detail")
    public Result<Products> getProductsDetail(@RequestParam Long id) {
        return productsService.getProductsDetail(id);
    }

    @GetMapping("/products/{id}/reviews")
    public Result<List<ProductsReviewDTO>> getProductsReview(@PathVariable Long id) {
        return productsService.getProductsReview(id);
    }

    @GetMapping("/products/custom-list")
    public Result<List<Products>> getCustomGoodsList(MassProductsQueryDTO dto) {
        return productsService.getCustomGoodsList(dto);
    }

    @GetMapping("/products/custom/detail")
    public Result<ProductsCustomDetailDTO> getProductCustomsDetail(@RequestParam Long id) {
        return productsService.getProductCustomsDetail(id);
    }

    @GetMapping("/products/es/init")
    public void initEsDoc(){
        productSyncService.syncAll();
    }
}
