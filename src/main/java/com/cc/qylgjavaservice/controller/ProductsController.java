package com.cc.qylgjavaservice.controller;


import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.productsDTO.ProductsDTO;
import com.cc.qylgjavaservice.entity.Products;
import com.cc.qylgjavaservice.service.ProductsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
