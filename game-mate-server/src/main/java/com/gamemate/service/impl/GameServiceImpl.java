package com.gamemate.service.impl;

import com.gamemate.config.FileUploadConfig;
import com.gamemate.dto.GameCreateDTO;
import com.gamemate.entity.Game;
import com.gamemate.mapper.GameMapper;
import com.gamemate.service.GameService;
import com.gamemate.vo.GameVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameMapper gameMapper;
    private final FileUploadConfig fileUploadConfig;

    @Override
    public List<GameVO> getGameList(Long userId) {
        List<Game> presetGames = gameMapper.findAllPreset();
        List<Game> customGames = new ArrayList<>();
        if (userId != null) {
            customGames = gameMapper.findAllCustomByUserId(userId);
        }
        presetGames.addAll(customGames);
        return presetGames.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<GameVO> getPresetGames() {
        List<Game> presetGames = gameMapper.findAllPreset();
        return presetGames.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<GameVO> getCustomGames(Long userId) {
        List<Game> customGames = gameMapper.findAllCustomByUserId(userId);
        return customGames.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public GameVO addCustomGame(Long userId, GameCreateDTO dto, MultipartFile icon) {
        Game game = new Game();
        game.setName(dto.getName());
        game.setGenre(dto.getGenre());
        game.setColor(dto.getColor());
        game.setDescription(dto.getDescription());
        game.setIsCustom(1);
        game.setUserId(userId);
        game.setTags(dto.getTags() != null ? String.join(",", dto.getTags()) : "");

        if (icon != null && !icon.isEmpty()) {
            String iconUrl = saveIcon(icon, null);
            game.setImageUrl(iconUrl);
        } else if (dto.getImageUrl() != null && !dto.getImageUrl().isEmpty()) {
            game.setImageUrl(dto.getImageUrl());
        }

        if (dto.getIcon() != null) {
            game.setIcon(dto.getIcon());
        } else {
            game.setIcon("🎮");
        }

        gameMapper.insert(game);
        return convertToVO(game);
    }

    @Override
    public void deleteCustomGame(Long userId, Long gameId) {
        Game game = gameMapper.selectById(gameId);
        if (game == null) {
            throw new RuntimeException("游戏不存在");
        }
        if (game.getIsCustom() == null || game.getIsCustom() != 1) {
            throw new RuntimeException("系统预设游戏不可删除");
        }
        if (game.getUserId() == null || !game.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此游戏");
        }
        gameMapper.deleteById(gameId);
    }

    @Override
    public String uploadIcon(Long userId, Long gameId, MultipartFile file) {
        return saveIcon(file, gameId);
    }

    @Override
    public List<String> uploadIcons(Long userId, MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String url = saveIcon(file, null);
                urls.add(url);
            }
        }
        return urls;
    }

    private String saveIcon(MultipartFile file, Long gameId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("图片不能超过5MB");
        }

        String contentType = file.getContentType();
        String ext = switch (contentType != null ? contentType.toLowerCase() : "") {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> throw new IllegalArgumentException("仅支持 JPG、PNG、WebP 或 GIF 图片");
        };

        try {
            String uploadPath = fileUploadConfig.getUploadPath();
            Path dirPath = Paths.get(uploadPath).toAbsolutePath().normalize();
            Files.createDirectories(dirPath);

            String fileName = "icon_" + (gameId != null ? gameId : UUID.randomUUID().toString().substring(0, 8)) + ext;
            Path destPath = dirPath.resolve(fileName).normalize();
            if (!destPath.startsWith(dirPath)) {
                throw new IllegalArgumentException("非法文件名");
            }

            try (InputStream input = file.getInputStream()) {
                Files.copy(input, destPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("图标上传失败: " + e.getMessage());
        }
    }

    private GameVO convertToVO(Game game) {
        GameVO vo = new GameVO();
        vo.setId(game.getId());
        vo.setName(game.getName());
        vo.setGenre(game.getGenre());
        vo.setIcon(game.getIcon());
        vo.setImageUrl(game.getImageUrl());
        vo.setColor(game.getColor());
        vo.setDescription(game.getDescription());
        vo.setTags(game.getTags());
        vo.setIsCustom(game.getIsCustom());
        vo.setCreateTime(game.getCreateTime());
        return vo;
    }
}
