package com.cc.qylgjavaservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.cc.qylgjavaservice.mapper.ProductsMapper;
import com.cc.qylgjavaservice.utils.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品表实体类
 * 对应数据库表: products
 */
@Data
@TableName(value = "products", autoResultMap = true) // 【重要】开启自动映射，否则 TypeHandler 不生效
public class Products {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类型: 1-普通商品，2-定制商品
     */
    private String type;

    private String title;

    private String cover;

    private BigDecimal price;

    @TableField("min_price")
    private BigDecimal minPrice;

    @TableField("max_price")
    private BigDecimal maxPrice;

    private String anchor;

    @TableField("description")
    private String description;

    @TableField(value = "images",
            typeHandler = JsonbTypeHandler.class,
            jdbcType = JdbcType.OTHER)
    private List<String> images;

    private String purpose;


    @TableField("total_sales")
    private Integer totalSales;

    private Integer stock;

    /**
     * 状态: 0-下架，1-上架
     */
    private Integer status;

    private String sizeRange;

    private Integer makeTime;

    @TableField(value = "created_at",fill = FieldFill.DEFAULT)
    private LocalDateTime createdAt;
}