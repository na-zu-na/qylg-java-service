package com.cc.qylgjavaservice.controller;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.ShoppingCartDTO.ShoppingCartDTO;
import com.cc.qylgjavaservice.dto.productsDTO.CartOperationDTO;
import com.cc.qylgjavaservice.service.ShoppingCartService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShoppingCartController {
    @Resource
    private ShoppingCartService shoppingCartService;

    @PostMapping("/api/cart/add")
    public Result<Integer> operateCart(@RequestBody CartOperationDTO dto) {
        return shoppingCartService.operateCart(dto);
    }

    @GetMapping("/api/cart/get")
    public Result<ShoppingCartDTO> getCart() {
        return shoppingCartService.getCart();
    }
}
