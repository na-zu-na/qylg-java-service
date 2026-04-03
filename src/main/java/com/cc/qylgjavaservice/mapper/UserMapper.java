package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.chatDTO.StaffVO;
import com.cc.qylgjavaservice.dto.userDTO.AdminAccountListVO;
import com.cc.qylgjavaservice.dto.userDTO.AdminUserListDTO;
import com.cc.qylgjavaservice.entity.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

    Page<Users> selectAccountList(
            Page<Users> usersPage,
            @Param("status") Integer status,
            @Param("keyword") String keyword,
            @Param("role_code") Integer role_code
    );

    @Select("""
    SELECT
        COUNT(*) AS total,
        COUNT(*) FILTER (WHERE status = 0) AS active,
        COUNT(*) FILTER (WHERE status = 1) AS disabled,
        COUNT(*) FILTER (WHERE role_code = 0) AS admins
    FROM users
""")
    AdminAccountListVO.Stats selectAccountStats();

    Page<StaffVO> selectStaffPage(Page<Object> objectPage, String keyword);
}
