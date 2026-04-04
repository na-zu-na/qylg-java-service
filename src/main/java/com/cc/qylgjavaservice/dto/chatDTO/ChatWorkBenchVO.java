package com.cc.qylgjavaservice.dto.chatDTO;

import com.cc.qylgjavaservice.dto.OrderDTO.CustomOrderListDTO;
import lombok.Data;

import java.util.List;

@Data
public class ChatWorkBenchVO {
    private List<ChatMySessions> mySessions;
}
