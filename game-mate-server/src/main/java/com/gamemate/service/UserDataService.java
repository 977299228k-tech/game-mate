package com.gamemate.service;

import com.gamemate.entity.*;
import com.gamemate.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataService {

    private final UserMapper userMapper;
    private final CustomGameMapper customGameMapper;
    private final UserSettingsMapper userSettingsMapper;
    private final HighlightMapper highlightMapper;
    private final UserExtraServiceMapper userExtraServiceMapper;

    public User getUserData(Long userId) {
        return userMapper.selectById(userId);
    }

    public List<CustomGame> getCustomGames(Long userId) {
        return customGameMapper.findByUserId(userId);
    }

    @Transactional
    public CustomGame addCustomGame(Long userId, String name, String genre, String icon, String color, String description) {
        CustomGame game = new CustomGame();
        game.setUserId(userId);
        game.setName(name);
        game.setGenre(genre);
        game.setIcon(icon);
        game.setColor(color);
        game.setDescription(description);
        customGameMapper.insert(game);
        return game;
    }

    @Transactional
    public void deleteCustomGame(Long userId, Long gameId) {
        CustomGame game = customGameMapper.selectById(gameId);
        if (game == null) {
            throw new RuntimeException("自定义游戏不存在");
        }
        if (!game.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此游戏");
        }
        customGameMapper.deleteById(gameId);
    }

    public UserSettings getSettings(Long userId) {
        if (userId == null) {
            return null;
        }
        UserSettings settings = userSettingsMapper.findByUserId(userId);
        if (settings == null) {
            settings = new UserSettings();
            settings.setUserId(userId);
            settings.setPersonality("friendly");
            settings.setVoice("default");
            settings.setMemoryEnabled(1);
            settings.setEmotionEnabled(1);
            settings.setTacticEnabled(0);
            settings.setGuideEnabled(1);
            userSettingsMapper.insert(settings);
        }
        return settings;
    }

    @Transactional
    public UserSettings updateSettings(Long userId, UserSettings settings) {
        UserSettings existing = userSettingsMapper.findByUserId(userId);
        if (existing == null) {
            settings.setUserId(userId);
            userSettingsMapper.insert(settings);
        } else {
            settings.setId(existing.getId());
            settings.setUserId(userId);
            userSettingsMapper.updateById(settings);
        }
        return userSettingsMapper.findByUserId(userId);
    }

    public List<Highlight> getHighlights(Long userId) {
        return highlightMapper.findByUserId(userId);
    }

    @Transactional
    public void deleteHighlight(Long userId, Long highlightId) {
        Highlight highlight = highlightMapper.selectById(highlightId);
        if (highlight == null) {
            throw new RuntimeException("高光不存在");
        }
        if (!highlight.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此高光");
        }
        highlightMapper.deleteById(highlightId);
    }

    public List<UserExtraService> getUserExtraServices(Long userId) {
        return userExtraServiceMapper.findByUserId(userId);
    }

    @Transactional
    public UserExtraService purchaseExtraService(Long userId, Long extraId, Integer hours, java.math.BigDecimal price) {
        UserExtraService existing = userExtraServiceMapper.findByUserIdAndExtraId(userId, extraId);
        if (existing != null) {
            existing.setTotalHours(existing.getTotalHours() + hours);
            existing.setPaidPrice(existing.getPaidPrice().add(price));
            userExtraServiceMapper.updateById(existing);
            return existing;
        }
        UserExtraService service = new UserExtraService();
        service.setUserId(userId);
        service.setExtraId(extraId);
        service.setTotalHours(hours);
        service.setUsedHours(0);
        service.setPaidPrice(price);
        userExtraServiceMapper.insert(service);
        return service;
    }

    @Transactional
    public void deductExtraServiceHours(Long userId, Long extraId, double hours) {
        UserExtraService service = userExtraServiceMapper.findByUserIdAndExtraId(userId, extraId);
        if (service != null) {
            int usedHours = (int) Math.ceil(hours);
            service.setUsedHours(Math.min(service.getTotalHours(), service.getUsedHours() + usedHours));
            userExtraServiceMapper.updateById(service);
        }
    }
}
