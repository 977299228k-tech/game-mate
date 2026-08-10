package com.gamemate.service.impl;

import com.gamemate.config.AiConfig;
import com.gamemate.dto.ChatMessageDTO;
import com.gamemate.entity.ChatMessage;
import com.gamemate.mapper.ChatMessageMapper;
import com.gamemate.service.AiService;
import com.gamemate.service.ChatService;
import com.gamemate.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final AiConfig aiConfig;
    private final ChatMessageMapper chatMessageMapper;
    private final AiService aiService;

    @Override
    public List<ChatMessageVO> getChatHistory(Long userId, Long gameId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ChatMessage> messages = chatMessageMapper.findByUserIdAndGameId(userId, gameId);
        return messages.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public ChatMessageVO sendMessage(Long userId, ChatMessageDTO dto) {
        if (userId == null) {
            userId = 1L;
        }
        ChatMessage userMessage = new ChatMessage();
        userMessage.setUserId(userId);
        userMessage.setGameId(dto.getGameId());
        userMessage.setRole("user");
        userMessage.setContent(dto.getContent());
        chatMessageMapper.insert(userMessage);

        String aiResponse;
        if (aiConfig.getEnabled()) {
            List<Map<String, String>> history = getRecentHistory(userId, dto.getGameId(), 10);
            aiResponse = aiService.chat(userId, dto.getGameId(), dto.getContent(), history);
        } else {
            aiResponse = generateFallbackResponse(dto.getContent());
        }

        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setUserId(userId);
        aiMessage.setGameId(dto.getGameId());
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        chatMessageMapper.insert(aiMessage);

        return convertToVO(aiMessage);
    }

    @Override
    public List<ChatMessageVO> getRecentMessages(Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ChatMessage> messages = chatMessageMapper.findByUserId(userId, 50);
        return messages.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public ChatMessageVO analyzeScreen(Long userId, ChatMessageDTO dto, MultipartFile image) {
        if (userId == null) {
            userId = 1L;
        }
        ChatMessage userMessage = new ChatMessage();
        userMessage.setUserId(userId);
        userMessage.setGameId(dto.getGameId());
        userMessage.setRole("user");
        userMessage.setContent("[画面分析] 已上传游戏画面，请求AI分析");
        chatMessageMapper.insert(userMessage);

        String aiResponse;
        if (aiConfig.getEnabled() && image != null && !image.isEmpty()) {
            String imageBase64 = null;
            try {
                imageBase64 = Base64.getEncoder().encodeToString(image.getBytes());
                String contentType = image.getContentType();
                if (contentType == null) contentType = "image/jpeg";
                imageBase64 = "data:" + contentType + ";base64," + imageBase64;
            } catch (Exception e) {
                log.warn("图片转base64失败: {}", e.getMessage());
            }
            aiResponse = aiService.analyzeScreen(userId, dto.getGameId(), imageBase64);
        } else {
            aiResponse = "画面分析功能暂未开启。当前检测到您上传了游戏画面，建议关注当前局势，合理利用技能和资源。";
        }

        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setUserId(userId);
        aiMessage.setGameId(dto.getGameId());
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        chatMessageMapper.insert(aiMessage);

        return convertToVO(aiMessage);
    }

    @Override
    public ChatMessageVO analyzeScreenWithQuery(Long userId, ChatMessageDTO dto, MultipartFile image) {
        String imageBase64 = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageBase64 = Base64.getEncoder().encodeToString(image.getBytes());
                String contentType = image.getContentType();
                if (contentType == null) contentType = "image/jpeg";
                imageBase64 = "data:" + contentType + ";base64," + imageBase64;
            } catch (Exception e) {
                log.warn("图片转base64失败: {}", e.getMessage());
            }
        }
        return analyzeScreenWithPersonality(userId, dto, imageBase64, null);
    }

    @Override
    public ChatMessageVO analyzeScreenWithPersonality(Long userId, ChatMessageDTO dto, String imageBase64, String personality) {
        if (userId == null) {
            userId = 1L;
        }
        ChatMessage userMessage = new ChatMessage();
        userMessage.setUserId(userId);
        userMessage.setGameId(dto.getGameId());
        userMessage.setRole("user");
        String queryText = dto.getContent() != null ? dto.getContent() : "请分析这张游戏画面";
        userMessage.setContent(imageBase64 != null && !imageBase64.isEmpty() ? "[画面分析] " + queryText : queryText);
        chatMessageMapper.insert(userMessage);

        String aiResponse;
        if (aiConfig.getEnabled()) {
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                aiResponse = aiService.analyzeScreenWithPersonality(userId, dto.getGameId(), imageBase64, queryText, personality);
            } else {
                log.info("无图片，使用纯文本游戏知识回答模式");
                aiResponse = aiService.chatWithPersonality(userId, dto.getGameId(),
                        "[游戏知识问答] 请基于游戏知识回答以下问题：\n" + queryText, null, personality);
            }
        } else {
            aiResponse = "AI功能暂未开启。";
        }

        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setUserId(userId);
        aiMessage.setGameId(dto.getGameId());
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        chatMessageMapper.insert(aiMessage);

        return convertToVO(aiMessage);
    }

    @Override
    public ChatMessageVO sendMessageWithPersonality(Long userId, ChatMessageDTO dto, String personality) {
        if (userId == null) {
            userId = 1L;
        }
        ChatMessage userMessage = new ChatMessage();
        userMessage.setUserId(userId);
        userMessage.setGameId(dto.getGameId());
        userMessage.setRole("user");
        userMessage.setContent(dto.getContent());
        chatMessageMapper.insert(userMessage);

        String aiResponse;
        if (aiConfig.getEnabled()) {
            List<Map<String, String>> history = getRecentHistory(userId, dto.getGameId(), 10);
            aiResponse = aiService.chatWithPersonality(userId, dto.getGameId(), dto.getContent(), history, personality);
        } else {
            aiResponse = generateFallbackResponse(dto.getContent());
        }

        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setUserId(userId);
        aiMessage.setGameId(dto.getGameId());
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        chatMessageMapper.insert(aiMessage);

        return convertToVO(aiMessage);
    }

    @Override
    public ChatMessageVO streamMessageWithPersonality(Long userId, ChatMessageDTO dto, String personality,
                                                      Consumer<String> onDelta) {
        Long effectiveUserId = userId != null ? userId : 1L;
        List<Map<String, String>> history = getRecentHistory(effectiveUserId, dto.getGameId(), 10);

        ChatMessage userMessage = new ChatMessage();
        userMessage.setUserId(effectiveUserId);
        userMessage.setGameId(dto.getGameId());
        userMessage.setRole("user");
        userMessage.setContent(dto.getContent());
        chatMessageMapper.insert(userMessage);

        String aiResponse;
        if (aiConfig.getEnabled()) {
            aiResponse = aiService.streamChatWithPersonality(
                    effectiveUserId, dto.getGameId(), dto.getContent(), history, personality, onDelta);
        } else {
            aiResponse = generateFallbackResponse(dto.getContent());
            onDelta.accept(aiResponse);
        }

        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setUserId(effectiveUserId);
        aiMessage.setGameId(dto.getGameId());
        aiMessage.setRole("assistant");
        aiMessage.setContent(aiResponse);
        chatMessageMapper.insert(aiMessage);
        return convertToVO(aiMessage);
    }

    private List<Map<String, String>> getRecentHistory(Long userId, Long gameId, int limit) {
        if (userId == null) {
            userId = 1L;
        }
        List<ChatMessage> messages = chatMessageMapper.findByUserIdAndGameIdOrderByCreateTimeAsc(userId, gameId);
        int start = Math.max(0, messages.size() - limit);
        List<ChatMessage> recentMessages = messages.subList(start, messages.size());

        return recentMessages.stream()
                .filter(m -> "user".equals(m.getRole()) || "assistant".equals(m.getRole()))
                .map(m -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("role", m.getRole());
                    map.put("content", m.getContent());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private String generateFallbackResponse(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "你好！我是你的AI游戏助手，有什么可以帮你的吗？";
        }

        String msg = userMessage.toLowerCase();

        if (msg.contains("怎么") || msg.contains("如何")) {
            return "这是一个很好的问题！建议你先观察当前局势，合理分配资源和技能使用。需要更详细的分析吗？";
        } else if (msg.contains("装备") || msg.contains("武器")) {
            return "关于装备选择，建议根据当前游戏阶段和对手情况来决定。需要我帮你分析具体的装备搭配吗？";
        } else if (msg.contains("队友") || msg.contains("配合")) {
            return "团队配合非常重要！建议与队友保持沟通，合理分工。我可以帮你制定战术策略。";
        } else {
            return "收到你的消息！我正在为你分析局势，请继续描述你的问题或上传游戏画面。";
        }
    }

    private ChatMessageVO convertToVO(ChatMessage msg) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(msg.getId());
        vo.setUserId(msg.getUserId());
        vo.setGameId(msg.getGameId());
        vo.setRole(msg.getRole());
        vo.setContent(msg.getContent());
        vo.setCreateTime(msg.getCreateTime());
        return vo;
    }
}
