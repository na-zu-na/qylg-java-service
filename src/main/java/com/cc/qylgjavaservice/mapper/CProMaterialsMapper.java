package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.qylgjavaservice.entity.CustomProMaterials;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CProMaterialsMapper extends BaseMapper<CustomProMaterials> {
    @Select("SELECT material_id FROM custom_pro_materials WHERE products_id = #{productId}")
    List<Long> selectMaterialIdsByProductId(Long productId);
}
