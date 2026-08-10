package com.gamemate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamemate.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT * FROM chat_message WHERE user_id = #{userId} AND game_id = #{gameId} ORDER BY create_time ASC")
    List<ChatMessage> findByUserIdAndGameId(@Param("userId") Long userId, @Param("gameId") Long gameId);

    @Select("SELECT * FROM chat_message WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<ChatMessage> findByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT * FROM chat_message WHERE user_id = #{userId} AND game_id = #{gameId} ORDER BY create_time ASC")
    List<ChatMessage> findByUserIdAndGameIdOrderByCreateTimeAsc(@Param("userId") Long userId, @Param("gameId") Long gameId);
}
