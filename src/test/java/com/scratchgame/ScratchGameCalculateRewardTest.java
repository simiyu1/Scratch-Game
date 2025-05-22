package com.scratchgame;

import com.scratchgame.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class specifically for the calculateReward method of the ScratchGame class.
 */
public class ScratchGameCalculateRewardTest {

    @Test
    @DisplayName("calculateReward should return 0 for non-positive bet amount")
    void testCalculateRewardWithNonPositiveBet() throws Exception {
        // Create a minimal valid game config
        GameConfig config = createMinimalGameConfig();
        ScratchGame game = new ScratchGame(config);

        // Create a minimal matrix and winning combinations
        List<List<String>> matrix = Arrays.asList(
                Arrays.asList("A", "A", "A"),
                Arrays.asList("B", "B", "B"),
                Arrays.asList("A", "A", "A")
        );
        Map<String, List<String>> winningCombinations = new HashMap<>();
        winningCombinations.put("A", Arrays.asList("same_symbol_3_times"));
        
        // Get calculateReward method with reflection
        Method calculateReward = game.getClass().getDeclaredMethod("calculateReward", 
                List.class, Map.class, double.class);
        calculateReward.setAccessible(true);
        
        // Test with zero bet amount
        Double reward = (Double) calculateReward.invoke(game, matrix, winningCombinations, 0.0);
        assertEquals(0.0, reward);

        // Test with negative bet amount
        reward = (Double) calculateReward.invoke(game, matrix, winningCombinations, -10.0);
        assertEquals(0.0, reward);
    }

