package com.cc.qylgjavaservice.controller;


import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.productsDTO.*;
import com.cc.qylgjavaservice.dto.productsDTO.productsSettings.ProductSettingListVO;
import com.cc.qylgjavaservice.dto.productsDTO.productsSettings.ProductSettingQueryDTO;
import com.cc.qylgjavaservice.dto.productsDTO.productsSettings.ProductsSettings;
import com.cc.qylgjavaservice.dto.productsDTO.productsSettings.ProductsSettingsDTO;
import com.cc.qylgjavaservice.entity.Products;
import com.cc.qylgjavaservice.service.ProductSettingsService;
import com.cc.qylgjavaservice.service.ProductSyncService;
import com.cc.qylgjavaservice.service.ProductsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop")
public class ProductsController {
    @Resource
    private ProductsService productsService;

    @Resource
    private ProductSyncService productSyncService;

    @Resource
    private ProductSettingsService productSettingsService;

    @GetMapping("/recommend")
    public Result<List<ProductsDTO>> getShopRecommend(){
        return productsService.getShopRecommend();
    }

    @GetMapping("/hot/products")
    public Result<List<ProductsDTO>> getHotProducts(){
        return productsService.getHotProducts();
    }

    @GetMapping("/products/search")
    public Result<List<Products>> searchProducts(@RequestParam(value = "keyword") String keyword,
                                                 @RequestParam(value = "type",defaultValue = "all") String type,
                                                 @RequestParam(defaultValue = "totalSales",required = false)String sortKey,
                                                 @RequestParam(defaultValue = "desc",required = false)String sortOrder){
        return productsService.searchProducts(keyword,type,sortKey,sortOrder);
    }

    @GetMapping("/products/mass-list")
    public Result<List<Products>> getMassGoodsList(MassProductsQueryDTO dto) {
        return productsService.getMassGoodsList(dto);
    }

    @GetMapping("/products/detail")
    public Result<Products> getProductsDetail(@RequestParam Long id) {
        return productsService.getProductsDetail(id);
    }

    @GetMapping("/products/{id}/reviews")
    public Result<List<ProductsReviewDTO>> getProductsReview(@PathVariable Long id) {
        return productsService.getProductsReview(id);
    }

    @GetMapping("/products/custom-list")
    public Result<List<Products>> getCustomGoodsList(MassProductsQueryDTO dto) {
        return productsService.getCustomGoodsList(dto);
    }

    @GetMapping("/products/custom/detail")
    public Result<ProductsCustomDetailDTO> getProductCustomsDetail(@RequestParam Long id) {
        return productsService.getProductCustomsDetail(id);
    }

    @GetMapping("/products/es/init")
    public void initEsDoc(){
        productSyncService.syncAll();
    }

    @GetMapping("/admin/products/mass")
    public Result<ProductsAdminDTO> getAdminProMass(@RequestParam(value = "page",defaultValue = "1") int page ,
                                                    @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                                    @RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) String keyword){
        return productsService.getAdminProMass(page,pageSize,status,keyword);
    }

    @PutMapping("/admin/products/mass/{id}")
    public Result<Void> updateProductStatus(@PathVariable Long id,
                                            @RequestParam Integer status) {
        return productsService.updateProductStatus(id, status);
    }

    @PostMapping("/admin/products/mass")
    public Result<Long> addMassProduct(@RequestBody Products dto) {
        return productsService.addMassProduct(dto);
    }

    @GetMapping("/admin/products/mass/{productId}")
    public Result<Products> getMassProductDetail(@PathVariable Long productId) {
        return productsService.getMassProductDetail(productId);
    }

    @PutMapping("/admin/products/mass")
    public Result<Long> editMassProduct(@RequestBody Products dto) {
        return productsService.editMassProduct(dto);
    }


    @GetMapping("/admin/products/custom")
    public Result<ProductsCustomAdminDTO> getAdminProCustom(@RequestParam(value = "page",defaultValue = "1") int page ,
                                                    @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                                    @RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) String keyword){
        return productsService.getAdminProCustom(page,pageSize,status,keyword);
    }

    @GetMapping("/admin/products/settings")
    public Result<ProductsSettings> getProductsSettings(){
        return productsService.getProductsSettings();
    }

    @PostMapping("/admin/products/custom")
    public Result<Long> addMassProduct(@RequestBody AddCustomProductsDTO dto) {
        return productsService.addCustomProduct(dto);
    }

    @GetMapping("/admin/custom/{templateId}")
    public Result<AddCustomProductsDTO> getCustomProductDetail(@PathVariable Long templateId) {
        return productsService.getCustomProductDetail(templateId);
    }

    @PutMapping("/admin/products/custom")
    public Result<Long> editCustomProduct(@RequestBody AddCustomProductsDTO dto) {
        return productsService.editCustomProduct(dto);
    }

    @GetMapping("/admin/products/settings/list")
    public Result<ProductSettingListVO> list(@ModelAttribute ProductSettingQueryDTO dto) {
        return productSettingsService.listSettings(dto);
    }

    @PutMapping("/admin/products/settings/{id}")
    public Result<Void> updateMaterialOrStyleStatus(@PathVariable Long id,
                                            @RequestParam Integer status,
                                                    @RequestParam String type) {
        return productSettingsService.updateMaterialOrStyleStatus(id, status,type);
    }

    @PostMapping("/admin/products/settings")
    public Result<Long> addProductSettings(@RequestBody ProductsSettingsDTO dto) {
        return productSettingsService.addProductSettings(dto);
    }

    @PutMapping("/admin/products/settings")
    public Result<Long> editProductSettings(@RequestBody ProductsSettingsDTO dto) {
        return productSettingsService.editProductSettings(dto);
    }
}
