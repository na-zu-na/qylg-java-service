package com.cc.qylgjavaservice.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("order_items")
public class OrderItems {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long goodsId;

    private Integer count;

    private BigDecimal price;
}
