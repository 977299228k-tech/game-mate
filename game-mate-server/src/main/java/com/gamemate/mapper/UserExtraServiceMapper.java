package com.gamemate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamemate.entity.UserExtraService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserExtraServiceMapper extends BaseMapper<UserExtraService> {

    @Select("SELECT * FROM user_extra_service WHERE user_id = #{userId}")
    List<UserExtraService> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM user_extra_service WHERE user_id = #{userId} AND extra_id = #{extraId}")
    UserExtraService findByUserIdAndExtraId(@Param("userId") Long userId, @Param("extraId") Long extraId);
}
