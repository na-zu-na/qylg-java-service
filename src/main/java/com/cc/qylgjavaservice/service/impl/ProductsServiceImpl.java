package com.cc.qylgjavaservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.productsDTO.*;
import com.cc.qylgjavaservice.entity.Products;
import com.cc.qylgjavaservice.mapper.ProductsMapper;
import com.cc.qylgjavaservice.service.ProductsService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.cc.qylgjavaservice.utils.RedisConstants.HOT_PRODUCT_KEY;
import static com.cc.qylgjavaservice.utils.RedisConstants.PRODUCT_KEY;


@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper,Products> implements ProductsService {
    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Result<List<ProductsDTO>> getShopRecommend() {
        RBucket<ProductCacheDTO> hotProductsRBucket=redissonClient.getBucket(HOT_PRODUCT_KEY,new JsonJacksonCodec());
        RBucket<ProductCacheDTO> bucket = redissonClient.getBucket(PRODUCT_KEY, new JsonJacksonCodec());

        if (bucket.isExists()) {
            return Result.success(bucket.get().getList());
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


        //转化为DTO，添加hot
        List<ProductsDTO> productsDTOS=allProducts.stream().map(p -> {
            ProductsDTO pd=new ProductsDTO();
            BeanUtil.copyProperties(p,pd);
            if (hotProducts.contains(p)){
                pd.setHot(true);
            }
            return pd;
        }).toList();

        //转化为DTO，添加hot
        List<ProductsDTO> hotProductsDTOS=hotProducts.stream().map(p -> {
            ProductsDTO pd=new ProductsDTO();
            BeanUtil.copyProperties(p,pd);
            return pd;
        }).toList();

        // 构建
        ProductCacheDTO cache = new ProductCacheDTO();
        ProductCacheDTO hotCache = new ProductCacheDTO();
        cache.setList(productsDTOS);
        hotCache.setList(hotProductsDTOS);

        //存入redis
        bucket.set(cache, Duration.ofMinutes(10));
        hotProductsRBucket.set(hotCache,Duration.ofMinutes(10));

        return Result.success(productsDTOS);
    }

    @Override
    public Result<List<ProductsDTO>> getHotProducts() {
        RBucket<ProductCacheDTO> hotProductsRBucket=redissonClient.getBucket(HOT_PRODUCT_KEY,new JsonJacksonCodec());
        if (hotProductsRBucket.isExists()){
            return Result.success(hotProductsRBucket.get().getList());
        }

        List<Products> products = productsMapper.selectList(new LambdaQueryWrapper<Products>().
                orderByDesc(Products::getTotalSales).
                last("LIMIT 20"));

        if (!products.isEmpty()){
            List<ProductsDTO> hotProductsDTOS=products.stream().map(p -> {
                ProductsDTO pd=new ProductsDTO();
                BeanUtil.copyProperties(p,pd);
                return pd;
            }).toList();

            // 构建
            ProductCacheDTO hotCache = new ProductCacheDTO();
            hotCache.setList(hotProductsDTOS);

            //存入redis
            hotProductsRBucket.set(hotCache,Duration.ofMinutes(10));

            return Result.success(hotProductsDTOS);
        }
        else {
            return Result.fail(404,"没找到");
        }
    }

    @Override
    public Result<List<Products>> searchProducts(String keyword, String type, String sorKey, String sortOrder) {
        //如果没有关键字
        if (Objects.equals(keyword, "")){
            if (type.equals("mass")) {
                MassProductsQueryDTO massProductsQueryDTO = new MassProductsQueryDTO();
                massProductsQueryDTO.setSortKey(sorKey);
                massProductsQueryDTO.setPriceOrder(sortOrder);
                return getMassGoodsList(massProductsQueryDTO);
            }
            else if (type.equals("custom")) {
                MassProductsQueryDTO massProductsQueryDTO = new MassProductsQueryDTO();
                massProductsQueryDTO.setSortKey(sorKey);
                massProductsQueryDTO.setPriceOrder(sortOrder);
                return getCustomGoodsList(massProductsQueryDTO);
            }
        }


        List<ProductDocument> docs=searchFromEs(keyword, type,sorKey,sortOrder);
        if (docs.isEmpty()){
            //db兜底
            return searchFromDb(keyword, type);
        }

        List<Products> collect = docs.stream().map(this::toProducts).toList();
        return Result.success(collect);
    }

    public List<ProductDocument> searchFromEs(String keyword, String type,String sorKey, String sortOrder) {

        try {
            SearchResponse<ProductDocument> response = elasticsearchClient.search(s -> {

                // ===== 构建 bool 查询 =====
                s.index("products_index")
                        .query(q -> q
                                .bool(b -> {

                                    // ===== 精准短语匹配（权重最高）=====
                                    b.should(sh -> sh
                                            .matchPhrase(mp -> mp
                                                    .field("title")
                                                    .query(keyword)
                                                    .boost(10.0f)
                                            )
                                    );

                                    // ===== 第二层：多字段匹配（核心召回）=====
                                    b.should(sh -> sh
                                            .multiMatch(mm -> mm
                                                    .query(keyword)
                                                    .fields(
                                                            "title^5",
                                                            "title.pinyin^3",
                                                            "anchor^2"
                                                    )
                                                    .minimumShouldMatch("60%")   //  别用95%
                                            )
                                    );

                                    // ===== 第三层：弱匹配兜底 =====
                                    b.should(sh -> sh
                                            .match(m -> m
                                                    .field("anchor")
                                                    .query(keyword)
                                                    .boost(0.5f)
                                            )
                                    );

                                    b.should(sh -> sh
                                            .term(t -> t
                                                    .field("title.keyword")
                                                    .value(keyword)
                                                    .boost(20.0f)
                                            )
                                    );

                                    // 至少命中一个 should
                                    b.minimumShouldMatch("1");

                                    // ===== filter 不参与评分 =====
                                    b.filter(f -> f
                                            .term(t -> t
                                                    .field("type")
                                                    .value(type)
                                            )
                                    );

                                    b.filter(f -> f
                                            .term(t -> t
                                                    .field("status")
                                                    .value(1)
                                            )
                                    );

                                    return b;
                                })
                        );

                        // ===== 排序逻辑 =====
                        if ("default".equals(sorKey)) {

                            s.sort(so -> so
                                    .score(sc -> sc.order(SortOrder.Desc))
                            );

                        } else {

                            s.sort(so -> so
                                    .field(f -> f
                                            .field(sorKey)
                                            .order("asc".equals(sortOrder) ? SortOrder.Asc : SortOrder.Desc)
                                    )
                            );

                            s.sort(so -> so
                                    .score(sc -> sc.order(SortOrder.Desc))
                            );
                        }

                        return s;

            }, ProductDocument.class);

            // ===== 解析结果 =====
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private Products toProducts(ProductDocument doc) {
        Products p = new Products();

        BeanUtils.copyProperties(doc, p, "price","maxPrice","minPrice");

        // 手动安全转换
        if (doc.getPrice() != null && !doc.getPrice().isEmpty()) {
            p.setPrice(new BigDecimal(doc.getPrice()));
            p.setMaxPrice(new BigDecimal(doc.getMaxPrice()));
            p.setMinPrice(new BigDecimal(doc.getMinPrice()));
        } else {
            p.setPrice(BigDecimal.ZERO); // 或 null，视业务而定
            p.setMaxPrice(BigDecimal.ZERO);
            p.setMinPrice(BigDecimal.ZERO);
        }

        return p;
    }


    public Result<List<Products>> searchFromDb(String keyword, String type) {
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

    @Override
    public Result<List<Products>> getCustomGoodsList(MassProductsQueryDTO dto) {
        LambdaQueryWrapper<Products> wrapper = new LambdaQueryWrapper<>();

        // 只查上架商品 (status = 1)
        wrapper.eq(Products::getStatus, 1).eq(Products::getType,"custom");

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
        } else if ("minPrice".equals(sortKey) || "maxPrice".equals(sortKey)) {
            // 价格排序：根据 priceOrder 决定升序还是降序
            if ("desc".equals(dto.getPriceOrder())) {
                if ("minPrice".equals(sortKey)){
                    wrapper.orderByDesc(Products::getMinPrice);
                }
                else{
                    wrapper.orderByDesc(Products::getMaxPrice);
                }
            } else {
                // 默认 asc
                if ("minPrice".equals(sortKey)){
                    wrapper.orderByAsc(Products::getMinPrice);
                }
                else{
                    wrapper.orderByAsc(Products::getMaxPrice);
                }
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
    public Result<ProductsCustomDetailDTO> getProductCustomsDetail(Long id) {
        ProductsCustomDetailDTO productsCustomDetailDTO=productsMapper.getProductCustomsDetail(id);
        if (productsCustomDetailDTO!=null){
            List<String> styleList = productsCustomDetailDTO.getStyleList();
            List<String> materialList = productsCustomDetailDTO.getMaterialList();
            if (styleList!=null){
                String style = String.join("/", styleList);
                productsCustomDetailDTO.setStyle(style);
            } else productsCustomDetailDTO.setStyle("暂无风格");

            if (materialList!=null){
                String material = String.join("/", materialList);
                productsCustomDetailDTO.setMaterial(material);
            } else productsCustomDetailDTO.setMaterial("暂无材质");

            return Result.success(productsCustomDetailDTO);
        }
        else {
            return Result.fail(404,"没查到");
        }
    }

    @Override
    public Result<ProductsAdminDTO> getAdminProMass(int page, int pageSize, Integer status, String keyword) {
        //查文章列表
        Page<ProductsDTO> productsDTOPage =new Page<>(page,pageSize);
        Page<ProductsDTO> products = productsMapper.selectAdminProductsPage(productsDTOPage,status,keyword);

        //组装page
        ProductsAdminDTO productsAdminDTO =new ProductsAdminDTO();
        productsAdminDTO.setSize(products.getSize());
        productsAdminDTO.setTotal(products.getTotal());
        productsAdminDTO.setCurrent(products.getCurrent());

        //统计参数
        ProductStatsDTO productStatsDTO=productsMapper.selectProductStats();
        productsAdminDTO.setProductStatsDTO(productStatsDTO);

        List<ProductsDTO> records = products.getRecords();
        records.forEach(i->{
            if (i.getTotalSales()>1000)
                i.setHot(true);
        });

        productsAdminDTO.setProductsDTOS(records);

        return Result.success(productsAdminDTO);
    }

    @Override
    public Result<Void> updateProductStatus(Long id, Integer status) {
        // 1. 参数校验
        if (id == null) {
            return Result.fail(400, "ID不能为空");
        }
        if (status == null) {
            return Result.fail(400, "状态不能为空");
        }
        if (status != 0 && status != 1) {
            return Result.fail(400, "状态值非法（0正常，1下架）");
        }

        // 2. 判断是否存在
        Products products = productsMapper.selectById(id);
        if (products == null) {
            return Result.fail(404, "文章不存在");
        }

        // 3. 更新
        products.setStatus(status);
        int rows = productsMapper.updateById(products);

        return rows > 0 ? Result.success() : Result.fail("文章状态更新失败");
    }

    @Override
    public Result<Long> addMassProduct(Products dto) {

        // 1. 参数校验
        if (dto == null) {
            return Result.fail(400, "请求参数不能为空");
        }
        validateParam(dto);

        // 2. DTO -> Entity
        Products product = new Products();
        product.setTitle(dto.getTitle());
        product.setCover(dto.getCover());
        product.setType(dto.getType());
        product.setPurpose(dto.getPurpose());
        product.setPrice(dto.getPrice());
        product.setTotalSales(dto.getTotalSales() == null ? 0 : dto.getTotalSales());
        product.setStatus(dto.getStatus());
        product.setStock(dto.getStock());
        product.setDescription(dto.getDescription());

        if (dto.getAnchor() != null) {
            product.setAnchor(objectMapper.writeValueAsString(dto.getAnchor()));
        }
        if (dto.getImages() != null) {
            product.setImages(dto.getImages());
        }

        // 3. 保存
        int rows = productsMapper.insert(product);
        if (rows<1) {
            return Result.fail("新增商品失败");
        }

        // 4. 返回新增后的ID
        return Result.success(product.getId());
    }

    @Override
    public Result<Products> getMassProductDetail(Long productId) {
        // 1. 参数校验
        if (productId == null) {
            return Result.fail(400, "商品ID不能为空");
        }

        // 2. 查询商品
        Products product = productsMapper.selectById(productId);
        if (product == null) {
            return Result.fail(404, "商品不存在");
        }

        // 3. 校验商品类型
        if (!"mass".equals(product.getType())) {
            return Result.fail(400, "该商品不是大众商品");
        }

        return Result.success(product);
    }

    @Override
    public Result<Long> editMassProduct(Products dto) {
        // 1. 参数校验
        if (dto == null) {
            return Result.fail(400, "请求参数不能为空");
        }
        if (dto.getId() == null) {
            return Result.fail(400, "商品ID不能为空");
        }

        validateParam(dto);

        Products products = productsMapper.selectById(dto.getId());
        if (products==null){
            return Result.fail(404,"没有找到商品");
        }

        int i = productsMapper.updateById(dto);
        if (i<1) {
            return Result.fail("修改商品失败");
        }

        // 4. 返回新增后的ID
        return Result.success(dto.getId());
    }

    private static void validateParam(Products dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            Result.fail(400, "商品名称不能为空");
            return;
        }
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            Result.fail(400, "商品类型不能为空");
            return;
        }
        if (!"mass".equals(dto.getType())) {
            Result.fail(400, "商品类型必须为 mass");
            return;
        }
        if (dto.getPrice() == null) {
            Result.fail(400, "商品价格不能为空");
            return;
        }
        if (dto.getStatus() == null) {
            Result.fail(400, "商品状态不能为空");
            return;
        }
        if (dto.getStatus() != 0 && dto.getStatus() != 1) {
            Result.fail(400, "商品状态非法，1表示启用，0表示下架");
            return;
        }
        if (dto.getStock() == null) {
            Result.fail(400, "库存不能为空");
        }
    }
}

