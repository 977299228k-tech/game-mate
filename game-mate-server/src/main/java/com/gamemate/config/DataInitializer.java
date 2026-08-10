package com.gamemate.config;

import com.gamemate.entity.Game;
import com.gamemate.entity.Plan;
import com.gamemate.mapper.GameMapper;
import com.gamemate.mapper.PlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@ConditionalOnProperty(name = "game-mate.data-initializer.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final GameMapper gameMapper;
    private final PlanMapper planMapper;

    @Override
    public void run(String... args) {
        log.info("【DataInitializer】检查并初始化预设数据...");
        initPresetGames();
        initPlans();
        log.info("【DataInitializer】数据初始化完成");
    }

    private void initPresetGames() {
        List<Game> existing = gameMapper.findAllPreset();
        
        // 预设游戏数据
        String[][] gamesData = {
            {"博德之门3", "CRPG", "⚔️", "/games/baldurs-gate3.jpg", "#8B0000", "经典CRPG角色扮演", "CRPG,角色扮演,剧情"},
            {"霍格沃茨之遗", "魔法RPG", "🪄", "/games/hogwarts.jpg", "#4B0082", "魔法世界冒险", "魔法,RPG,冒险"},
            {"荒野大镖客2", "西部RPG", "🤠", "/games/rdr2.jpg", "#8B4513", "西部荒野传奇", "西部,RPG,开放世界"},
            {"怪物猎人：世界", "动作RPG", "🦖", "/games/mhw.jpg", "#228B22", "狩猎动作游戏", "动作,RPG,狩猎"},
            {"星露谷物语", "农场模拟", "🌾", "/games/stardew.jpg", "#FFD700", "田园生活模拟", "农场,模拟,休闲"},
            {"终末地", "开放世界RPG", "🌆", "/games/R-C.jpg", "#9370DB", "都市奇幻冒险", "开放世界,RPG,奇幻"},
            {"埃尔登法环", "动作RPG", "⚔️", "/games/elden-ring.jpg", "#3B1F0A", "开放世界动作RPG", "动作,RPG,开放世界"},
            {"原神", "开放世界RPG", "✨", "/games/genshin.jpg", "#4169E1", "奇幻冒险RPG", "开放世界,RPG,奇幻"},
            {"赛博朋克2077", "科幻RPG", "🌆", "/games/cyberpunk.jpg", "#FF1493", "未来都市RPG", "科幻,RPG,开放世界"}
        };

        // 构建现有游戏名称映射
        Set<String> existingNames = new HashSet<>();
        for (Game g : existing) {
            existingNames.add(g.getName());
        }

        // 插入缺失的游戏 + 更新现有游戏的图片/图标
        for (String[] data : gamesData) {
            String gameName = data[0];
            if (existingNames.contains(gameName)) {
                // 更新现有游戏的图片和图标
                for (Game game : existing) {
                    if (game.getName().equals(gameName)) {
                        String newUrl = data[3];
                        String newIcon = data[2];
                        if (!newUrl.equals(game.getImageUrl()) || !newIcon.equals(game.getIcon())) {
                            game.setImageUrl(newUrl);
                            game.setIcon(newIcon);
                            gameMapper.updateById(game);
                            log.info("【DataInitializer】更新游戏: {} -> imageUrl: {}", gameName, newUrl);
                        }
                        break;
                    }
                }
            } else {
                // 插入新游戏
                Game game = new Game();
                game.setName(data[0]);
                game.setGenre(data[1]);
                game.setIcon(data[2]);
                game.setImageUrl(data[3]);
                game.setColor(data[4]);
                game.setDescription(data[5]);
                game.setTags(data[6]);
                game.setIsCustom(0);
                gameMapper.insert(game);
                log.info("【DataInitializer】新增游戏: {}", gameName);
            }
        }
        log.info("【DataInitializer】预设游戏初始化/更新完成，共 {} 款", gamesData.length);
    }

    private void initPlans() {
        Long count = planMapper.selectCount(null);
        if (count != null && count > 0) {
            log.info("【DataInitializer】套餐数据已存在，跳过初始化");
            return;
        }

        log.info("【DataInitializer】开始初始化套餐数据...");
        Object[][] plansData = {
            {"体验套餐", 2, 9.90, 19.90, 0},
            {"基础套餐", 10, 39.90, 59.90, 0},
            {"标准套餐", 30, 99.90, 149.90, 1},
            {"高级套餐", 60, 179.90, 239.90, 0},
            {"至尊套餐", 120, 299.90, 399.90, 0}
        };

        for (Object[] data : plansData) {
            Plan plan = new Plan();
            plan.setName((String) data[0]);
            plan.setHours((Integer) data[1]);
            plan.setPrice(new BigDecimal(data[2].toString()));
            plan.setOriginalPrice(new BigDecimal(data[3].toString()));
            plan.setIsPopular((Integer) data[4]);
            planMapper.insert(plan);
        }
        log.info("【DataInitializer】套餐数据初始化完成，共 {} 个", plansData.length);
    }
}
