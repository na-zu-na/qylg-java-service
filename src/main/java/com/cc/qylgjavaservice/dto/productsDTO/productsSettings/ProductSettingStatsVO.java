package com.cc.qylgjavaservice.dto.productsDTO.productsSettings;


import lombok.Data;

@Data
public class ProductSettingStatsVO {

    private Long totalStyles;
    private Long totalMaterials;

    private Long enabledStyles;
    private Long disabledStyles;

    private Long enabledMaterials;
    private Long disabledMaterials;
}