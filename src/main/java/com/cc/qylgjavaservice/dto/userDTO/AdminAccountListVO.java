package com.cc.qylgjavaservice.dto.userDTO;

import com.cc.qylgjavaservice.entity.Users;
import lombok.Data;

import java.util.List;

@Data
public class AdminAccountListVO {
    private List<Users> users;

    private Stats stats;

    private Long current;

    private Long total;

    private Long size;

    @Data
    public static class Stats{
        private Long total;

        private Long active;

        private Long disabled;

        private Long admins;
    }
}