    @Test
    @DisplayName("calculateReward should throw exception when win combinations config is missing")
    void testCalculateRewardWithMissingWinCombinations() throws Exception {
        // Create a valid game config
        GameConfig config = createMinimalGameConfig();
        ScratchGame game = new ScratchGame(config);
        
        // Use reflection to set the win combinations to null after object creation
        Field configField = game.getClass().getDeclaredField("config");
        configField.setAccessible(true);
        GameConfig gameConfig = (GameConfig) configField.get(game);
        
        // Set win combinations to null or empty map
        gameConfig.setWinCombinations(null);

        // Create a minimal matrix and winnings
        List<List<String>> matrix = Arrays.asList(
                Arrays.asList("A", "A", "A"),
                Arrays.asList("A", "A", "A"),
                Arrays.asList("A", "A", "A")
        );
        Map<String, List<String>> winningCombinations = new HashMap<>();
        winningCombinations.put("A", Arrays.asList("same_symbol_3_times"));

        // Get calculateReward method with reflection
        Method calculateReward = game.getClass().getDeclaredMethod("calculateReward", 
                List.class, Map.class, double.class);
        calculateReward.setAccessible(true);
        
        // This should throw an IllegalStateException wrapped in an InvocationTargetException
        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            calculateReward.invoke(game, matrix, winningCombinations, 10.0);
        });
        
        // Verify the cause is an IllegalStateException
        Throwable cause = ((InvocationTargetException) exception).getCause();
        assertEquals(IllegalStateException.class, cause.getClass());
        assertTrue(cause.getMessage().contains("Win combinations configuration is missing"));
    }
    
    @Test
    @DisplayName("calculateReward should throw exception when win combinations config is empty")
    void testCalculateRewardWithEmptyWinCombinations() throws Exception {
        // Create a valid game config
        GameConfig config = createMinimalGameConfig();
        ScratchGame game = new ScratchGame(config);
        
        // Use reflection to set the win combinations to empty after object creation
        Field configField = game.getClass().getDeclaredField("config");
        configField.setAccessible(true);
        GameConfig gameConfig = (GameConfig) configField.get(game);
        
        // Set win combinations to empty map
        gameConfig.setWinCombinations(new HashMap<>());

        // Create a minimal matrix and winnings
        List<List<String>> matrix = Arrays.asList(
                Arrays.asList("A", "A", "A"),
                Arrays.asList("A", "A", "A"),
                Arrays.asList("A", "A", "A")
        );
        Map<String, List<String>> winningCombinations = new HashMap<>();
        winningCombinations.put("A", Arrays.asList("same_symbol_3_times"));

        // Get calculateReward method with reflection
        Method calculateReward = game.getClass().getDeclaredMethod("calculateReward", 
                List.class, Map.class, double.class);
        calculateReward.setAccessible(true);
        
        // This should throw an IllegalStateException wrapped in an InvocationTargetException
        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            calculateReward.invoke(game, matrix, winningCombinations, 10.0);
        });
        
        // Verify the cause is an IllegalStateException
        Throwable cause = ((InvocationTargetException) exception).getCause();
        assertEquals(IllegalStateException.class, cause.getClass());
        assertTrue(cause.getMessage().contains("Win combinations configuration is missing"));
    }

    @Test
    @DisplayName("calculateReward should throw exception when symbol config is not found")
    void testCalculateRewardWithMissingSymbolConfig() throws Exception {
        // Create a game config where a symbol in winning combinations doesn't exist in the config
        GameConfig config = createMinimalGameConfig();
        ScratchGame game = new ScratchGame(config);

        List<List<String>> matrix = Arrays.asList(
                Arrays.asList("A", "A", "A"),
                Arrays.asList("B", "B", "B"),
                Arrays.asList("A", "A", "A")
        );
        
        // Add winning combination for symbol "C" which doesn't exist in config
        Map<String, List<String>> winningCombinations = new HashMap<>();
        winningCombinations.put("C", Arrays.asList("same_symbol_3_times"));

        // Get calculateReward method with reflection
        Method calculateReward = game.getClass().getDeclaredMethod("calculateReward", 
                List.class, Map.class, double.class);
        calculateReward.setAccessible(true);
        
        // This should throw an IllegalStateException wrapped in an InvocationTargetException
        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            calculateReward.invoke(game, matrix, winningCombinations, 10.0);
        });
        
        // Verify the cause is an IllegalStateException
        Throwable cause = ((InvocationTargetException) exception).getCause();
        assertEquals(IllegalStateException.class, cause.getClass());
        assertTrue(cause.getMessage().contains("Symbol configuration not found"));
    }

    @Test
    @DisplayName("calculateReward should throw exception when win combination config is not found")
    void testCalculateRewardWithMissingWinCombinationConfig() throws Exception {
        // Create a game config where a winning combination name doesn't exist in the config
        GameConfig config = createMinimalGameConfig();
        ScratchGame game = new ScratchGame(config);

        List<List<String>> matrix = Arrays.asList(
                Arrays.asList("A", "A", "A"),
                Arrays.asList("B", "B", "B"),
                Arrays.asList("A", "A", "A")
        );
        
        // Add non-existent winning combination name
        Map<String, List<String>> winningCombinations = new HashMap<>();
        winningCombinations.put("A", Arrays.asList("non_existent_combo"));

        // Get calculateReward method with reflection
        Method calculateReward = game.getClass().getDeclaredMethod("calculateReward", 
                List.class, Map.class, double.class);
        calculateReward.setAccessible(true);
        
        // This should throw an IllegalStateException wrapped in an InvocationTargetException
        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            calculateReward.invoke(game, matrix, winningCombinations, 10.0);
        });
        
        // Verify the cause is an IllegalStateException
        Throwable cause = ((InvocationTargetException) exception).getCause();
        assertEquals(IllegalStateException.class, cause.getClass());
        assertTrue(cause.getMessage().contains("Win combination configuration not found"));
    }

    // Helper method to create a minimal valid game configuration
    private GameConfig createMinimalGameConfig() {
        GameConfig config = new GameConfig();
        config.setRows(3);
        config.setColumns(3);
        
        // Set symbols
        Map<String, Symbol> symbols = new HashMap<>();
        symbols.put("A", createSymbol("standard", 5.0));
        symbols.put("B", createSymbol("standard", 3.0));
        config.setSymbols(symbols);
        
        // Set win combinations
        Map<String, WinCombination> winCombinations = new HashMap<>();
        WinCombination winCombination = new WinCombination();
        winCombination.setWhen("same_symbols");
        winCombination.setCount(3);
        winCombination.setRewardMultiplier(1.0);
        winCombinations.put("same_symbol_3_times", winCombination);
        config.setWinCombinations(winCombinations);
        
        // Set probabilities
        GameConfig.Probabilities probabilities = new GameConfig.Probabilities();
        List<GameConfig.StandardSymbolProbability> standardSymbols = new ArrayList<>();
        standardSymbols.add(createStandardSymbolProbability(0, 0, "A", 100));
        standardSymbols.add(createStandardSymbolProbability(0, 1, "A", 100));
        standardSymbols.add(createStandardSymbolProbability(0, 2, "A", 100));
        standardSymbols.add(createStandardSymbolProbability(1, 0, "B", 100));
        standardSymbols.add(createStandardSymbolProbability(1, 1, "B", 100));
        standardSymbols.add(createStandardSymbolProbability(1, 2, "B", 100));
        standardSymbols.add(createStandardSymbolProbability(2, 0, "A", 100));
        standardSymbols.add(createStandardSymbolProbability(2, 1, "A", 100));
        standardSymbols.add(createStandardSymbolProbability(2, 2, "A", 100));
        probabilities.setStandardSymbols(standardSymbols);
        config.setProbabilities(probabilities);
        
        return config;
    }
    
    // Helper method to create a Symbol object
    private Symbol createSymbol(String type, double rewardMultiplier) {
        Symbol symbol = new Symbol();
        symbol.setType(type);
        symbol.setRewardMultiplier(rewardMultiplier);
        symbol.setExtra(0.0); // Use 0.0 instead of 0 to match the Double type
        symbol.setImpact(null);
        return symbol;
    }
    
    // Helper method to create a StandardSymbolProbability object
    private GameConfig.StandardSymbolProbability createStandardSymbolProbability(int row, int column, String symbolName, int probability) {
        GameConfig.StandardSymbolProbability symbolProb = new GameConfig.StandardSymbolProbability();
        symbolProb.setRow(row);
        symbolProb.setColumn(column);
        Map<String, Integer> symbols = new HashMap<>();
        symbols.put(symbolName, probability);
        symbolProb.setSymbols(symbols);
        return symbolProb;
    }
} 