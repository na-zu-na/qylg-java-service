package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.cc.qylgjavaservice.utils.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("custom_orders")
public class CustomOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long productId;

    private String purpose;
    private String style;
    private String material;

    private BigDecimal minBudget;
    private BigDecimal maxBudget;

    @TableField(typeHandler = JsonbTypeHandler.class,jdbcType = JdbcType.OTHER)
    private List<String> colors;

    @TableField(typeHandler = JsonbTypeHandler.class,jdbcType = JdbcType.OTHER)
    private List<String> patterns;

    private String size;
    private String remark;

    @TableField(typeHandler = JsonbTypeHandler.class,jdbcType = JdbcType.OTHER)
    private List<String> images;

    private int status; // 'making', 'shipping', 'finished'

    private Long createdAt;

    private BigDecimal quote;

    private String proposalImage;

    private int isConfirmed;
}