package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.qylgjavaservice.dto.productsDTO.productsSettings.IdCountVO;
import com.cc.qylgjavaservice.entity.CustomProStyles;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CProStylesMapper extends BaseMapper<CustomProStyles> {
    @Select("SELECT style_id FROM custom_pro_styles WHERE product_id = #{productId}")
    List<Long> selectStyleIdsByProductId(Long productId);

    List<IdCountVO> countByStyleIds(@Param("ids") List<Long> ids);
}
