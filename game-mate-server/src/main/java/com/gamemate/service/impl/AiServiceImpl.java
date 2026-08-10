package com.gamemate.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamemate.config.AiConfig;
import com.gamemate.config.FileUploadConfig;
import com.gamemate.dto.ClientAiConfigDTO;
import com.gamemate.entity.Game;
import com.gamemate.entity.User;
import com.gamemate.mapper.GameMapper;
import com.gamemate.mapper.UserMapper;
import com.gamemate.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final GameMapper gameMapper;
    private final UserMapper userMapper;
    private final FileUploadConfig fileUploadConfig;

    @Override
    public String chat(Long userId, Long gameId, String userMessage, List<Map<String, String>> history) {
        if (!aiConfig.getEnabled()) {
            log.warn("AI功能未开启");
            return "⚠️ AI功能当前未开启。请在 application.yml 中设置 game-mate.ai.enabled=true";
        }

        log.info("开始处理聊天请求，userId={}, gameId={}", userId, gameId);
        try {
            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            String enrichedPrompt = buildSystemPrompt(gameId, userId);
            systemMessage.put("content", enrichedPrompt);
            messages.add(systemMessage);

            if (history != null) {
                messages.addAll(history.stream()
                        .filter(m -> "user".equals(m.get("role")) || "assistant".equals(m.get("role")))
                        .collect(Collectors.toList()));
            }

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            log.info("调用AI API，消息数: {}", messages.size());
            String result = callAiApi(messages);
            log.info("AI调用成功，回答长度: {}", result.length());
            return result;
        } catch (Exception e) {
            log.error("AI调用失败", e);
            return "❌ AI服务调用失败：" + e.getMessage() + "\n\n请检查后端日志了解详细错误信息。可能的原因：\n1. API Key无效或过期\n2. 网络连接问题\n3. AI服务暂时不可用";
        }
    }

    @Override
    public String chatWithPersonality(Long userId, Long gameId, String userMessage, List<Map<String, String>> history, String personality) {
        return chatWithPersonality(userId, gameId, userMessage, history, personality, null);
    }

    @Override
    public String chatWithPersonality(Long userId, Long gameId, String userMessage,
                                      List<Map<String, String>> history, String personality,
                                      ClientAiConfigDTO clientConfig) {
        if (!aiConfig.getEnabled() && clientConfig == null) {
            log.warn("AI功能未开启");
            return "⚠️ AI功能当前未开启。请在 application.yml 中设置 game-mate.ai.enabled=true";
        }

        log.info("开始处理性格聊天请求，userId={}, gameId={}, personality={}", userId, gameId, personality);
        try {
            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            String enrichedPrompt = buildSystemPrompt(gameId, userId, personality);
            systemMessage.put("content", enrichedPrompt);
            messages.add(systemMessage);

            if (history != null) {
                messages.addAll(history.stream()
                        .filter(m -> "user".equals(m.get("role")) || "assistant".equals(m.get("role")))
                        .collect(Collectors.toList()));
            }

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            log.info("调用AI API，消息数: {}", messages.size());
            String result = callAiApi(messages, clientConfig);
            log.info("AI调用成功，回答长度: {}", result.length());
            return result;
        } catch (Exception e) {
            log.error("AI调用失败", e);
            return "❌ AI服务调用失败：" + e.getMessage() + "\n\n请检查后端日志了解详细错误信息。可能的原因：\n1. API Key无效或过期\n2. 网络连接问题\n3. AI服务暂时不可用";
        }
    }

    @Override
    public String streamChatWithPersonality(Long userId, Long gameId, String userMessage,
                                            List<Map<String, String>> history, String personality,
                                            Consumer<String> onDelta) {
        return streamChatWithPersonality(userId, gameId, userMessage, history, personality, null, onDelta);
    }

    @Override
    public String streamChatWithPersonality(Long userId, Long gameId, String userMessage,
                                            List<Map<String, String>> history, String personality,
                                            ClientAiConfigDTO clientConfig, Consumer<String> onDelta) {
        if (!aiConfig.getEnabled() && clientConfig == null) {
            String fallback = "⚠️ AI功能当前未开启。请在 application.yml 中设置 game-mate.ai.enabled=true";
            onDelta.accept(fallback);
            return fallback;
        }

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", buildSystemPrompt(gameId, userId, personality));
        messages.add(systemMessage);

        if (history != null) {
            messages.addAll(history.stream()
                    .filter(m -> "user".equals(m.get("role")) || "assistant".equals(m.get("role")))
                    .collect(Collectors.toList()));
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        return callAiApiStream(messages, clientConfig, onDelta);
    }

    @Override
    public String analyzeScreen(Long userId, Long gameId, String imageUrl) {
        return analyzeScreenWithPersonality(userId, gameId, imageUrl, null, null);
    }

    @Override
    public String analyzeScreenWithPersonality(Long userId, Long gameId, String imageBase64, String query, String personality) {
        return analyzeScreenWithPersonality(userId, gameId, imageBase64, query, personality, null);
    }

    @Override
    public String analyzeScreenWithPersonality(Long userId, Long gameId, String imageBase64, String query,
                                               String personality, ClientAiConfigDTO clientConfig) {
        if (!aiConfig.getEnabled() && clientConfig == null) {
            return "画面分析功能暂未开启。当前检测到您上传了游戏画面，建议关注当前局势，合理利用技能和资源。";
        }

        String effectiveQuery = query != null ? query : "请分析这张游戏画面，识别当前局势并给出建议";

        log.info("开始画面分析，gameId={}, query={}, personality={}, hasImage={}", gameId, effectiveQuery, personality, imageBase64 != null && !imageBase64.isEmpty());

        try {
            List<Map<String, Object>> messages = new ArrayList<>();

            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            String gameContext = getGameContext(gameId);
            String personalityPrompt = getPersonalityPrompt(personality);
            String analysisPrompt = "你是游戏画面分析师。请分析截图内容（角色、装备、位置、局势），并给出具体建议。若无图片则基于游戏知识回答。回答要简洁，用分点。";
            systemMessage.put("content", personalityPrompt + "\n\n" + gameContext + "\n\n" + analysisPrompt);
            messages.add(systemMessage);

            if (imageBase64 != null && !imageBase64.isEmpty()) {
                String base64Image;
                try {
                    base64Image = normalizeBase64Image(imageBase64);
                    log.info("图片处理成功，base64长度: {} 字符", base64Image.length());

                    int base64SizeKB = (int)(base64Image.length() * 3 / 4 / 1024);
                    log.info("图片大小约: {}KB", base64SizeKB);

                    if (base64SizeKB > 500) {
                        log.warn("图片过大({}KB)，AI API可能无法处理", base64SizeKB);
                    }
                } catch (Exception imgError) {
                    log.warn("图片处理失败: {}，使用纯文本模式", imgError.getMessage());
                    return analyzeWithTextOnly(userId, gameId, effectiveQuery, personality, clientConfig);
                }

                List<Map<String, Object>> userContent = new ArrayList<>();

                Map<String, Object> textPart = new HashMap<>();
                textPart.put("type", "text");
                textPart.put("text", "请分析这张游戏截图，识别局势并给出建议。\n玩家问题：" + effectiveQuery);
                userContent.add(textPart);

                Map<String, Object> imagePart = new HashMap<>();
                imagePart.put("type", "image_url");
                Map<String, String> imageUrlMap = new HashMap<>();
                imageUrlMap.put("url", base64Image);
                imagePart.put("image_url", imageUrlMap);
                userContent.add(imagePart);

                Map<String, Object> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", userContent);
                messages.add(userMessage);

                log.info("视觉API请求构造完成，开始调用... 图片大小: {}字符 (约{}KB)", base64Image.length(), base64Image.length() / 1024);

                // 检查图片大小
                if (base64Image.length() > 5000000) {
                    log.warn("图片过大: {}字符，可能导致超时。建议压缩图片后再试。", base64Image.length());
                }

                try {
                    long startTime = System.currentTimeMillis();
                    String result = callVisionAiApi(messages, clientConfig);
                    long elapsed = System.currentTimeMillis() - startTime;
                    log.info("视觉分析成功，耗时: {}ms，回答长度: {}", elapsed, result.length());

                    if (result.contains("无法") || result.contains("看不到") || result.contains("未收到")) {
                        log.warn("AI可能没有正确分析图片内容，回答: {}", result.substring(0, Math.min(100, result.length())));
                    }
                    return result;
                } catch (Exception visionError) {
                    String errMsg = visionError.getMessage();
                    log.warn("视觉API调用失败: {}，尝试使用纯文本模式回退", errMsg);

                    // 所有错误都回退到纯文本模式
                    if (errMsg != null && errMsg.contains("Read timed out")) {
                        log.info("检测到超时，切换为纯文本分析模式");
                        return analyzeWithTextOnly(userId, gameId, effectiveQuery + "（注：图片分析超时，请描述你看到的画面内容）", personality, clientConfig);
                    }
                    if (errMsg != null && (errMsg.contains("image_url") || errMsg.contains("expected text") || errMsg.contains("400") || errMsg.contains("不支持"))) {
                        log.info("检测到模型不支持视觉，切换为纯文本分析模式");
                        return analyzeWithTextOnly(userId, gameId, effectiveQuery + "（注：因模型限制，无法查看截图，请玩家描述画面细节）", personality, clientConfig);
                    }
                    log.warn("其他错误，回退到纯文本模式");
                    return analyzeWithTextOnly(userId, gameId, effectiveQuery + "（注：图片分析暂时不可用）", personality, clientConfig);
                }
            } else {
                log.info("无图片URL，使用纯文本分析模式");
                return analyzeWithTextOnly(userId, gameId, effectiveQuery, personality, clientConfig);
            }
        } catch (Exception e) {
            log.error("画面分析失败", e);
            String errorMsg = e.getMessage();

            if (errorMsg != null && errorMsg.contains("401")) {
                return "❌ AI API 认证失败（401错误）。请检查 application.yml 中的 api-key 配置是否正确。";
            } else if (errorMsg != null && errorMsg.contains("403")) {
                return "❌ AI API 访问被拒绝（403错误）。API Key可能权限不足或已过期。";
            } else if (errorMsg != null && errorMsg.contains("图片文件不存在")) {
                return "❌ 截图文件丢失，请重新截取画面后重试。";
            }
            return "❌ 画面分析失败：" + (errorMsg != null ? errorMsg : "未知错误") + "\n\n请检查后端日志了解详细错误信息。";
        }
    }

    private String analyzeWithTextOnly(Long userId, Long gameId, String query, String personality) {
        return analyzeWithTextOnly(userId, gameId, query, personality, null);
    }

    private String analyzeWithTextOnly(Long userId, Long gameId, String query, String personality,
                                       ClientAiConfigDTO clientConfig) {
        String enrichedQuery = "关于《游戏》的问题：" + query + "。请基于游戏知识简洁回答，用分点。";
        return chatWithPersonality(userId, gameId, enrichedQuery, null, personality, clientConfig);
    }

    @Override
    public String analyzeScreenWithQuery(Long userId, Long gameId, String imageUrl, String query) {
        return analyzeScreenWithPersonality(userId, gameId, imageUrl, query, null);
    }

    private String normalizeBase64Image(String base64) {
        if (base64 == null || base64.isEmpty()) {
            throw new IllegalArgumentException("图片数据为空");
        }

        String pureBase64;
        String mimeType = "image/jpeg";

        if (base64.startsWith("data:")) {
            int commaIndex = base64.indexOf(',');
            if (commaIndex > 0) {
                String meta = base64.substring(0, commaIndex);
                if (meta.contains("image/png")) {
                    mimeType = "image/png";
                } else if (meta.contains("image/gif")) {
                    mimeType = "image/gif";
                } else if (meta.contains("image/webp")) {
                    mimeType = "image/webp";
                }
                pureBase64 = base64.substring(commaIndex + 1);
            } else {
                pureBase64 = base64;
            }
        } else {
            pureBase64 = base64;
        }

        int base64SizeKB = (int)(pureBase64.length() * 3 / 4 / 1024);
        log.info("原始图片大小约: {}KB", base64SizeKB);

        // 如果图片大于500KB，进行压缩
        if (base64SizeKB > 500) {
            log.info("图片过大，开始压缩...");
            try {
                byte[] imageBytes = Base64.getDecoder().decode(pureBase64);
                BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

                if (originalImage == null) {
                    log.warn("无法解析图片，使用原始版本");
                    return "data:" + mimeType + ";base64," + pureBase64;
                }

                int originalWidth = originalImage.getWidth();
                int originalHeight = originalImage.getHeight();
                log.info("原始图片尺寸: {}x{}", originalWidth, originalHeight);

                // 计算压缩后的尺寸，最大宽度1024
                int maxWidth = 1024;
                int newWidth = originalWidth;
                int newHeight = originalHeight;

                if (originalWidth > maxWidth) {
                    newWidth = maxWidth;
                    newHeight = (int)((double)originalHeight * maxWidth / originalWidth);
                }

                // 确保高度也是偶数
                if (newHeight % 2 != 0) {
                    newHeight--;
                }
                if (newWidth % 2 != 0) {
                    newWidth--;
                }

                log.info("压缩后尺寸: {}x{}", newWidth, newHeight);

                // 缩放图片
                BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = scaledImage.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                graphics.dispose();

                // 压缩为JPEG，使用较低质量以减小体积
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                float quality = 0.80f;

                // 设置压缩质量
                javax.imageio.ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName("jpg").next();
                javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(baos);
                writer.setOutput(ios);
                javax.imageio.plugins.jpeg.JPEGImageWriteParam jpegParams = (javax.imageio.plugins.jpeg.JPEGImageWriteParam) writer.getDefaultWriteParam();
                jpegParams.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                jpegParams.setCompressionQuality(quality);
                writer.write(null, new javax.imageio.IIOImage(scaledImage, null, null), jpegParams);
                ios.close();
                writer.dispose();

                byte[] compressedBytes = baos.toByteArray();
                String compressedBase64 = Base64.getEncoder().encodeToString(compressedBytes);

                int compressedSizeKB = (int)(compressedBase64.length() * 3 / 4 / 1024);
                log.info("压缩完成，新图片大小约: {}KB (原始: {}KB)", compressedSizeKB, base64SizeKB);

                return "data:image/jpeg;base64," + compressedBase64;

            } catch (Exception e) {
                log.warn("图片压缩失败: {}，使用原始版本", e.getMessage());
            }
        }

        return "data:" + mimeType + ";base64," + pureBase64;
    }

    private String imageUrlToBase64(String imageUrl) {
        try {
            log.info("开始处理图片URL: {}", imageUrl);

            String uploadPath = fileUploadConfig.getUploadPath();
            Path basePath = Paths.get(uploadPath).toAbsolutePath().normalize();

            Path filePath = null;

            if (imageUrl.startsWith("/uploads/")) {
                String relativePath = imageUrl.substring("/uploads/".length());

                filePath = basePath.resolve(relativePath).normalize();

                if (!Files.exists(filePath)) {
                    log.warn("尝试备选路径: {}", filePath);
                    Path altPath = basePath.resolve("screenshots").resolve(imageUrl.substring(imageUrl.lastIndexOf("/") + 1));
                    if (Files.exists(altPath)) {
                        filePath = altPath;
                    }
                }
            } else {
                filePath = Paths.get(imageUrl).toAbsolutePath().normalize();
            }

            log.info("最终图片路径: {}, 基础路径: {}", filePath, basePath);

            if (filePath == null || !Files.exists(filePath)) {
                log.error("图片文件不存在: {}", filePath);
                throw new IOException("图片文件不存在: " + filePath);
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            String base64 = Base64.getEncoder().encodeToString(fileBytes);

            String mimeType = getMimeType(filePath);
            log.info("图片转换成功，MIME类型: {}, Base64长度: {}", mimeType, base64.length());

            return "data:" + mimeType + ";base64," + base64;
        } catch (IOException e) {
            log.error("转换图片为base64失败", e);
            throw new RuntimeException("无法读取图片文件: " + e.getMessage());
        }
    }

    private String getMimeType(Path filePath) {
        try {
            String mimeType = Files.probeContentType(filePath);
            if (mimeType != null && !mimeType.isEmpty()) {
                return mimeType;
            }
        } catch (Exception e) {
            log.warn("获取MIME类型失败", e);
        }
        String fileName = filePath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private String getGameContext(Long gameId) {
        if (gameId == null) return "";
        try {
            Game game = gameMapper.selectById(gameId);
            if (game != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("当前游戏：").append(game.getName());
                if (game.getGenre() != null) {
                    sb.append("，类型：").append(game.getGenre());
                }
                if (game.getDescription() != null) {
                    sb.append("，描述：").append(game.getDescription());
                }
                return sb.toString();
            }
        } catch (Exception e) {
            log.warn("获取游戏上下文失败", e);
        }
        return "";
    }

    private String getGameKnowledge(Long gameId) {
        if (gameId == null) return "";
        try {
            Game game = gameMapper.selectById(gameId);
            if (game == null) return "";

            String name = game.getName();
            StringBuilder knowledge = new StringBuilder();
            knowledge.append("\n\n【游戏背景知识供参考】");

            if ("王者荣耀".equals(name)) {
                knowledge.append("\n王者荣耀是MOBA手游，5v5对战，含上/中/下/野四个位置，核心资源有小龙、大龙、暴君。");
            } else if ("原神".equals(name)) {
                knowledge.append("\n原神是开放世界RPG，含七国设定，核心是元素反应系统（火水冰水雷风岩草），角色分主C/副C/辅助。");
            } else if ("英雄联盟".equals(name)) {
                knowledge.append("\nLOL是MOBA端游，5v5，分上/野/中/ADC/辅五路，核心资源有男爵、小龙、峡谷先锋。");
            } else if ("和平精英".equals(name) || "绝地求生".equals(name) || "PUBG".equals(name)) {
                knowledge.append("\n大逃杀射击游戏，核心是搜装、战术转移、团队配合，需注意毒圈收缩。");
            } else if ("永劫无间".equals(name)) {
                knowledge.append("\n武侠大逃杀，近战格斗为主，远程辅助，讲究技能配合和连招。");
            } else if ("DOTA2".equals(name)) {
                knowledge.append("\nMOBA游戏，三条兵线，五位置，核心机制含反补、拉野、控符。");
            } else if ("CS2".equals(name) || "CSGO".equals(name) || "反恐精英".equals(name)) {
                knowledge.append("\nFPS竞技游戏，回合制爆破，讲究经济管理、预瞄、团队配合。");
            } else if ("APEX英雄".equals(name)) {
                knowledge.append("\n大逃杀英雄射击，三人小队，强调传奇技能配合和快速节奏。");
            } else if ("艾尔登法环".equals(name)) {
                knowledge.append("\n开放世界魂类RPG，核心机制含翻滚闪避、格挡弹反，死亡后魂掉落。");
            } else if ("博德之门3".equals(name)) {
                knowledge.append("\n回合制CRPG，基于D&D5e规则，含职业系统、骰子检定、对话选择。");
            } else if ("怪物猎人".equals(name) || "怪物猎人：世界".equals(name)) {
                knowledge.append("\n动作狩猎RPG，14种武器，核心是狩猎怪物、观察动作、预判时机。");
            } else if ("赛博朋克2077".equals(name)) {
                knowledge.append("\n开放世界第一人称RPG，含义体改造、黑客系统、潜行战斗多种玩法。");
            } else if ("荒野大镖客2".equals(name)) {
                knowledge.append("\n开放世界动作冒险，西部背景，含荣誉系统、死眼、营地建设。");
            } else if ("星露谷物语".equals(name)) {
                knowledge.append("\n农场模拟游戏，含种田、养殖、钓鱼、社交、探索等玩法。");
            } else if ("霍格沃茨之遗".equals(name)) {
                knowledge.append("\n开放世界RPG，魔法主题，含咒语学习、战斗、探索系统。");
            } else if ("终末地".equals(name)) {
                knowledge.append("\n3D动作RPG，含元素反应、武器切换、角色培养系统。");
            } else {
                knowledge.append("\n游戏类型：").append(game.getGenre() != null ? game.getGenre() : "动作冒险");
            }

            return knowledge.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private record ResolvedAiClient(String apiUrl, String apiKey, String model) {}

    private ResolvedAiClient resolveAiClient(ClientAiConfigDTO clientConfig, boolean vision) {
        if (clientConfig == null) {
            String model = vision && aiConfig.getVisionModel() != null
                    ? aiConfig.getVisionModel() : aiConfig.getModel();
            return new ResolvedAiClient(aiConfig.getApiUrl(), aiConfig.getApiKey(), model);
        }

        String apiUrl = requireClientValue(clientConfig.getApiUrl(), "API地址", 2048);
        String apiKey = requireClientValue(clientConfig.getApiKey(), "API Key", 2048);
        String model = requireClientValue(clientConfig.getModel(), "模型名称", 200);
        validateExternalAiEndpoint(apiUrl);
        return new ResolvedAiClient(apiUrl, apiKey, model);
    }

    private String requireClientValue(String value, String fieldName, int maxLength) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + "长度超出限制");
        }
        return trimmed;
    }

    void validateExternalAiEndpoint(String apiUrl) {
        try {
            java.net.URI uri = java.net.URI.create(apiUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("客户API地址必须使用HTTPS");
            }
            if (uri.getHost() == null || uri.getHost().isBlank() || uri.getUserInfo() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException("客户API地址格式不正确");
            }
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            if ("localhost".equals(host) || host.endsWith(".local")) {
                throw new IllegalArgumentException("客户API地址不能指向本机或内网");
            }
            for (java.net.InetAddress address : java.net.InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                        || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) {
                    throw new IllegalArgumentException("客户API地址不能指向本机或内网");
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("无法验证客户API地址: " + e.getMessage(), e);
        }
    }

    private String callAiApi(List<Map<String, String>> messages) {
        return callAiApi(messages, null);
    }

    private String callAiApi(List<Map<String, String>> messages, ClientAiConfigDTO clientConfig) {
        ResolvedAiClient resolved = resolveAiClient(clientConfig, false);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", resolved.model());
        requestBody.put("messages", messages);
        requestBody.put("temperature", aiConfig.getTemperature());
        requestBody.put("max_tokens", aiConfig.getMaxTokens());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + resolved.apiKey());
        headers.put("Content-Type", "application/json");

        return doCallApi(resolved.apiUrl(), requestBody, headers);
    }

    private String callAiApiStream(List<Map<String, String>> messages, ClientAiConfigDTO clientConfig,
                                   Consumer<String> onDelta) {
        ResolvedAiClient resolved = resolveAiClient(clientConfig, false);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", resolved.model());
        requestBody.put("messages", messages);
        requestBody.put("temperature", aiConfig.getTemperature());
        requestBody.put("max_tokens", aiConfig.getMaxTokens());
        requestBody.put("stream", true);

        java.net.HttpURLConnection conn = null;
        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            conn = (java.net.HttpURLConnection) new java.net.URL(resolved.apiUrl()).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + resolved.apiKey());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(120000);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                java.io.InputStream errorStream = conn.getErrorStream();
                String errorBody = errorStream == null ? "" : new String(
                        errorStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                throw new RuntimeException("HTTP Error: " + responseCode + " - " + errorBody);
            }

            StringBuilder complete = new StringBuilder();
            StringBuilder nonStreamResponse = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        if (!line.isBlank()) {
                            nonStreamResponse.append(line);
                        }
                        continue;
                    }

                    String data = line.substring(5).trim();
                    if (data.isEmpty() || "[DONE]".equals(data)) {
                        continue;
                    }

                    JsonNode root = objectMapper.readTree(data);
                    if (root.has("error")) {
                        throw new RuntimeException("API错误: " + root.get("error"));
                    }
                    JsonNode choices = root.path("choices");
                    if (!choices.isArray() || choices.isEmpty()) {
                        continue;
                    }
                    JsonNode choice = choices.get(0);
                    JsonNode contentNode = choice.path("delta").path("content");
                    if (contentNode.isMissingNode() || contentNode.isNull()) {
                        contentNode = choice.path("message").path("content");
                    }
                    if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                        String chunk = contentNode.asText();
                        if (!chunk.isEmpty()) {
                            complete.append(chunk);
                            onDelta.accept(chunk);
                        }
                    }
                }
            }

            // 兼容不支持SSE、但仍返回普通JSON的OpenAI兼容服务。
            if (complete.isEmpty() && !nonStreamResponse.isEmpty()) {
                JsonNode root = objectMapper.readTree(nonStreamResponse.toString());
                JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
                if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                    String content = contentNode.asText();
                    complete.append(content);
                    onDelta.accept(content);
                }
            }

            if (complete.isEmpty()) {
                throw new RuntimeException("AI流式响应为空");
            }
            return complete.toString();
        } catch (Exception e) {
            log.error("AI流式调用失败", e);
            throw new RuntimeException("AI流式调用失败: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String callVisionAiApi(List<Map<String, Object>> messages, ClientAiConfigDTO clientConfig) {
        ResolvedAiClient resolved = resolveAiClient(clientConfig, true);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", resolved.model());
        requestBody.put("messages", messages);
        requestBody.put("temperature", aiConfig.getTemperature());
        requestBody.put("max_tokens", aiConfig.getMaxTokens());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + resolved.apiKey());
        headers.put("Content-Type", "application/json");

        try {
            return doCallApi(resolved.apiUrl(), requestBody, headers);
        } catch (Exception e) {
            // 如果thinking参数不支持，移除后重试
            if (e.getMessage() != null && e.getMessage().contains("thinking")) {
                log.warn("模型不支持thinking参数，移除后重试");
                requestBody.remove("thinking");
                return doCallApi(resolved.apiUrl(), requestBody, headers);
            }
            throw e;
        }
    }

    private String doCallApi(Map<String, Object> requestBody, Map<String, String> headers) {
        return doCallApi(aiConfig.getApiUrl(), requestBody, headers);
    }

    private String doCallApi(String apiUrl, Map<String, Object> requestBody, Map<String, String> headers) {
        try {
            String response = HttpPost(apiUrl, requestBody, headers);

            JsonNode root = objectMapper.readTree(response);

            if (root.has("error")) {
                String errorMsg = root.get("error").toString();
                log.error("API返回错误: {}", errorMsg);
                throw new RuntimeException("API错误: " + errorMsg);
            }

            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                return choices.get(0).get("message").get("content").asText();
            }
            return "AI服务暂时无法响应，请稍后重试。";
        } catch (Exception e) {
            log.error("调用AI API失败", e);
            throw new RuntimeException("AI调用失败: " + e.getMessage());
        }
    }

    private String HttpPost(String url, Object body, Map<String, String> headers) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            long startTime = System.currentTimeMillis();

            log.info("发送API请求: URL={}, 请求体大小={}字节", url, jsonBody.length());

            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(60000);

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("API响应耗时: {}ms, 状态码: {}", elapsed, responseCode);

            if (responseCode == 200) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    log.info("响应内容长度: {}字符", response.length());
                    return response.toString();
                }
            } else if (responseCode == 401) {
                throw new RuntimeException("HTTP Error: 401 - API认证失败，请检查API Key是否正确");
            } else if (responseCode == 403) {
                throw new RuntimeException("HTTP Error: 403 - 访问被拒绝，可能是API Key权限不足");
            } else if (responseCode == 429) {
                throw new RuntimeException("HTTP Error: 429 - 请求过于频繁，请稍后重试");
            } else {
                String errorBody = "";
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorBody = errorResponse.toString();
                }
                throw new RuntimeException("HTTP Error: " + responseCode + " - " + errorBody);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("API调用失败: " + e.getMessage());
        }
    }

    private String buildSystemPrompt(Long gameId, Long userId) {
        return buildSystemPrompt(gameId, userId, null);
    }

    private String buildSystemPrompt(Long gameId, Long userId, String personality) {
        StringBuilder prompt = new StringBuilder();

        if (gameId != null) {
            Game game = gameMapper.selectById(gameId);
            if (game != null) {
                prompt.append("你是《").append(game.getName()).append("》的专业游戏顾问。");
                prompt.append("请基于游戏知识快速、简洁、准确地回答玩家问题。");
                prompt.append("使用分点列表，给出具体可执行的建议。");
                prompt.append(getGameKnowledge(gameId));
            }
        } else {
            prompt.append("你是专业的游戏助手，请简洁回答玩家问题。");
        }

        prompt.append("\n要求：回答简洁（不超过300字），具体可执行，使用分点。");

        return prompt.toString();
    }

    private String getPersonalityPrompt(String personality) {
        if (personality == null || personality.isEmpty()) {
            return aiConfig.getSystemPrompt();
        }
        return aiConfig.getPersonalityPrompt(personality);
    }

    private String generateFallbackResponse(String userMessage) {
        log.warn("使用降级响应，可能原因：AI功能未开启或API调用失败");
        return "⚠️ AI服务暂时不可用。请检查：\n\n1. 后端服务是否正常运行\n2. AI API Key是否正确配置\n3. 网络连接是否正常\n\n您可以在 application.yml 中检查 game-mate.ai 配置项。\n\n如需帮助，请联系管理员。";
    }
}
