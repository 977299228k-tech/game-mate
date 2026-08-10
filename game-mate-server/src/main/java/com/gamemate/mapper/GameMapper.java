package com.gamemate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamemate.entity.Game;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GameMapper extends BaseMapper<Game> {

    @Select("SELECT * FROM game WHERE is_custom = 0 ORDER BY create_time ASC")
    List<Game> findAllPreset();

    @Select("SELECT * FROM game WHERE is_custom = 1 AND user_id IS NULL ORDER BY create_time DESC")
    List<Game> findAllCustom();

    @Select("SELECT * FROM game WHERE is_custom = 1 AND user_id = #{userId} ORDER BY create_time DESC")
    List<Game> findAllCustomByUserId(@Param("userId") Long userId);
}
