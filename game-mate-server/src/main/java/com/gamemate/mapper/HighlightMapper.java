package com.gamemate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamemate.entity.Highlight;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HighlightMapper extends BaseMapper<Highlight> {

    @Select("SELECT * FROM highlight WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Highlight> findByUserId(Long userId);

    @Select("SELECT * FROM highlight WHERE user_id = #{userId} AND game_id = #{gameId} ORDER BY create_time DESC")
    List<Highlight> findByUserIdAndGameId(Long userId, Long gameId);
}