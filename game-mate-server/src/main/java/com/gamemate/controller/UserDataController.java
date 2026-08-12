package com.gamemate.controller;

import com.gamemate.common.Result;
import com.gamemate.entity.*;
import com.gamemate.mapper.HighlightMapper;
import com.gamemate.service.UserDataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/user-data")
@RequiredArgsConstructor
public class UserDataController {

    private final UserDataService userDataService;
    private final HighlightMapper highlightMapper;

    @GetMapping("/all")
    public Result<Map<String, Object>> getAllUserData(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("获取用户全部数据 - userId: {}", userId);

        if (userId == null) {
            log.warn("userId为null，返回空数据");
            Map<String, Object> data = new HashMap<>();
            return Result.success(data);
        }

        Map<String, Object> data = new HashMap<>();

        User user = userDataService.getUserData(userId);
        if (user != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("balance", user.getBalance());
            userInfo.put("personality", user.getPersonality());
            userInfo.put("voice", user.getVoice());
            data.put("user", userInfo);
        }

        List<CustomGame> customGames = userDataService.getCustomGames(userId);
        data.put("customGames", customGames);

        UserSettings settings = userDataService.getSettings(userId);
        data.put("settings", settings);

        List<Map<String, Object>> highlights = highlightMapper.findByUserId(userId).stream().map(h -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", h.getId());
            map.put("gameId", h.getGameId());
            map.put("title", h.getTitle());
            map.put("videoUrl", h.getVideoUrl());
            map.put("thumbnail", h.getThumbnail());
            map.put("duration", h.getDuration());
            map.put("createTime", h.getCreateTime());
            return map;
        }).collect(Collectors.toList());
        data.put("highlights", highlights);

        List<UserExtraService> extraServices = userDataService.getUserExtraServices(userId);
        List<Map<String, Object>> extras = extraServices.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("extraId", e.getExtraId());
            map.put("totalHours", e.getTotalHours());
            map.put("usedHours", e.getUsedHours());
            int totalHours = e.getTotalHours() == null ? 0 : e.getTotalHours();
            int usedHours = e.getUsedHours() == null ? 0 : e.getUsedHours();
            map.put("remainingHours", Math.max(0, totalHours - usedHours));
            return map;
        }).collect(Collectors.toList());
        data.put("extraServices", extras);

        return Result.success(data);
    }

    @GetMapping("/custom-games")
    public Result<List<CustomGame>> getCustomGames(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userDataService.getCustomGames(userId));
    }

    @PostMapping("/custom-games")
    public Result<CustomGame> addCustomGame(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        String name = body.get("name");
        if (name == null || name.isBlank() || name.length() > 100) {
            return Result.error(400, "游戏名称不能为空且不能超过100个字符");
        }
        String genre = body.get("genre");
        String icon = body.get("icon");
        String color = body.get("color");
        String description = body.get("description");
        return Result.success(userDataService.addCustomGame(userId, name, genre, icon, color, description));
    }

    @DeleteMapping("/custom-games/{gameId}")
    public Result<Void> deleteCustomGame(HttpServletRequest request, @PathVariable Long gameId) {
        Long userId = (Long) request.getAttribute("userId");
        userDataService.deleteCustomGame(userId, gameId);
        return Result.success();
    }

    @GetMapping("/settings")
    public Result<UserSettings> getSettings(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userDataService.getSettings(userId));
    }

    @PutMapping("/settings")
    public Result<UserSettings> updateSettings(HttpServletRequest request, @RequestBody UserSettings settings) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userDataService.updateSettings(userId, settings));
    }

    @GetMapping("/highlights")
    public Result<List<Map<String, Object>>> getHighlights(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Highlight> highlights = highlightMapper.findByUserId(userId);
        List<Map<String, Object>> result = highlights.stream().map(h -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", h.getId());
            map.put("gameId", h.getGameId());
            map.put("title", h.getTitle());
            map.put("videoUrl", h.getVideoUrl());
            map.put("thumbnail", h.getThumbnail());
            map.put("duration", h.getDuration());
            map.put("createTime", h.getCreateTime());
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @DeleteMapping("/highlights/{highlightId}")
    public Result<Void> deleteHighlight(HttpServletRequest request, @PathVariable Long highlightId) {
        Long userId = (Long) request.getAttribute("userId");
        userDataService.deleteHighlight(userId, highlightId);
        return Result.success();
    }

    @GetMapping("/extra-services")
    public Result<List<Map<String, Object>>> getExtraServices(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<UserExtraService> services = userDataService.getUserExtraServices(userId);
        List<Map<String, Object>> result = services.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("extraId", s.getExtraId());
            map.put("totalHours", s.getTotalHours());
            map.put("usedHours", s.getUsedHours());
            int totalHours = s.getTotalHours() == null ? 0 : s.getTotalHours();
            int usedHours = s.getUsedHours() == null ? 0 : s.getUsedHours();
            map.put("remainingHours", Math.max(0, totalHours - usedHours));
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

}
