package com.scratchgame;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scratchgame.model.GameConfig;
import com.scratchgame.model.GameResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SampleMatrixTest {
    private ScratchGame game;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        GameConfig config = objectMapper.readValue(new File("config.json"), GameConfig.class);
        game = new ScratchGame(config);
    }

    @Test
    void testSampleMatrix() {
        // Create a sample 4x4 matrix with multiple winning combinations
        List<List<String>> sampleMatrix = Arrays.asList(
            // Horizontal winning line of 'A' symbols in row 0
            Arrays.asList("A", "A", "A", "B"),
            // Vertical winning line of 'B' symbols in column 0
            Arrays.asList("B", "C", "D", "E"),
            Arrays.asList("B", "C", "F", "A"),
            // Diagonal winning line of 'C' symbols from top-left to bottom-right
            Arrays.asList("C", "D", "C", "C")
        );
        
        // Inject the test matrix
        game.setTestMatrix(sampleMatrix);
        
        // Run the game with a bet of 100
        GameResult result = game.play(100);
        
        // Verify the results
        assertNotNull(result);
        assertEquals(sampleMatrix, result.getMatrix());
        
        // We should have winning combinations for symbols A, B, and potentially C
        assertTrue(result.getReward() > 0, "Should have a positive reward");
        assertNotNull(result.getAppliedWinningCombinations(), "Should have winning combinations");
        
        // Print the result for manual verification
        System.out.println("Matrix:");
        for (List<String> row : result.getMatrix()) {
            System.out.println(row);
        }
        
        System.out.println("Reward: " + result.getReward());
        System.out.println("Winning combinations: " + result.getAppliedWinningCombinations());
        System.out.println("Bonus symbol: " + result.getAppliedBonusSymbol());
    }
} 