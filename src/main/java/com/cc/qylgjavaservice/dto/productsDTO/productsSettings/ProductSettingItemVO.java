package com.cc.qylgjavaservice.dto.productsDTO.productsSettings;

import lombok.Data;

@Data
public class ProductSettingItemVO {

    /**
     * ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 关联商品数
     */
    private Long relationCount;

    /**
     * 分类：style / material
     */
    private String type;

    /**
     * 状态：0=启用，1=禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private String createdAt;
}
