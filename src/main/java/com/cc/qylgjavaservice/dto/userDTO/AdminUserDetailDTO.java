package com.cc.qylgjavaservice.dto.userDTO;

import com.cc.qylgjavaservice.dto.chatDTO.ConversationSessionsDTO;
import com.cc.qylgjavaservice.entity.*;
import lombok.Data;

import java.util.List;

@Data
public class AdminUserDetailDTO {
    private Users users;

    private int orderCount;

    private Long totalSpend;

    private int customOrderCount;

    private int articleCount;

    private Addresses addresses;

    private List<Orders> orderHistory;

    private List<CustomOrder> customOrders;

    private List<Articles> articlesList;

    private List<ConversationSessionsDTO> conversationMemberList;
}
