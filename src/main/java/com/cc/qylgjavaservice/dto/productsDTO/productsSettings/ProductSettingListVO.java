package com.cc.qylgjavaservice.dto.productsDTO.productsSettings;

import lombok.Data;

import java.util.List;

@Data
public class ProductSettingListVO {

    /**
     * 顶部统计
     */
    private ProductSettingStatsVO stats;

    /**
     * 当前页
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 列表数据
     */
    private List<ProductSettingItemVO> records;
}
