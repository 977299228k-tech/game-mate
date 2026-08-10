package com.gamemate.controller;

import com.gamemate.common.Result;
import com.gamemate.dto.LoginDTO;
import com.gamemate.dto.RegisterDTO;
import com.gamemate.service.UserService;
import com.gamemate.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("【UserController】登录请求 - account: {}", loginDTO.getAccount());
        try {
            UserVO userVO = userService.login(loginDTO);
            log.info("【UserController】登录成功 - userId: {}, nickname: {}", userVO.getId(), userVO.getNickname());
            return Result.success(userVO);
        } catch (Exception e) {
            log.error("【UserController】登录失败 - account: {}, error: {}", loginDTO.getAccount(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        log.info("【UserController】注册请求 - phone: {}, nickname: {}, email: {}", 
                 registerDTO.getPhone(), registerDTO.getNickname(), registerDTO.getEmail());
        try {
            UserVO userVO = userService.register(registerDTO);
            log.info("【UserController】注册成功 - userId: {}, phone: {}", userVO.getId(), userVO.getPhone());
            return Result.success(userVO);
        } catch (Exception e) {
            log.error("【UserController】注册失败 - phone: {}, error: {}", registerDTO.getPhone(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("【UserController】获取用户信息 - userId: {}", userId);
        try {
            UserVO userVO = userService.getUserInfo(userId);
            log.info("【UserController】获取用户信息成功 - userId: {}", userId);
            return Result.success(userVO);
        } catch (Exception e) {
            log.error("【UserController】获取用户信息失败 - userId: {}, error: {}", userId, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/info")
    public Result<UserVO> updateUserInfo(HttpServletRequest request, @RequestBody UserVO userVO) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("【UserController】更新用户信息 - userId: {}", userId);
        try {
            UserVO updated = userService.updateUserInfo(userId, userVO);
            log.info("【UserController】更新用户信息成功 - userId: {}", userId);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("【UserController】更新用户信息失败 - userId: {}, error: {}", userId, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/balance/deduct")
    public Result<Void> deductBalance(HttpServletRequest request, @RequestParam Integer hours) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("【UserController】扣减余额 - userId: {}, hours: {}", userId, hours);
        try {
            userService.deductBalance(userId, hours);
            log.info("【UserController】扣减余额成功 - userId: {}, hours: {}", userId, hours);
            return Result.success();
        } catch (Exception e) {
            log.error("【UserController】扣减余额失败 - userId: {}, error: {}", userId, e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
