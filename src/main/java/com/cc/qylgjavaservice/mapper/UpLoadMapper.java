package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.qylgjavaservice.dto.FileDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UpLoadMapper extends BaseMapper<FileDTO> {
}
