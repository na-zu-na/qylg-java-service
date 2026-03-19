package com.cc.qylgjavaservice.service;

import com.cc.qylgjavaservice.dto.productsDTO.ProductDocument;
import com.cc.qylgjavaservice.entity.Products;

public interface ProductSyncService {
    /**
     * 全量同步（初始化用）
     */
    public void syncAll();

    /**
     * 单个同步（新增/修改时调用）
     */
    public void syncOne(Products product);

    /**
     * 删除同步
     */
    public void delete(Long id);

    /**
     * Products → ES
     */
    public ProductDocument toDoc(Products p);
}
