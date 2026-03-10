package com.cc.qylgjavaservice.dto.ShoppingCartDTO;

import com.cc.qylgjavaservice.entity.ShoppingCart;
import lombok.Data;

import java.util.List;

@Data
public class ShoppingCartDTO {
    private List<CartItems> cartItems;

    private int proCount;


}
