package com.gamemate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamemate.entity.CustomGame;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomGameMapper extends BaseMapper<CustomGame> {

    @Select("SELECT * FROM custom_game WHERE user_id = #{userId}")
    List<CustomGame> findByUserId(@Param("userId") Long userId);
}
