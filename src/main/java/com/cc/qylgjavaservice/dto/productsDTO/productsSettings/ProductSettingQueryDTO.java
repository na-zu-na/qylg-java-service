package com.cc.qylgjavaservice.dto.productsDTO.productsSettings;

import lombok.Data;

@Data
public class ProductSettingQueryDTO {

    /**
     * 页码，默认1
     */
    private Integer page = 1;

    /**
     * 每页条数，默认10
     */
    private Integer pageSize = 10;

    /**
     * 搜索名称 / 描述 / ID
     */
    private String keyword;

    /**
     * 状态：1=启用，0=禁用
     */
    private Integer status;

    /**
     * 分类：style / material
     */
    private String category;
}
