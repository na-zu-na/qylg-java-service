package com.cc.qylgjavaservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.productsDTO.CartOperationDTO;
import com.cc.qylgjavaservice.dto.productsDTO.MassProductsQueryDTO;
import com.cc.qylgjavaservice.dto.productsDTO.ProductsDTO;
import com.cc.qylgjavaservice.dto.productsDTO.ProductsReviewDTO;
import com.cc.qylgjavaservice.entity.Products;
import com.cc.qylgjavaservice.entity.ShoppingCart;
import com.cc.qylgjavaservice.mapper.ProductsMapper;
import com.cc.qylgjavaservice.service.ProductsService;
import com.cc.qylgjavaservice.utils.UserContext;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.cc.qylgjavaservice.utils.RedisConstants.HOT_PRODUCT_KEY;
import static com.cc.qylgjavaservice.utils.RedisConstants.PRODUCT_KEY;


@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper,Products> implements ProductsService {
    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Result<List<ProductsDTO>> getShopRecommend() {
        RBucket<List<Products>> hotProductsRBucket=redissonClient.getBucket(HOT_PRODUCT_KEY);
        RBucket<List<ProductsDTO>> productsRBucket=redissonClient.getBucket(PRODUCT_KEY);
        //查缓存
        if (productsRBucket.isExists()){
            return Result.success(productsRBucket.get());
        }

        List<Products> allProducts = productsMapper.selectList(
                new LambdaQueryWrapper<Products>()
                        .eq(Products::getStatus, 1)
        );

        //找热门
        List<Products> hotProducts = productsMapper.selectList(new LambdaQueryWrapper<Products>().
                orderByDesc(Products::getTotalSales).
                last("LIMIT 20"));

        if (allProducts.isEmpty()) {
            return Result.fail(404, "没找到");
        }

        // 内存随机
        Collections.shuffle(allProducts);

        // 取前 20，防止不足 20 个报错
        int limit = Math.min(20, allProducts.size());
        List<Products> products=allProducts.subList(0, limit);

        //转化为DTO，添加hot
        List<ProductsDTO> productsDTOS=allProducts.stream().map(p -> {
            ProductsDTO pd=new ProductsDTO();
            BeanUtil.copyProperties(p,pd);
            if (hotProducts.contains(p)){
                pd.setHot(true);
            }
            return pd;
        }).toList();

        //存入redis
        productsRBucket.set(productsDTOS, Duration.ofMinutes(10));
        hotProductsRBucket.set(hotProducts,Duration.ofMinutes(10));

        return Result.success(productsDTOS);
    }

    @Override
    public Result<List<Products>> getHotProducts() {
        RBucket<List<Products>> hotProductsRBucket=redissonClient.getBucket(HOT_PRODUCT_KEY);
        if (hotProductsRBucket.isExists()){
            return Result.success(hotProductsRBucket.get());
        }

        List<Products> products = productsMapper.selectList(new LambdaQueryWrapper<Products>().
                orderByDesc(Products::getTotalSales).
                last("LIMIT 20"));

        if (!products.isEmpty()){
            hotProductsRBucket.set(products,Duration.ofMinutes(10));
            return Result.success(products);
        }
        else {
            return Result.fail(404,"没找到");
        }
    }

    @Override
    public Result<List<Products>> searchProducts(String keyword, String type) {
        List<Products> products;
        if (Objects.equals(type, "mass")){
             products= productsMapper.selectList(new LambdaQueryWrapper<Products>()
                     .eq(Products::getType,type)
                     .like(Products::getTitle, keyword));
        }
        else {
            products = productsMapper.selectList(new LambdaQueryWrapper<Products>()
                    .and(wrapper -> wrapper
                            .like(Products::getTitle, keyword)
                            .or()
                            .like(Products::getAnchor, keyword)
                            .or()
                            .like(Products::getPurpose,keyword)
                    )
            );
        }
        if (!products.isEmpty()){
            return Result.success(products);
        }
        else {
            return Result.fail(404,"没找到");
        }
    }

    @Override
    public Result<List<Products>> getMassGoodsList(MassProductsQueryDTO dto) {
        LambdaQueryWrapper<Products> wrapper = new LambdaQueryWrapper<>();

        // 只查上架商品 (status = 1)
        wrapper.eq(Products::getStatus, 1).eq(Products::getType,"mass");

        //搜索逻辑
        if (StringUtils.hasText(dto.getKeyword())) {
            String kw = dto.getKeyword().trim();
            wrapper.and(w -> w
                    .like(Products::getTitle, kw)
                    .or()
                    .like(Products::getAnchor, kw)
                    .or()
                    .like(Products::getPurpose, kw)
            );
        }

        //  排序逻辑
        String sortKey = dto.getSortKey();

        if ("totalSales".equals(sortKey)) {
            // 销量降序
            wrapper.orderByDesc(Products::getTotalSales);
        } else if ("price".equals(sortKey)) {
            // 价格排序：根据 priceOrder 决定升序还是降序
            if ("desc".equalsIgnoreCase(dto.getPriceOrder())) {
                wrapper.orderByDesc(Products::getPrice);
            } else {
                // 默认 asc
                wrapper.orderByAsc(Products::getPrice);
            }
        } else {
            wrapper.orderByDesc(Products::getId);
        }

        List<Products> products = productsMapper.selectList(wrapper);
        if (!products.isEmpty()){
            return Result.success(products);
        }
        else {
            return Result.fail(404,"没找到");
        }
    }

    @Override
    public Result<Products> getProductsDetail(Long id) {
        Products byId = this.getById(id);
        if (byId==null || byId.getStatus()==0){
            return Result.fail(1001,"商品已下架");
        }
        return Result.success(byId);
    }

    @Override
    public Result<List<ProductsReviewDTO>> getProductsReview(Long id) {
        List<ProductsReviewDTO> list=productsMapper.getProductsReview(id);

        if (list.isEmpty()){
            return Result.fail(404,"没有评论");
        }
        return Result.success(list);
    }
}
