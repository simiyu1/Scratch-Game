package com.scratchgame;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scratchgame.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for edge cases in ScratchGame.
 * These tests verify that the game handles various edge cases and invalid inputs correctly.
 */
public class ScratchGameEdgeCasesTest {
    private ObjectMapper objectMapper;
    private GameConfig baseConfig;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        baseConfig = createBaseConfig();
    }

    @Nested
    @DisplayName("Configuration Validation Tests")
    class ConfigurationValidationTests {
        @Test
        @DisplayName("Should throw exception when config is null")
        void testNullConfig() {
            assertThrows(IllegalArgumentException.class, () -> new ScratchGame(null),
                    "Should throw IllegalArgumentException when config is null");
        }

        @Test
        @DisplayName("Should throw exception when matrix dimensions are invalid")
        void testInvalidMatrixDimensions() {
            GameConfig config = createBaseConfig();
            config.setRows(0);
            config.setColumns(-1);

            assertThrows(IllegalArgumentException.class, () -> new ScratchGame(config),
                    "Should throw IllegalArgumentException for invalid matrix dimensions");
        }

        @Test
        @DisplayName("Should throw exception when symbols are missing")
        void testMissingSymbols() {
            GameConfig config = createBaseConfig();
            config.setSymbols(null);

            assertThrows(IllegalArgumentException.class, () -> new ScratchGame(config),
                    "Should throw IllegalArgumentException when symbols are missing");
        }

        @Test
        @DisplayName("Should throw exception when win combinations are missing")
        void testMissingWinCombinations() {
            GameConfig config = createBaseConfig();
            config.setWinCombinations(null);

            assertThrows(IllegalArgumentException.class, () -> new ScratchGame(config),
                    "Should throw IllegalArgumentException when win combinations are missing");
        }

        @Test
        @DisplayName("Should throw exception when probabilities are missing")
        void testMissingProbabilities() {
            GameConfig config = createBaseConfig();
            config.setProbabilities(null);

            assertThrows(IllegalArgumentException.class, () -> new ScratchGame(config),
                    "Should throw IllegalArgumentException when probabilities are missing");
        }
    }

    @Nested
    @DisplayName("Matrix Generation Tests")
    class MatrixGenerationTests {
        @Test
        @DisplayName("Should throw exception when probability distribution is invalid")
        void testInvalidProbabilityDistribution() {
            GameConfig config = createBaseConfig();
            GameConfig.StandardSymbolProbability probability = new GameConfig.StandardSymbolProbability();
            probability.setSymbols(new HashMap<>());
            config.getProbabilities().setStandardSymbols(Collections.singletonList(probability));

            assertThrows(IllegalStateException.class, () -> new ScratchGame(config).play(100),
                    "Should throw IllegalStateException for invalid probability distribution");
        }

        @Test
        @DisplayName("Should throw exception when symbol not found in configuration")
        void testSymbolNotFoundInConfig() {
            GameConfig config = createBaseConfig();
            Map<String, Integer> symbols = new HashMap<>();
            symbols.put("INVALID_SYMBOL", 100);
            GameConfig.StandardSymbolProbability probability = new GameConfig.StandardSymbolProbability();
            probability.setSymbols(symbols);
            config.getProbabilities().setStandardSymbols(Collections.singletonList(probability));

            assertThrows(IllegalStateException.class, () -> new ScratchGame(config).play(100),
                    "Should throw IllegalStateException when symbol not found in configuration");
        }
    }

    @Nested
    @DisplayName("Winning Combinations Tests")
    class WinningCombinationsTests {
        @Test
        @DisplayName("Should handle overlapping winning combinations correctly")
        void testOverlappingWinningCombinations() {
            GameConfig config = createBaseConfig();
            // Create a configuration with overlapping winning combinations
            Map<String, WinCombination> combinations = new HashMap<>();
            
            // Create a horizontal line combination
            WinCombination horizontal = new WinCombination();
            horizontal.setWhen("linear_symbols");
            horizontal.setCount(3);
            horizontal.setRewardMultiplier(2);
            horizontal.setCoveredAreas(Collections.singletonList(
                Arrays.asList("0:0", "0:1", "0:2")
            ));
            
            // Create a vertical line combination that overlaps
            WinCombination vertical = new WinCombination();
            vertical.setWhen("linear_symbols");
            vertical.setCount(3);
            vertical.setRewardMultiplier(2);
            vertical.setCoveredAreas(Collections.singletonList(
                Arrays.asList("0:0", "1:0", "2:0")
            ));
            
            combinations.put("horizontal_line", horizontal);
            combinations.put("vertical_line", vertical);
            config.setWinCombinations(combinations);

            ScratchGame game = new ScratchGame(config);
            GameResult result = game.play(100);

            // Verify that both combinations are applied
            assertNotNull(result.getAppliedWinningCombinations());
            assertTrue(result.getAppliedWinningCombinations().size() > 0);
        }

        @Test
        @DisplayName("Should throw exception for invalid position format")
        void testInvalidPositionFormat() {
            GameConfig config = createBaseConfig();
            WinCombination combination = new WinCombination();
            combination.setWhen("linear_symbols");
            combination.setCount(3);
            combination.setRewardMultiplier(2);
            combination.setCoveredAreas(Collections.singletonList(
                Arrays.asList("invalid_position")
            ));
            
            config.setWinCombinations(Collections.singletonMap("invalid", combination));

            assertThrows(IllegalStateException.class, () -> new ScratchGame(config).play(100),
                    "Should throw IllegalStateException for invalid position format");
        }

        @Test
        @DisplayName("Should throw exception for out of bounds position")
        void testOutOfBoundsPosition() {
            GameConfig config = createBaseConfig();
            WinCombination combination = new WinCombination();
            combination.setWhen("linear_symbols");
            combination.setCount(3);
            combination.setRewardMultiplier(2);
            combination.setCoveredAreas(Collections.singletonList(
                Arrays.asList("10:10") // Position outside matrix
            ));
            
            config.setWinCombinations(Collections.singletonMap("out_of_bounds", combination));

            assertThrows(IllegalStateException.class, () -> new ScratchGame(config).play(100),
                    "Should throw IllegalStateException for out of bounds position");
        }
    }

    @Nested
    @DisplayName("Reward Calculation Tests")
    class RewardCalculationTests {
        @Test
        @DisplayName("Should handle missing symbol configuration")
        void testMissingSymbolConfig() {
            GameConfig config = createBaseConfig();
            // Create a winning combination first
            Map<String, WinCombination> combinations = new HashMap<>();
            WinCombination combination = new WinCombination();
            combination.setWhen("same_symbols");
            combination.setCount(3);
            combination.setRewardMultiplier(2.0);
            combinations.put("same_symbols_3", combination);
            config.setWinCombinations(combinations);

            // Create a valid game instance first
            ScratchGame game = new ScratchGame(config);
            
            // Then remove the symbol after initialization
            config.getSymbols().remove("A");

            assertThrows(IllegalStateException.class, () -> game.play(100),
                    "Should throw IllegalStateException when symbol configuration is missing");
        }

        @Test
        @DisplayName("Should handle missing win combination configuration")
        void testMissingWinCombinationConfig() {
            GameConfig config = createBaseConfig();
            // Create a winning combination first
            Map<String, WinCombination> combinations = new HashMap<>();
            WinCombination combination = new WinCombination();
            combination.setWhen("same_symbols");
            combination.setCount(3);
            combination.setRewardMultiplier(2.0);
            combinations.put("same_symbols_3", combination);
            config.setWinCombinations(combinations);

            // Set up probabilities to ensure we get a winning combination
            List<GameConfig.StandardSymbolProbability> standardSymbols = new ArrayList<>();
            
            // Set up first row to have all A symbols
            for (int col = 0; col < 3; col++) {
                GameConfig.StandardSymbolProbability probability = new GameConfig.StandardSymbolProbability();
                probability.setRow(0);
                probability.setColumn(col);
                Map<String, Integer> symbolProbabilities = new HashMap<>();
                symbolProbabilities.put("A", 100); // 100% chance of getting symbol A
                probability.setSymbols(symbolProbabilities);
                standardSymbols.add(probability);
            }
            
            // Add probabilities for remaining positions
            for (int row = 1; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    GameConfig.StandardSymbolProbability probability = new GameConfig.StandardSymbolProbability();
                    probability.setRow(row);
                    probability.setColumn(col);
                    Map<String, Integer> symbolProbabilities = new HashMap<>();
                    symbolProbabilities.put("A", 100); // 100% chance of getting symbol A
                    probability.setSymbols(symbolProbabilities);
                    standardSymbols.add(probability);
                }
            }
            
            config.getProbabilities().setStandardSymbols(standardSymbols);

            // Create a valid game instance first
            ScratchGame game = new ScratchGame(config);
            
            // Then clear the combinations after initialization
            config.getWinCombinations().clear();

            assertThrows(IllegalStateException.class, () -> game.play(100),
                    "Should throw IllegalStateException when win combination configuration is missing");
        }

        @Test
        @DisplayName("Should handle reward calculation overflow")
        void testRewardCalculationOverflow() {
            GameConfig config = createBaseConfig();
            // Set extremely high multipliers to cause overflow
            config.getSymbols().get("A").setRewardMultiplier(Double.MAX_VALUE);
            config.getWinCombinations().get("same_symbols_3").setRewardMultiplier(Double.MAX_VALUE);

            assertThrows(ArithmeticException.class, () -> new ScratchGame(config).play(100),
                    "Should throw ArithmeticException for reward calculation overflow");
        }
    }

    @Nested
    @DisplayName("Bonus Symbol Tests")
    class BonusSymbolTests {
        @Test
        @DisplayName("Should handle missing bonus symbol configuration")
        void testMissingBonusSymbolConfig() {
            GameConfig config = createBaseConfig();
            config.getProbabilities().setBonusSymbols(null);

            ScratchGame game = new ScratchGame(config);
            GameResult result = game.play(100);
            assertNull(result.getAppliedBonusSymbol(),
                    "Should return null when bonus symbol configuration is missing");
        }

        @Test
        @DisplayName("Should handle invalid bonus symbol impact")
        void testInvalidBonusSymbolImpact() {
            GameConfig config = createBaseConfig();
            // Create a winning combination to ensure bonus is applied
            Map<String, WinCombination> combinations = new HashMap<>();
            WinCombination combination = new WinCombination();
            combination.setWhen("same_symbols");
            combination.setCount(3);
            combination.setRewardMultiplier(2.0);
            combinations.put("same_symbols_3", combination);
            config.setWinCombinations(combinations);

            // Add bonus symbol with invalid impact
            Symbol bonusSymbol = new Symbol();
            bonusSymbol.setType("bonus");
            bonusSymbol.setImpact("invalid_impact");
            config.getSymbols().put("BONUS", bonusSymbol);

            // Set bonus symbol probability to 100%
            GameConfig.BonusSymbolProbability bonusSymbols = new GameConfig.BonusSymbolProbability();
            Map<String, Integer> bonusProbabilities = new HashMap<>();
            bonusProbabilities.put("BONUS", 100);
            bonusSymbols.setSymbols(bonusProbabilities);
            config.getProbabilities().setBonusSymbols(bonusSymbols);

            assertThrows(IllegalStateException.class, () -> new ScratchGame(config).play(100),
                    "Should throw IllegalStateException for invalid bonus symbol impact");
        }

        @Test
        @DisplayName("Should handle invalid bonus multiplier")
        void testInvalidBonusMultiplier() {
            GameConfig config = createBaseConfig();
            // Create a winning combination to ensure bonus is applied
            Map<String, WinCombination> combinations = new HashMap<>();
            WinCombination combination = new WinCombination();
            combination.setWhen("same_symbols");
            combination.setCount(3);
            combination.setRewardMultiplier(2.0);
            combinations.put("same_symbols_3", combination);
            config.setWinCombinations(combinations);

            // Add bonus symbol with invalid multiplier
            Symbol bonusSymbol = new Symbol();
            bonusSymbol.setType("bonus");
            bonusSymbol.setImpact("multiply_reward");
            bonusSymbol.setRewardMultiplier(-1);
            config.getSymbols().put("BONUS", bonusSymbol);

            // Set bonus symbol probability to 100%
            GameConfig.BonusSymbolProbability bonusSymbols = new GameConfig.BonusSymbolProbability();
            Map<String, Integer> bonusProbabilities = new HashMap<>();
            bonusProbabilities.put("BONUS", 100);
            bonusSymbols.setSymbols(bonusProbabilities);
            config.getProbabilities().setBonusSymbols(bonusSymbols);

            assertThrows(IllegalStateException.class, () -> new ScratchGame(config).play(100),
                    "Should throw IllegalStateException for invalid bonus multiplier");
        }

        @Test
        @DisplayName("Should handle bonus calculation overflow")
        void testBonusCalculationOverflow() {
            GameConfig config = createBaseConfig();
            // Create a winning combination to ensure bonus is applied
            Map<String, WinCombination> combinations = new HashMap<>();
            WinCombination combination = new WinCombination();
            combination.setWhen("same_symbols");
            combination.setCount(3);
            combination.setRewardMultiplier(2.0);
            combinations.put("same_symbols_3", combination);
            config.setWinCombinations(combinations);

            // Add bonus symbol with overflow multiplier
            Symbol bonusSymbol = new Symbol();
            bonusSymbol.setType("bonus");
            bonusSymbol.setImpact("multiply_reward");
            bonusSymbol.setRewardMultiplier(Double.MAX_VALUE);
            config.getSymbols().put("BONUS", bonusSymbol);

            // Set bonus symbol probability to 100%
            GameConfig.BonusSymbolProbability bonusSymbols = new GameConfig.BonusSymbolProbability();
            Map<String, Integer> bonusProbabilities = new HashMap<>();
            bonusProbabilities.put("BONUS", 100);
            bonusSymbols.setSymbols(bonusProbabilities);
            config.getProbabilities().setBonusSymbols(bonusSymbols);

            assertThrows(ArithmeticException.class, () -> new ScratchGame(config).play(100),
                    "Should throw ArithmeticException for bonus calculation overflow");
        }
    }

    /**
     * Creates a base configuration for testing.
     * This configuration includes all necessary components for basic game functionality.
     */
    private GameConfig createBaseConfig() {
        GameConfig config = new GameConfig();
        config.setRows(3);
        config.setColumns(3);

        // Add symbols
        Map<String, Symbol> symbols = new HashMap<>();
        Symbol symbolA = new Symbol();
        symbolA.setType("standard");
        symbolA.setRewardMultiplier(1.0);
        symbols.put("A", symbolA);
        config.setSymbols(symbols);

        // Add win combinations
        Map<String, WinCombination> combinations = new HashMap<>();
        WinCombination sameSymbols = new WinCombination();
        sameSymbols.setWhen("same_symbols");
        sameSymbols.setCount(3);
        sameSymbols.setRewardMultiplier(2.0);
        combinations.put("same_symbols_3", sameSymbols);
        config.setWinCombinations(combinations);

        // Add probabilities
        GameConfig.Probabilities probabilities = new GameConfig.Probabilities();
        List<GameConfig.StandardSymbolProbability> standardSymbols = new ArrayList<>();
        GameConfig.StandardSymbolProbability probability = new GameConfig.StandardSymbolProbability();
        probability.setRow(0);
        probability.setColumn(0);
        Map<String, Integer> symbolProbabilities = new HashMap<>();
        symbolProbabilities.put("A", 100);
        probability.setSymbols(symbolProbabilities);
        standardSymbols.add(probability);
        probabilities.setStandardSymbols(standardSymbols);

        GameConfig.BonusSymbolProbability bonusSymbols = new GameConfig.BonusSymbolProbability();
        Map<String, Integer> bonusProbabilities = new HashMap<>();
        bonusProbabilities.put("MISS", 100);
        bonusSymbols.setSymbols(bonusProbabilities);
        probabilities.setBonusSymbols(bonusSymbols);

        config.setProbabilities(probabilities);

        return config;
    }
} 