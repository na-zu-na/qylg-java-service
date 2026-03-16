package com.cc.qylgjavaservice.dto.productsDTO;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductsCustomDetailDTO {
    private Long id;

    private String title;

    private String cover;

    @TableField("min_price")
    private BigDecimal minPrice;

    @TableField("max_price")
    private BigDecimal maxPrice;

    private String anchor;

    @TableField("description")
    private String description;

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

    @TableField("created_at")
    private LocalDateTime createdAt;

    private List<String> styleList;

    private List<String> materialList;

    private String style;

    private String material;
}
