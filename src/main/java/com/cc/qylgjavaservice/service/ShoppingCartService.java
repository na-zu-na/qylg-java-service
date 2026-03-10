package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.ShoppingCartDTO.ShoppingCartDTO;
import com.cc.qylgjavaservice.dto.productsDTO.CartOperationDTO;

public interface ShoppingCartService {
    Result<Integer> operateCart(CartOperationDTO dto);

    Result<ShoppingCartDTO> getCart();
}
