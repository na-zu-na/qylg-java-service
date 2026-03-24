package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.qylgjavaservice.dto.userDTO.AdminUserListDTO;
import com.cc.qylgjavaservice.entity.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<Users> {
    @Select("""
    SELECT
        COUNT(*) AS totalUsers,
        SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS activeUsers,
        SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS disabledUsers
    FROM users
""")
    AdminUserListDTO.Stats getUserStats();
}
