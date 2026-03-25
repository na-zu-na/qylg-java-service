package com.cc.qylgjavaservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ArticleStatus implements IEnum<Integer> {
    PUBLISH(0),
    OFF(1);

    @EnumValue
    private final int article_status;

    @Override
    public Integer getValue() {
        return this.article_status;
    }
}
