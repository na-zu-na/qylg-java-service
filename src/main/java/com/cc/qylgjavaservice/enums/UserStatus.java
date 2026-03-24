package com.cc.qylgjavaservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus implements IEnum<Integer> {
    BANNED(1),
    NORMAL(0);

    @EnumValue
    private final int status_code;

    @Override
    public Integer getValue() {
        return this.status_code;
    }
}
