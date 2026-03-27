package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.dto.productsDTO.productsSettings.*;
import com.cc.qylgjavaservice.entity.CustomProStyles;
import com.cc.qylgjavaservice.entity.Materials;
import com.cc.qylgjavaservice.entity.Products;
import com.cc.qylgjavaservice.entity.Styles;
import com.cc.qylgjavaservice.mapper.CProMaterialsMapper;
import com.cc.qylgjavaservice.mapper.CProStylesMapper;
import com.cc.qylgjavaservice.mapper.MaterialMapper;
import com.cc.qylgjavaservice.mapper.StylesMapper;
import com.cc.qylgjavaservice.service.ProductSettingsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductSettingsServiceImpl implements ProductSettingsService {

    @Autowired
    private StylesMapper stylesMapper;

    @Autowired
    private MaterialMapper materialsMapper;

    @Autowired
    private CProStylesMapper customProStylesMapper;

    @Autowired
    private CProMaterialsMapper customProMaterialsMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Override
    public Result<ProductSettingListVO> listSettings(ProductSettingQueryDTO dto) {
        if (dto == null) {
            dto = new ProductSettingQueryDTO();
        }

        long pageNum = dto.getPage() == null || dto.getPage() < 1 ? 1L : dto.getPage();
        long pageSize = dto.getPageSize() == null || dto.getPageSize() < 1 ? 10L : dto.getPageSize();
        String keyword = dto.getKeyword();
        Integer status = dto.getStatus();
        String category = dto.getCategory();

        if (category == null || category.isBlank()) {
            category = "style";
        }

        if (!"style".equals(category) && !"material".equals(category)) {
            return Result.fail(400, "category参数错误，只能为style或material");
        }

        ProductSettingListVO vo = new ProductSettingListVO();

        //统计数据
        vo.setStats(buildStats());

        if ("style".equals(category)) {
            return Result.success(fillStylePage(vo, pageNum, pageSize, keyword, status));
        } else {
            return Result.success(fillMaterialPage(vo, pageNum, pageSize, keyword, status));
        }
    }

    @Override
    public Result<Void> updateMaterialOrStyleStatus(Long id, Integer status, String type) {
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

        if (Objects.equals(type, "style")){
            // 2. 判断是否存在
            Styles styles = stylesMapper.selectById(id);
            if (styles == null) {
                return Result.fail(404, "样式不存在");
            }

            // 3. 更新
            styles.setStatus(status);
            int rows = stylesMapper.updateById(styles);

            return rows > 0 ? Result.success() : Result.fail("样式状态更新失败");
        }

        else if (Objects.equals(type,"material")){
            Materials materials = materialsMapper.selectById(id);
            if (materials==null){
                return Result.fail(404,"材质不存在");
            }

            materials.setStatus(status);
            int rows=materialsMapper.updateById(materials);
            return rows > 0 ? Result.success() : Result.fail("材质状态更新失败");
        }

        return Result.fail("传入type错误");
    }

    @Override
    public Result<Long> addProductSettings(ProductsSettingsDTO dto) {
        if (dto==null){
            return Result.fail(400,"请求体不能为空");
        }

        String type = dto.getType();
        if (type==null){
            return Result.fail(400,"类型不能为空");
        }

        if (Objects.equals(type, "style")){
            Styles styles=new Styles();
            styles.setName(dto.getName());
            styles.setStatus(dto.getStatus());
            styles.setDescription(dto.getDescription());
            stylesMapper.insert(styles);
            return Result.success(styles.getId());
        }

        else if (Objects.equals(type,"material")){
            Materials materials=new Materials();
            materials.setName(dto.getName());
            materials.setStatus(dto.getStatus());
            materials.setDescription(dto.getDescription());
            materialsMapper.insert(materials);
            return Result.success(materials.getId());
        }

        return Result.fail(400,"请求类型错误");
    }

    @Override
    public Result<Long> editProductSettings(ProductsSettingsDTO dto) {
        if (dto==null){
            return Result.fail(400,"请求体不能为空");
        }

        String type = dto.getType();
        if (type==null){
            return Result.fail(400,"类型不能为空");
        }

        if (Objects.equals(type, "style")){
            Styles styles = stylesMapper.selectById(dto.getId());
            if (styles==null){
                return Result.fail(404,"无效样式");
            }

            styles.setName(dto.getName());
            styles.setStatus(dto.getStatus());
            styles.setDescription(dto.getDescription());

            int i = stylesMapper.updateById(styles);
            if (i<1){
                return Result.fail(500,"更新错误");
            }
            return Result.success(styles.getId());
        }

        else if (Objects.equals(type,"material")){
            Materials materials = materialsMapper.selectById(dto.getId());
            if (materials==null){
                return Result.fail(404,"无效材质");
            }

            materials.setName(dto.getName());
            materials.setStatus(dto.getStatus());
            materials.setDescription(dto.getDescription());
            int i = materialsMapper.updateById(materials);
            if (i<1){
                return Result.fail(500,"更新错误");
            }
            return Result.success(materials.getId());
        }

        return Result.fail(400,"请求类型错误");
    }

    private ProductSettingStatsVO buildStats() {
        ProductSettingStatsVO stats = new ProductSettingStatsVO();

        stats.setTotalStyles(stylesMapper.selectCount(null));
        stats.setEnabledStyles(stylesMapper.selectCount(
                new LambdaQueryWrapper<Styles>().eq(Styles::getStatus, 0)
        ));
        stats.setDisabledStyles(stylesMapper.selectCount(
                new LambdaQueryWrapper<Styles>().eq(Styles::getStatus, 1)
        ));

        stats.setTotalMaterials(materialsMapper.selectCount(null));
        stats.setEnabledMaterials(materialsMapper.selectCount(
                new LambdaQueryWrapper<Materials>().eq(Materials::getStatus, 0)
        ));
        stats.setDisabledMaterials(materialsMapper.selectCount(
                new LambdaQueryWrapper<Materials>().eq(Materials::getStatus, 1)
        ));

        return stats;
    }

    private ProductSettingListVO fillStylePage(ProductSettingListVO vo,
                                               long pageNum,
                                               long pageSize,
                                               String keyword,
                                               Integer status) {

        Page<Styles> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Styles> wrapper = new LambdaQueryWrapper<>();

        //构建查询
        wrapper.eq(status != null, Styles::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(Styles::getName, keyword)
                        .or()
                        .like(Styles::getDescription, keyword)
                        .or()
                        .apply("CAST(id AS CHAR) LIKE CONCAT('%',{0},'%')", keyword)
                )
                .orderByDesc(Styles::getId);

        Page<Styles> stylePage = stylesMapper.selectPage(page, wrapper);
        List<Styles> styleRecords = stylePage.getRecords();

        Map<Long, Long> relationCountMap;
        if (styleRecords != null && !styleRecords.isEmpty()) {
            //构建id
            List<Long> ids = styleRecords.stream().map(Styles::getId).toList();
            List<IdCountVO> countList = customProStylesMapper.countByStyleIds(ids);
            relationCountMap = countList.stream()
                    .collect(Collectors.toMap(IdCountVO::getId, IdCountVO::getCount));
        } else {
            relationCountMap = Collections.emptyMap();
        }

        List<ProductSettingItemVO> records = null;
        if (styleRecords != null) {
            records = styleRecords.stream().map(item -> {
                ProductSettingItemVO itemVO = new ProductSettingItemVO();
                itemVO.setId(item.getId());
                itemVO.setName(item.getName());
                itemVO.setDescription(item.getDescription());
                itemVO.setRelationCount(relationCountMap.getOrDefault(item.getId(), 0L));
                itemVO.setType("style");
                itemVO.setStatus(item.getStatus());
                itemVO.setCreatedAt(item.getCreatedAt() == null ? null : item.getCreatedAt().format(TIME_FORMATTER));
                return itemVO;
            }).toList();
        }

        vo.setCurrent(stylePage.getCurrent());
        vo.setSize(stylePage.getSize());
        vo.setTotal(stylePage.getTotal());
        vo.setRecords(records);
        return vo;
    }

    private ProductSettingListVO fillMaterialPage(ProductSettingListVO vo,
                                                  long pageNum,
                                                  long pageSize,
                                                  String keyword,
                                                  Integer status) {

        Page<Materials> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Materials> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Materials::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(Materials::getName, keyword)
                        .or()
                        .like(Materials::getDescription, keyword)
                        .or()
                        .apply("CAST(id AS CHAR) LIKE CONCAT('%',{0},'%')", keyword)
                )
                .orderByDesc(Materials::getId);

        Page<Materials> materialPage = materialsMapper.selectPage(page, wrapper);
        List<Materials> materialRecords = materialPage.getRecords();

        Map<Long, Long> relationCountMap;
        if (materialRecords != null && !materialRecords.isEmpty()) {
            List<Long> ids = materialRecords.stream().map(Materials::getId).toList();
            List<IdCountVO> countList = customProMaterialsMapper.countByMaterialIds(ids);
            relationCountMap = countList.stream()
                    .collect(Collectors.toMap(IdCountVO::getId, IdCountVO::getCount));
        } else {
            relationCountMap = Collections.emptyMap();
        }

        List<ProductSettingItemVO> records = null;
        if (materialRecords != null) {
            records = materialRecords.stream().map(item -> {
                ProductSettingItemVO itemVO = new ProductSettingItemVO();
                itemVO.setId(item.getId());
                itemVO.setName(item.getName());
                itemVO.setDescription(item.getDescription());
                itemVO.setRelationCount(relationCountMap.getOrDefault(item.getId(), 0L));
                itemVO.setType("material");
                itemVO.setStatus(item.getStatus());
                itemVO.setCreatedAt(item.getCreatedAt() == null ? null : item.getCreatedAt().format(TIME_FORMATTER));
                return itemVO;
            }).toList();
        }

        vo.setCurrent(materialPage.getCurrent());
        vo.setSize(materialPage.getSize());
        vo.setTotal(materialPage.getTotal());
        vo.setRecords(records);
        return vo;
    }
}
