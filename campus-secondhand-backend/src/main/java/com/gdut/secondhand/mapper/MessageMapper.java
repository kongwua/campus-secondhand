package com.gdut.secondhand.mapper;

import com.gdut.secondhand.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    int insert(Message message);

    Message selectById(@Param("id") Long id);

    List<Message> selectConversation(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);

    List<Message> selectMessageList(@Param("userId") Long userId);

    int markAsRead(@Param("id") Long id);

    int markConversationAsRead(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);

    int countUnread(@Param("userId") Long userId);
}