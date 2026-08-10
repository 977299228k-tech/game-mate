package com.gamemate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamemate.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE account = #{account}")
    User findByAccount(String account);

    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User findByPhone(String phone);
}