package com.cc.qylgjavaservice.dto.chatDTO;

import lombok.Data;

@Data
public class StaffVO {
    private Long id;
    private String userName;
    private Integer role;

    //0表示离线，1表示在线
    private Integer onlineStatus;
    private Double currentLoad;
    private Integer maxLoad=10;
    private boolean canAccept;
    private Double activeSessions;
}
