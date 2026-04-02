package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.chatDTO.SessionDTO;
import com.cc.qylgjavaservice.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    Long ExistingConversationId(Long senderId, Long receiverId);

    Page<SessionDTO> selectSessionPage(Page<SessionDTO> page,
                                       @Param("keyword") String keyword,
                                       @Param("status") Integer status,
                                       @Param("onlineUserIds") List<Long> onlineUserIds,
                                       @Param("offlineUserIds") List<Long> offlineUserIds,
                                       @Param("includeNullUser") boolean includeNullUser);

    List<Long> selectSessionUserIds(@Param("keyword") String keyword);
}
