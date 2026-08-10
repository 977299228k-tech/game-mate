package com.gamemate.service.impl;

import com.gamemate.config.FileUploadConfig;
import com.gamemate.entity.Game;
import com.gamemate.entity.Highlight;
import com.gamemate.mapper.GameMapper;
import com.gamemate.mapper.HighlightMapper;
import com.gamemate.service.HighlightService;
import com.gamemate.vo.HighlightVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HighlightServiceImpl implements HighlightService {

    private final HighlightMapper highlightMapper;
    private final GameMapper gameMapper;
    private final FileUploadConfig fileUploadConfig;

    @Override
    public List<HighlightVO> getHighlightList(Long userId) {
        List<Highlight> highlights = highlightMapper.findByUserId(userId);
        return highlights.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<HighlightVO> getHighlightListByGame(Long userId, Long gameId) {
        List<Highlight> highlights = highlightMapper.findByUserIdAndGameId(userId, gameId);
        return highlights.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public HighlightVO uploadHighlight(Long userId, Long gameId, String title, MultipartFile video) {
        String videoUrl = null;
        if (video != null && !video.isEmpty()) {
            videoUrl = saveVideo(video);
        }

        Highlight highlight = new Highlight();
        highlight.setUserId(userId);
        highlight.setGameId(gameId);
        highlight.setTitle(title != null ? title : "高光时刻");
        highlight.setVideoUrl(videoUrl);
        highlight.setThumbnail(null);
        highlight.setDuration(0);
        highlightMapper.insert(highlight);

        return convertToVO(highlight);
    }

    @Override
    public void deleteHighlight(Long userId, Long highlightId) {
        Highlight highlight = highlightMapper.selectById(highlightId);
        if (highlight == null) {
            throw new RuntimeException("高光不存在");
        }
        if (!highlight.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此高光");
        }
        highlightMapper.deleteById(highlightId);
    }

    private String saveVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择视频文件");
        }
        if (file.getSize() > 100L * 1024 * 1024) {
            throw new IllegalArgumentException("视频不能超过100MB");
        }

        String contentType = file.getContentType();
        String ext = switch (contentType != null ? contentType.toLowerCase() : "") {
            case "video/mp4" -> ".mp4";
            case "video/webm" -> ".webm";
            case "video/quicktime" -> ".mov";
            default -> throw new IllegalArgumentException("仅支持 MP4、WebM 或 MOV 视频");
        };

        try {
            String uploadPath = fileUploadConfig.getUploadPath() + "highlights/";
            Path dirPath = Paths.get(uploadPath).toAbsolutePath().normalize();
            Files.createDirectories(dirPath);

            String fileName = "hl_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + ext;
            Path destPath = dirPath.resolve(fileName).normalize();
            if (!destPath.startsWith(dirPath)) {
                throw new IllegalArgumentException("非法文件名");
            }
            
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, destPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/highlights/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("视频上传失败: " + e.getMessage());
        }
    }

    private HighlightVO convertToVO(Highlight h) {
        HighlightVO vo = new HighlightVO();
        vo.setId(h.getId());
        vo.setUserId(h.getUserId());
        vo.setGameId(h.getGameId());
        vo.setTitle(h.getTitle());
        vo.setVideoUrl(h.getVideoUrl());
        vo.setThumbnail(h.getThumbnail());
        vo.setDuration(h.getDuration());
        vo.setCreateTime(h.getCreateTime());

        Game game = gameMapper.selectById(h.getGameId());
        if (game != null) {
            vo.setGameName(game.getName());
        }
        return vo;
    }
}
