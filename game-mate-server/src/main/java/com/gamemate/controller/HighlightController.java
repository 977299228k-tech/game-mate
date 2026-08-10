package com.gamemate.controller;

import com.gamemate.common.Result;
import com.gamemate.service.HighlightService;
import com.gamemate.vo.HighlightVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/highlight")
@RequiredArgsConstructor
public class HighlightController {

    private final HighlightService highlightService;

    @GetMapping("/list")
    public Result<List<HighlightVO>> getHighlightList(
            HttpServletRequest request,
            @RequestParam(required = false) Long gameId) {
        Long userId = (Long) request.getAttribute("userId");
        if (gameId != null) {
            return Result.success(highlightService.getHighlightListByGame(userId, gameId));
        }
        return Result.success(highlightService.getHighlightList(userId));
    }

    @PostMapping
    public Result<HighlightVO> uploadHighlight(
            HttpServletRequest request,
            @RequestParam Long gameId,
            @RequestParam(required = false) String title,
            @RequestParam("video") MultipartFile video,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(highlightService.uploadHighlight(userId, gameId, title, video));
    }

    @DeleteMapping("/{highlightId}")
    public Result<Void> deleteHighlight(
            HttpServletRequest request,
            @PathVariable Long highlightId) {
        Long userId = (Long) request.getAttribute("userId");
        highlightService.deleteHighlight(userId, highlightId);
        return Result.success();
    }
}
