package com.cc.qylgjavaservice.dto.chatDTO;

import lombok.Data;

import java.util.List;

@Data
public class ChatStaffVO {
    private List<StaffVO> staffVOS;
    private Stats stats;

    private Long current;
    private Long total;
    private Long size;


    @Data
    public static class Stats{
              private Integer online;
              private Integer busy;
              private Integer canAccept;
              private Double totalLoad;
    }
}
