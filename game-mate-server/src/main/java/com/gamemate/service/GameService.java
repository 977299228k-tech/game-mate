package com.gamemate.service;

import com.gamemate.dto.GameCreateDTO;
import com.gamemate.vo.GameVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GameService {

    List<GameVO> getGameList(Long userId);

    List<GameVO> getPresetGames();

    List<GameVO> getCustomGames(Long userId);

    GameVO addCustomGame(Long userId, GameCreateDTO dto, MultipartFile icon);

    void deleteCustomGame(Long userId, Long gameId);

    String uploadIcon(Long userId, Long gameId, MultipartFile file);

    List<String> uploadIcons(Long userId, MultipartFile[] files);
}
