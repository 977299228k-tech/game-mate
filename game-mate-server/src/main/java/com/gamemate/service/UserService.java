package com.gamemate.service;

import com.gamemate.dto.LoginDTO;
import com.gamemate.dto.RegisterDTO;
import com.gamemate.vo.UserVO;

public interface UserService {

    UserVO login(LoginDTO loginDTO);

    UserVO register(RegisterDTO registerDTO);

    UserVO getUserInfo(Long userId);

    UserVO updateUserInfo(Long userId, UserVO userVO);

    void addBalance(Long userId, Integer hours);

    void deductBalance(Long userId, Integer hours);
}