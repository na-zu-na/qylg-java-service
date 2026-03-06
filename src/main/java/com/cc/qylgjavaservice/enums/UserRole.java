package com.cc.qylgjavaservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole implements IEnum<Integer> {
    ADMIN(0),
    WORKER(1),
    USER(2);

    @EnumValue
    private final int role_code;

    @Override
    public Integer getValue() {
        return this.role_code;
    }
}
