package com.cc.qylgjavaservice.controller;

import com.cc.qylgjavaservice.dto.AiDTO.AiSearchRequestDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.Products;
import com.cc.qylgjavaservice.service.ProductsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    @Resource
    private ProductsService productsService;

    @PostMapping("/search/product")
    public Result<List<Products>> aiSearchProduct(@RequestBody AiSearchRequestDTO dto){
        return productsService.aiSearchProduct(dto.getText());
    }
}
