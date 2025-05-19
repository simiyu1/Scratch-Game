package com.scratchgame;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scratchgame.model.GameConfig;
import com.scratchgame.model.GameResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ScratchGameTest {
    private ScratchGame game;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        GameConfig config = objectMapper.readValue(new File("config.json"), GameConfig.class);
        game = new ScratchGame(config);
    }

    @Test
    void testPlayWithValidBet() {
        double betAmount = 100;
        GameResult result = game.play(betAmount);

        // Verify matrix structure
        assertNotNull(result.getMatrix());
        assertEquals(4, result.getMatrix().size());
        assertEquals(4, result.getMatrix().get(0).size());

        // Verify reward is non-negative
        assertTrue(result.getReward() >= 0);

        // If there are winning combinations, verify the structure
        if (result.getReward() > 0) {
            assertNotNull(result.getAppliedWinningCombinations());
            assertFalse(result.getAppliedWinningCombinations().isEmpty());
        }

        // Verify bonus symbol is either null or a valid symbol
        if (result.getAppliedBonusSymbol() != null) {
            assertFalse(result.getAppliedBonusSymbol().isEmpty());
        }
    }

    @Test
    void testPlayWithZeroBet() {
        double betAmount = 0;
        GameResult result = game.play(betAmount);

        // Verify reward is zero
        assertEquals(0, result.getReward());
        
        // Verify no winning combinations
        assertTrue(result.getAppliedWinningCombinations() == null || 
                  result.getAppliedWinningCombinations().isEmpty());
        
        // Verify no bonus symbol
        assertNull(result.getAppliedBonusSymbol());
    }

    @Test
    void testPlayWithNegativeBet() {
        double betAmount = -100;
        GameResult result = game.play(betAmount);

        // Verify reward is zero for negative bet
        assertEquals(0, result.getReward());
        
        // Verify no winning combinations
        assertTrue(result.getAppliedWinningCombinations() == null || 
                  result.getAppliedWinningCombinations().isEmpty());
        
        // Verify no bonus symbol
        assertNull(result.getAppliedBonusSymbol());
    }
} 