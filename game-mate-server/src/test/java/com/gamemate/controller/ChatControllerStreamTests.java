package com.gamemate.controller;

import com.gamemate.service.ChatService;
import com.gamemate.vo.ChatMessageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatControllerStreamTests {

    private MockMvc mockMvc;

    @Mock
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(chatService)).build();
    }

    @Test
    void streamsDeltaAndDoneEvents() throws Exception {
        when(chatService.streamMessageWithPersonality(any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    Consumer<String> onDelta = invocation.getArgument(4);
                    onDelta.accept("第一段。");
                    onDelta.accept("第二段。");

                    ChatMessageVO result = new ChatMessageVO();
                    result.setId(88L);
                    result.setContent("第一段。第二段。");
                    return result;
                });

        MvcResult asyncResult = mockMvc.perform(post("/api/chat/messages-with-personality/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"gameId":1,"content":"测试连续语音","personality":"friendly",
                                 "clientAiConfig":{"apiUrl":"https://example.com/v1/chat/completions","apiKey":"test-key","model":"test-model"}}
                                """))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("event:delta")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("第一段。")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("event:done")));
    }
}
