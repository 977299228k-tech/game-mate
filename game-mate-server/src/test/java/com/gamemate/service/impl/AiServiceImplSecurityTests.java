package com.gamemate.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamemate.config.AiConfig;
import com.gamemate.config.FileUploadConfig;
import com.gamemate.mapper.GameMapper;
import com.gamemate.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AiServiceImplSecurityTests {

    private AiServiceImpl aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiServiceImpl(
                new AiConfig(),
                new ObjectMapper(),
                new RestTemplate(),
                mock(GameMapper.class),
                mock(UserMapper.class),
                mock(FileUploadConfig.class));
    }

    @Test
    void rejectsNonHttpsClientEndpoint() {
        assertThrows(IllegalArgumentException.class,
                () -> aiService.validateExternalAiEndpoint("http://api.example.com/v1/chat/completions"));
    }

    @Test
    void rejectsLoopbackClientEndpoint() {
        assertThrows(IllegalArgumentException.class,
                () -> aiService.validateExternalAiEndpoint("https://127.0.0.1/v1/chat/completions"));
    }
}
