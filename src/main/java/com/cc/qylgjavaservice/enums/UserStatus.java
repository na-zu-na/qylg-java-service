package com.cc.qylgjavaservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus implements IEnum<Integer> {
    BANNED(0),
    NORMAL(1);

    @EnumValue
    private final int status_code;

    @Override
    public Integer getValue() {
        return this.status_code;
    }
}
