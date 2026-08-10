package com.gamemate.service;

import com.gamemate.vo.HighlightVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HighlightService {

    List<HighlightVO> getHighlightList(Long userId);

    List<HighlightVO> getHighlightListByGame(Long userId, Long gameId);

    HighlightVO uploadHighlight(Long userId, Long gameId, String title, MultipartFile video);

    void deleteHighlight(Long userId, Long highlightId);
}
