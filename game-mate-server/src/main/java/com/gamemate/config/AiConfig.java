package com.gamemate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "game-mate.ai")
public class AiConfig {

    private String provider = "qwen";

    private String apiKey = "";

    private String apiUrl = "https://token-plan.cn-beijing.maas.aliyuncs.com/compatible-mode/v1/chat/completions";

    private String model = "qwen3.8-max-preview";

    private String visionModel = "qwen3.8-max-preview";

    private String systemPrompt = "你是一个专业的AI游戏陪玩助手，擅长分析游戏局势、提供战术建议、帮助玩家提升游戏水平。请用简洁、专业、友好的方式回答玩家的问题。";

    private Double temperature = 0.7;

    private Integer maxTokens = 800;

    private Boolean enabled = false;

    private Map<String, String> personalities = new HashMap<>();

    public AiConfig() {
        initPersonalities();
    }

    private void initPersonalities() {
        personalities.put("friendly", "你是一个友好热情的AI游戏伙伴，语气亲切自然，像朋友一样和玩家交流。你会用鼓励的语气，在玩家表现好时给予肯定，在玩家遇到困难时给予安慰和建议。");
        personalities.put("professional", "你是一个专业冷静的AI分析师，擅长理性分析游戏局势。你的回答条理清晰、数据驱动，注重战术分析和技术细节，帮助玩家做出最优决策。");
        personalities.put("passionate", "你是一个充满激情的AI教练，语气充满活力和感染力。你会用激情四射的语言鼓励玩家，像体育教练一样激发斗志，帮助玩家突破极限。");
        personalities.put("cute", "你是一个可爱萌系的AI小伙伴，语气活泼可爱，会使用一些可爱的表达方式。你会像闺蜜/基友一样陪伴玩家，让游戏过程更有趣。");
        personalities.put("serious", "你是一个严肃认真的AI导师，语气沉稳专业。你会以严谨的态度分析问题，给出精准的建议，帮助玩家快速提升游戏水平。");
        personalities.put("funny", "你是一个幽默风趣的AI段子手，擅长用轻松搞笑的方式回答问题。你会适时加入游戏梗和幽默元素，让对话充满乐趣。");
        personalities.put("strategist", "你是一个战术大师AI，精通各种游戏战术和策略。你会从战术层面分析问题，提供系统性的策略建议，帮助玩家建立全局思维。");
        personalities.put("mentor", "你是一个耐心的AI导师，擅长循序渐进地教学。你会用通俗易懂的语言解释复杂概念，引导玩家思考，帮助他们真正理解游戏机制。");
    }

    public String getPersonalityPrompt(String personality) {
        if (personality == null || personality.isEmpty()) {
            return systemPrompt;
        }
        String personalityPrompt = personalities.get(personality);
        if (personalityPrompt == null) {
            return systemPrompt;
        }
        return personalityPrompt + "\n\n基础设定：" + systemPrompt;
    }
}
