package com.gamemate.controller;

import com.gamemate.common.Result;
import com.gamemate.dto.GameCreateDTO;
import com.gamemate.service.GameService;
import com.gamemate.vo.GameVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/list")
    public Result<List<GameVO>> getGameList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(gameService.getGameList(userId));
    }

    @GetMapping("/preset")
    public Result<List<GameVO>> getPresetGames() {
        return Result.success(gameService.getPresetGames());
    }

    @GetMapping("/custom")
    public Result<List<GameVO>> getCustomGames(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(gameService.getCustomGames(userId));
    }

    @PostMapping("/custom")
    public Result<GameVO> addCustomGame(
            HttpServletRequest request,
            @Valid @RequestBody GameCreateDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(gameService.addCustomGame(userId, dto, null));
    }

    @DeleteMapping("/custom/{gameId}")
    public Result<Void> deleteCustomGame(HttpServletRequest request, @PathVariable Long gameId) {
        Long userId = (Long) request.getAttribute("userId");
        gameService.deleteCustomGame(userId, gameId);
        return Result.success();
    }

    @PostMapping("/icon")
    public Result<String> uploadIcon(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(gameService.uploadIcon(userId, null, file));
    }

    @PostMapping("/icons")
    public Result<List<String>> uploadIcons(
            HttpServletRequest request,
            @RequestParam("files") MultipartFile[] files) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(gameService.uploadIcons(userId, files));
    }
}
