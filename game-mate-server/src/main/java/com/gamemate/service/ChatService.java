package com.gamemate.service;

import com.gamemate.dto.ChatMessageDTO;
import com.gamemate.dto.ClientAiConfigDTO;
import com.gamemate.vo.ChatMessageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.function.Consumer;

public interface ChatService {

    List<ChatMessageVO> getChatHistory(Long userId, Long gameId);

    ChatMessageVO sendMessage(Long userId, ChatMessageDTO dto);

    List<ChatMessageVO> getRecentMessages(Long userId);

    ChatMessageVO analyzeScreen(Long userId, ChatMessageDTO dto, MultipartFile image);

    ChatMessageVO analyzeScreenWithQuery(Long userId, ChatMessageDTO dto, MultipartFile image);

    ChatMessageVO analyzeScreenWithPersonality(Long userId, ChatMessageDTO dto, String imageBase64, String personality);

    ChatMessageVO analyzeScreenWithPersonality(Long userId, ChatMessageDTO dto, String imageBase64,
                                               String personality, ClientAiConfigDTO clientConfig);

    ChatMessageVO sendMessageWithPersonality(Long userId, ChatMessageDTO dto, String personality);

    ChatMessageVO sendMessageWithPersonality(Long userId, ChatMessageDTO dto, String personality,
                                             ClientAiConfigDTO clientConfig);

    ChatMessageVO streamMessageWithPersonality(Long userId, ChatMessageDTO dto, String personality,
                                               Consumer<String> onDelta);

    ChatMessageVO streamMessageWithPersonality(Long userId, ChatMessageDTO dto, String personality,
                                               ClientAiConfigDTO clientConfig, Consumer<String> onDelta);
}
