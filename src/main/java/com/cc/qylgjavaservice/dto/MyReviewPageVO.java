package com.cc.qylgjavaservice.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class MyReviewPageVO {

    private Integer commentCount;

    private Integer buyCount;

    private Page<MyReviewDTO> page;
}
