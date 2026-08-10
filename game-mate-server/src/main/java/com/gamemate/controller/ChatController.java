package com.gamemate.controller;

import com.gamemate.common.Result;
import com.gamemate.dto.ChatMessageDTO;
import com.gamemate.dto.ClientAiConfigDTO;
import com.gamemate.service.ChatService;
import com.gamemate.vo.ChatMessageVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/messages")
    public Result<List<ChatMessageVO>> getChatMessages(
            HttpServletRequest request,
            @RequestParam Long gameId) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(chatService.getChatHistory(userId, gameId));
    }

    @PostMapping("/messages")
    public Result<ChatMessageVO> sendMessage(
            HttpServletRequest request,
            @Valid @RequestBody ChatMessageDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(chatService.sendMessage(userId, dto));
    }

    @PostMapping("/messages-with-personality")
    public Result<ChatMessageVO> sendMessageWithPersonality(
            HttpServletRequest request,
            @RequestBody Map<String, Object> payload) {
        Long userId = (Long) request.getAttribute("userId");
        
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setGameId(((Number) payload.get("gameId")).longValue());
        dto.setRole((String) payload.getOrDefault("role", "user"));
        dto.setContent((String) payload.get("content"));
        
        String personality = (String) payload.get("personality");
        
        return Result.success(chatService.sendMessageWithPersonality(
                userId, dto, personality, extractClientAiConfig(payload)));
    }

    @PostMapping(value = "/messages-with-personality/stream", produces = "text/event-stream")
    public SseEmitter streamMessageWithPersonality(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody Map<String, Object> payload) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/event-stream;charset=UTF-8");
        Long userId = (Long) request.getAttribute("userId");
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setGameId(payload.get("gameId") == null ? null : Long.valueOf(payload.get("gameId").toString()));
        dto.setRole((String) payload.getOrDefault("role", "user"));
        dto.setContent((String) payload.get("content"));
        String personality = (String) payload.get("personality");
        ClientAiConfigDTO clientConfig = extractClientAiConfig(payload);

        SseEmitter emitter = new SseEmitter(130_000L);
        CompletableFuture.runAsync(() -> {
            try {
                ChatMessageVO result = chatService.streamMessageWithPersonality(
                        userId, dto, personality, clientConfig, chunk -> sendDelta(emitter, chunk));
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data(Map.of("id", result.getId(), "content", result.getContent())));
                emitter.complete();
            } catch (Exception e) {
                log.warn("流式聊天中断: {}", e.getMessage());
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("message", e.getMessage() == null ? "流式聊天失败" : e.getMessage())));
                } catch (IOException ignored) {
                    // 客户端主动中断时连接已经关闭，无需再次写入。
                }
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    private void sendDelta(SseEmitter emitter, String chunk) {
        try {
            emitter.send(SseEmitter.event().name("delta").data(Map.of("content", chunk)));
        } catch (IOException e) {
            throw new RuntimeException("客户端已断开流式连接", e);
        }
    }

    @GetMapping("/recent")
    public Result<List<ChatMessageVO>> getRecentMessages(
            HttpServletRequest request,
            @RequestParam(required = false) Long gameId,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(chatService.getRecentMessages(userId));
    }

    @PostMapping("/analyze")
    public Result<ChatMessageVO> analyzeScreen(
            HttpServletRequest request,
            @RequestParam Long gameId,
            @RequestParam("image") MultipartFile image) {
        Long userId = (Long) request.getAttribute("userId");
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setGameId(gameId);
        dto.setRole("user");
        dto.setContent("画面分析请求");
        return Result.success(chatService.analyzeScreen(userId, dto, image));
    }

    @PostMapping("/analyze-with-query")
    public Result<ChatMessageVO> analyzeScreenWithQuery(
            HttpServletRequest request,
            @RequestParam Long gameId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam("image") MultipartFile image) {
        Long userId = (Long) request.getAttribute("userId");
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setGameId(gameId);
        dto.setRole("user");
        dto.setContent(content != null ? content : "请分析这张游戏画面");
        return Result.success(chatService.analyzeScreenWithQuery(userId, dto, image));
    }

    @PostMapping("/analyze-with-personality")
    public Result<ChatMessageVO> analyzeScreenWithPersonality(
            HttpServletRequest request,
            @RequestBody Map<String, Object> data) {
        Long userId = (Long) request.getAttribute("userId");
        
        Long gameId = data.get("gameId") != null ? Long.valueOf(data.get("gameId").toString()) : null;
        String imageBase64 = (String) data.get("imageBase64");
        String content = (String) data.get("content");
        String personality = (String) data.get("personality");
        
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setGameId(gameId);
        dto.setRole("user");
        dto.setContent(content != null ? content : "请分析这张游戏画面");
        
        return Result.success(chatService.analyzeScreenWithPersonality(
                userId, dto, imageBase64, personality, extractClientAiConfig(data)));
    }

    private ClientAiConfigDTO extractClientAiConfig(Map<String, Object> payload) {
        Object rawConfig = payload.get("clientAiConfig");
        if (!(rawConfig instanceof Map<?, ?> configMap)) {
            return null;
        }
        ClientAiConfigDTO config = new ClientAiConfigDTO();
        config.setApiUrl(stringValue(configMap.get("apiUrl")));
        config.setApiKey(stringValue(configMap.get("apiKey")));
        config.setModel(stringValue(configMap.get("model")));
        return config;
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
