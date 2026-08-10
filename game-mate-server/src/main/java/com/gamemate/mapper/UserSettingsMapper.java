package com.gamemate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamemate.entity.UserSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserSettingsMapper extends BaseMapper<UserSettings> {

    @Select("SELECT * FROM user_settings WHERE user_id = #{userId}")
    UserSettings findByUserId(@Param("userId") Long userId);
}
