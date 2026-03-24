package com.cc.qylgjavaservice.dto.userDTO;

import com.cc.qylgjavaservice.entity.Users;
import lombok.Data;

import java.util.List;

@Data
public class AdminUserListDTO {
    private List<Users> users;

    private Stats stats;

    @Data
    public static class Stats{
        private int totalUsers;
        private int activeUsers;
        private int disabledUsers;
        private int riskyUsers;
    }
}


