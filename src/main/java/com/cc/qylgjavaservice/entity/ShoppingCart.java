package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@TableName("shopping_cart")
public class ShoppingCart {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long goodsId;

    private Integer count;

    @TableField(value = "created_at",fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
