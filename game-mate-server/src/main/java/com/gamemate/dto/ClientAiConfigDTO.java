package com.gamemate.dto;

import lombok.Data;

@Data
public class ClientAiConfigDTO {

    /** OpenAI 兼容的完整 chat/completions 地址。 */
    private String apiUrl;

    private String apiKey;

    private String model;
}
