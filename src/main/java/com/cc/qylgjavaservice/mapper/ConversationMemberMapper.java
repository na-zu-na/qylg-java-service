package com.cc.qylgjavaservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cc.qylgjavaservice.dto.chatDTO.ConversationSessionsDTO;
import com.cc.qylgjavaservice.entity.ConversationMember;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConversationMemberMapper extends BaseMapper<ConversationMember> {

    List<ConversationSessionsDTO> getConversationSessionsByUserId(Long userId);
}