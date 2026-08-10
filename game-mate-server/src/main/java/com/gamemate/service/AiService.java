package com.gamemate.service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface AiService {

    String chat(Long userId, Long gameId, String userMessage, List<Map<String, String>> history);

    String chatWithPersonality(Long userId, Long gameId, String userMessage, List<Map<String, String>> history, String personality);

    String streamChatWithPersonality(Long userId, Long gameId, String userMessage,
                                     List<Map<String, String>> history, String personality,
                                     Consumer<String> onDelta);

    String analyzeScreen(Long userId, Long gameId, String imageBase64);

    String analyzeScreenWithQuery(Long userId, Long gameId, String imageBase64, String query);

    String analyzeScreenWithPersonality(Long userId, Long gameId, String imageBase64, String query, String personality);
}
