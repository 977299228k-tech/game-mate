package com.gamemate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamemate.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE account = #{account}")
    User findByAccount(String account);

    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User findByPhone(String phone);

    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    @Update("UPDATE user SET balance = COALESCE(balance, 0) + #{hours} WHERE id = #{userId}")
    int incrementBalance(@Param("userId") Long userId, @Param("hours") int hours);

    @Update("UPDATE user SET balance = balance - #{hours} WHERE id = #{userId} AND balance >= #{hours}")
    int decrementBalanceIfEnough(@Param("userId") Long userId, @Param("hours") int hours);
}
