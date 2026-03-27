package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.qylgjavaservice.dto.productsDTO.productsSettings.ProductSettingItemVO;
import com.cc.qylgjavaservice.entity.Styles;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StylesMapper extends BaseMapper<Styles> {
    Long countAll(@Param("status") Integer status);

    Long countPage(@Param("keyword") String keyword,
                   @Param("status") Integer status);

    List<ProductSettingItemVO> selectPage(@Param("keyword") String keyword,
                                          @Param("status") Integer status,
                                          @Param("offset") Long offset,
                                          @Param("pageSize") Integer pageSize);

    List<ProductSettingItemVO> selectUnionPage(@Param("keyword") String keyword,
                                               @Param("status") Integer status,
                                               @Param("offset") Long offset,
                                               @Param("pageSize") Integer pageSize);
}
