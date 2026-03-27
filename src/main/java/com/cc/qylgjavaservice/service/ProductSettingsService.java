package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.productsDTO.productsSettings.ProductSettingListVO;
import com.cc.qylgjavaservice.dto.productsDTO.productsSettings.ProductSettingQueryDTO;
import com.cc.qylgjavaservice.dto.productsDTO.productsSettings.ProductsSettingsDTO;

public interface ProductSettingsService {
    Result<ProductSettingListVO> listSettings(ProductSettingQueryDTO dto);

    Result<Void> updateMaterialOrStyleStatus(Long id, Integer status, String type);

    Result<Long> addProductSettings(ProductsSettingsDTO dto);

    Result<Long> editProductSettings(ProductsSettingsDTO dto);
}
