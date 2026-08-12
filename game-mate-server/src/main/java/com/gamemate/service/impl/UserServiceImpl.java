package com.gamemate.service.impl;

import com.gamemate.dto.LoginDTO;
import com.gamemate.dto.RegisterDTO;
import com.gamemate.entity.User;
import com.gamemate.mapper.UserMapper;
import com.gamemate.service.UserService;
import com.gamemate.util.JwtUtil;
import com.gamemate.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserVO login(LoginDTO loginDTO) {
        User user = userMapper.findByAccount(loginDTO.getAccount());
        if (user == null) {
            throw new RuntimeException("账号不存在");
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return convertToVO(user, true);
    }

    @Override
    public UserVO register(RegisterDTO registerDTO) {
        if (registerDTO.getPhone() == null || !registerDTO.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("两次密码不一致");
        }
        User existPhoneUser = userMapper.findByPhone(registerDTO.getPhone());
        if (existPhoneUser != null) {
            throw new RuntimeException("手机号已注册");
        }
        if (registerDTO.getEmail() != null && !registerDTO.getEmail().isBlank()
                && userMapper.findByEmail(registerDTO.getEmail()) != null) {
            throw new RuntimeException("邮箱已注册");
        }

        User user = new User();
        user.setAccount(registerDTO.getPhone());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getPhone());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setBalance(2);
        user.setPersonality("friendly");
        user.setVoice("default");
        user.setMemoryEnabled(1);
        user.setEmotionEnabled(1);
        user.setTacticEnabled(0);
        user.setGuideEnabled(1);
        userMapper.insert(user);

        return convertToVO(user, true);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToVO(user, false);
    }

    @Override
    public UserVO updateUserInfo(Long userId, UserVO userVO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (userVO.getNickname() != null) {
            user.setNickname(userVO.getNickname());
        }
        if (userVO.getPhone() != null) {
            if (!userVO.getPhone().matches("^1[3-9]\\d{9}$")) {
                throw new IllegalArgumentException("手机号格式不正确");
            }
            User existingPhoneUser = userMapper.findByPhone(userVO.getPhone());
            if (existingPhoneUser != null && !existingPhoneUser.getId().equals(userId)) {
                throw new RuntimeException("手机号已注册");
            }
            user.setPhone(userVO.getPhone());
            user.setAccount(userVO.getPhone());
        }
        if (userVO.getEmail() != null) {
            User existingEmailUser = userMapper.findByEmail(userVO.getEmail());
            if (existingEmailUser != null && !existingEmailUser.getId().equals(userId)) {
                throw new RuntimeException("邮箱已注册");
            }
            user.setEmail(userVO.getEmail());
        }
        if (userVO.getAvatar() != null) {
            user.setAvatar(userVO.getAvatar());
        }
        if (userVO.getPersonality() != null) {
            user.setPersonality(userVO.getPersonality());
        }
        if (userVO.getVoice() != null) {
            user.setVoice(userVO.getVoice());
        }
        if (userVO.getMemoryEnabled() != null) {
            user.setMemoryEnabled(userVO.getMemoryEnabled());
        }
        if (userVO.getEmotionEnabled() != null) {
            user.setEmotionEnabled(userVO.getEmotionEnabled());
        }
        if (userVO.getTacticEnabled() != null) {
            user.setTacticEnabled(userVO.getTacticEnabled());
        }
        if (userVO.getGuideEnabled() != null) {
            user.setGuideEnabled(userVO.getGuideEnabled());
        }
        userMapper.updateById(user);

        return convertToVO(user, false);
    }

    @Override
    public void addBalance(Long userId, Integer hours) {
        if (hours == null || hours <= 0) {
            throw new IllegalArgumentException("充值时长必须大于0");
        }
        if (userMapper.incrementBalance(userId, hours) == 0) {
            throw new RuntimeException("用户不存在");
        }
    }

    @Override
    public void deductBalance(Long userId, Integer hours) {
        if (hours == null || hours <= 0) {
            throw new IllegalArgumentException("扣减时长必须大于0");
        }
        int updated = userMapper.decrementBalanceIfEnough(userId, hours);
        if (updated == 1) {
            return;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        throw new RuntimeException("余额不足");
    }

    private UserVO convertToVO(User user, boolean withToken) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setAccount(user.getAccount());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setBalance(user.getBalance());
        vo.setPersonality(user.getPersonality());
        vo.setVoice(user.getVoice());
        vo.setMemoryEnabled(user.getMemoryEnabled());
        vo.setEmotionEnabled(user.getEmotionEnabled());
        vo.setTacticEnabled(user.getTacticEnabled());
        vo.setGuideEnabled(user.getGuideEnabled());
        vo.setCreateTime(user.getCreateTime());
        if (withToken) {
            vo.setToken(jwtUtil.generateToken(user.getId(), user.getAccount()));
        }
        return vo;
    }
}
