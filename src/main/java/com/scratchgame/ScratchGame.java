package com.scratchgame;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scratchgame.model.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ScratchGame {
    private final GameConfig config;
    private final Random random;
    private final Map<String, WeightedRandomGenerator> symbolGenerators = new HashMap<>();
    private List<List<String>> testMatrix = null;

    // To improve testing, allows injecting a test matrix. In game, this is not used.
    public void setTestMatrix(List<List<String>> matrix) {
        this.testMatrix = matrix;
    }
    
    public ScratchGame(GameConfig config) {
        validateConfig(config);
        this.config = config;
        this.random = new Random();
        
        // Precompute WeightedRandomGenerators for each position
        for (GameConfig.StandardSymbolProbability probability : config.getProbabilities().getStandardSymbols()) {
            String key = probability.getRow() + ":" + probability.getColumn();
            symbolGenerators.put(key, new WeightedRandomGenerator(probability.getSymbols()));
        }
        
        // Also create one for bonus symbols if they exist
        if (config.getProbabilities().getBonusSymbols() != null) {
            symbolGenerators.put("bonus", new WeightedRandomGenerator(
                config.getProbabilities().getBonusSymbols().getSymbols()));
        }
    }

    private void validateConfig(GameConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Game configuration cannot be null");
        }
        if (config.getRows() <= 0 || config.getColumns() <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive");
        }
        if (config.getSymbols() == null || config.getSymbols().isEmpty()) {
            throw new IllegalArgumentException("Game must have at least one symbol");
        }
        if (config.getWinCombinations() == null || config.getWinCombinations().isEmpty()) {
            throw new IllegalArgumentException("Game must have at least one winning combination");
        }
        if (config.getProbabilities() == null || 
            config.getProbabilities().getStandardSymbols() == null || 
            config.getProbabilities().getStandardSymbols().isEmpty()) {
            throw new IllegalArgumentException("Game must have probability configurations");
        }
    }

    public GameResult play(double betAmount) {
        // Handle invalid bet amounts
        if (betAmount <= 0) {
            GameResult result = new GameResult();
            result.setMatrix(generateMatrix());
            result.setReward(0);
            result.setAppliedWinningCombinations(new HashMap<>());
            result.setAppliedBonusSymbol(null);
            return result;
        }

        // Generate matrix
        List<List<String>> matrix = generateMatrix();
        
        // Find winning combinations
        Map<String, List<String>> winningCombinations = findWinningCombinations(matrix);
        
        // Calculate reward
        double reward = calculateReward(matrix, winningCombinations, betAmount);
        
        // Apply bonus symbol if there are winning combinations
        String bonusSymbol = null;
        if (reward > 0) {
            bonusSymbol = applyBonusSymbol(matrix, reward);
        }
        
        // Create result
        GameResult result = new GameResult();
        result.setMatrix(matrix);
        result.setReward(reward);
        result.setAppliedWinningCombinations(winningCombinations);
        result.setAppliedBonusSymbol(bonusSymbol);
        
        return result;
    }

    private List<List<String>> generateMatrix() {
        if (testMatrix != null) {
            return testMatrix;
        }
        
        List<List<String>> matrix = new ArrayList<>();
        
        for (int row = 0; row < config.getRows(); row++) {
            List<String> rowList = new ArrayList<>();
            for (int col = 0; col < config.getColumns(); col++) {
                String symbol = generateSymbolForPosition(row, col);
                rowList.add(symbol);
            }
            matrix.add(rowList);
        }
        
        return matrix;
    }

    private String generateSymbolForPosition(int row, int col) {
        String key = row + ":" + col;
        WeightedRandomGenerator generator = symbolGenerators.get(key);
        
        if (generator == null) {
            // Fallback to default if specific position doesn't have a configuration
            generator = symbolGenerators.get("0:0");
        }
        
        String symbol = generator.nextSymbol();
        if (!config.getSymbols().containsKey(symbol)) {
            throw new IllegalStateException("Symbol " + symbol + " not found in configuration");
        }
        
        return symbol;
    }

    private Map<String, List<String>> findWinningCombinations(List<List<String>> matrix) {
        Map<String, List<String>> winningCombinations = new HashMap<>();
        Set<String> usedPositions = new HashSet<>();
        
        // Check each symbol in the matrix
        for (int row = 0; row < matrix.size(); row++) {
            for (int col = 0; col < matrix.get(row).size(); col++) {
                String symbol = matrix.get(row).get(col);
                System.out.println("Checking symbol at [" + row + "," + col + "]: " + symbol);
                if (!config.getSymbols().containsKey(symbol)) {
                    throw new IllegalStateException("Invalid symbol found in matrix: " + symbol);
                }
                if (config.getSymbols().get(symbol).getType().equals("standard")) {
                    List<String> combinations = findWinningCombinationsForSymbol(matrix, symbol, usedPositions);
                    if (!combinations.isEmpty()) {
                        System.out.println("Found winning combinations for symbol " + symbol + ": " + combinations);
                        winningCombinations.put(symbol, combinations);
                    }
                }
            }
        }
        
        return winningCombinations;
    }

    private List<String> findWinningCombinationsForSymbol(List<List<String>> matrix, String symbol, Set<String> usedPositions) {
        List<String> combinations = new ArrayList<>();
        
        // Check each win combination
        for (Map.Entry<String, WinCombination> entry : config.getWinCombinations().entrySet()) {
            String combinationName = entry.getKey();
            WinCombination combination = entry.getValue();
            
            if (combination.getWhen().equals("same_symbols")) {
                // Count occurrences of the symbol
                int count = 0;
                for (List<String> row : matrix) {
                    for (String s : row) {
                        if (s.equals(symbol)) {
                            count++;
                        }
                    }
                }
                System.out.println("Found " + count + " occurrences of symbol " + symbol);
                
                if (count >= combination.getCount()) {
                    combinations.add(combinationName);
                }
            } else if (combination.getWhen().equals("linear_symbols")) {
                // Check each covered area for linear combinations
                if (combination.getCoveredAreas() != null) {
                    for (List<String> area : combination.getCoveredAreas()) {
                        boolean isValid = true;
                        for (String position : area) {
                            try {
                                String[] coords = position.split(":");
                                if (coords.length != 2) {
                                    throw new IllegalStateException("Invalid position format: " + position);
                                }
                                int row = Integer.parseInt(coords[0]);
                                int col = Integer.parseInt(coords[1]);
                                
                                if (row < 0 || row >= matrix.size() || col < 0 || col >= matrix.get(0).size()) {
                                    throw new IllegalStateException("Position out of bounds: " + position);
                                }
                                
                                if (!matrix.get(row).get(col).equals(symbol)) {
                                    isValid = false;
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                throw new IllegalStateException("Invalid position format: " + position);
                            }
                        }
                        if (isValid) {
                            combinations.add(combinationName);
                            break;
                        }
                    }
                }
            }
        }
        
        return combinations;
    }

    private double calculateReward(List<List<String>> matrix, Map<String, List<String>> winningCombinations, double betAmount) {
        if (betAmount <= 0) {
            return 0;
        }

        // Validate that win combinations exist in configuration
        if (config.getWinCombinations() == null || config.getWinCombinations().isEmpty()) {
            throw new IllegalStateException("Win combinations configuration is missing");
        }

        double totalReward = 0;
        
        for (Map.Entry<String, List<String>> entry : winningCombinations.entrySet()) {
            String symbol = entry.getKey();
            List<String> combinations = entry.getValue();
            
            Symbol symbolConfig = config.getSymbols().get(symbol);
            if (symbolConfig == null) {
                //System.out.println("Symbol configuration not found for: " + symbol);
                throw new IllegalStateException("Symbol configuration not found: " + symbol);
            }
            
            double symbolReward = betAmount * symbolConfig.getRewardMultiplier();
            
            for (String combination : combinations) {
                WinCombination winConfig = config.getWinCombinations().get(combination);
                if (winConfig == null) {
                    //System.out.println("Win combination configuration not found for: " + combination);
                    throw new IllegalStateException("Win combination configuration not found: " + combination);
                }
                symbolReward *= winConfig.getRewardMultiplier();
                
                // Check for overflow
                if (Double.isInfinite(symbolReward) || Double.isNaN(symbolReward)) {
                    throw new ArithmeticException("Reward calculation overflow for symbol: " + symbol);
                }
            }
            
            totalReward += symbolReward;
        }
        
        return totalReward;
    }

    private String applyBonusSymbol(List<List<String>> matrix, double currentReward) {
        GameConfig.BonusSymbolProbability bonusConfig = config.getProbabilities().getBonusSymbols();
        if (bonusConfig == null || bonusConfig.getSymbols() == null || bonusConfig.getSymbols().isEmpty()) {
            return null;
        }

        // Generate random bonus symbol based on probabilities
        Map<String, Integer> bonusSymbols = bonusConfig.getSymbols();
        int totalProbability = bonusSymbols.values().stream().mapToInt(Integer::intValue).sum();
        if (totalProbability <= 0) {
            return null;
        }

        int randomValue = random.nextInt(totalProbability);
        
        int currentSum = 0;
        String selectedSymbol = null;
        for (Map.Entry<String, Integer> entry : bonusSymbols.entrySet()) {
            currentSum += entry.getValue();
            if (randomValue < currentSum) {
                selectedSymbol = entry.getKey();
                break;
            }
        }
        
        if (selectedSymbol != null && !selectedSymbol.equals("MISS")) {
            Symbol symbol = config.getSymbols().get(selectedSymbol);
            if (symbol == null) {
                throw new IllegalStateException("Bonus symbol configuration not found: " + selectedSymbol);
            }
            
            if (symbol.getImpact() == null) {
                throw new IllegalStateException("Bonus symbol impact not defined: " + selectedSymbol);
            }
            
            if (symbol.getImpact().equals("multiply_reward")) {
                if (symbol.getRewardMultiplier() <= 0) {
                    throw new IllegalStateException("Invalid bonus multiplier for symbol: " + selectedSymbol);
                }
                currentReward *= symbol.getRewardMultiplier();
                
                // Check for overflow
                if (Double.isInfinite(currentReward) || Double.isNaN(currentReward)) {
                    throw new ArithmeticException("Bonus reward calculation overflow");
                }
            } else if (symbol.getImpact().equals("extra_bonus")) {
                if (symbol.getExtra() < 0) {
                    throw new IllegalStateException("Invalid extra bonus for symbol: " + selectedSymbol);
                }
                currentReward += symbol.getExtra();
                
                // Check for overflow
                if (Double.isInfinite(currentReward) || Double.isNaN(currentReward)) {
                    throw new ArithmeticException("Bonus reward calculation overflow");
                }
            } else {
                throw new IllegalStateException("Unknown bonus impact type: " + symbol.getImpact());
            }
        }
        
        return selectedSymbol;
    }

    public static void main(String[] args) {
        if (args.length != 4 || !args[0].equals("--config") || !args[2].equals("--betting-amount")) {
            System.out.println("Usage: java -jar scratch-game.jar --config <config-file> --betting-amount <amount>");
            System.exit(1);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            GameConfig config = mapper.readValue(new File(args[1]), GameConfig.class);
            double betAmount = Double.parseDouble(args[3]);

            ScratchGame game = new ScratchGame(config);
            GameResult result = game.play(betAmount);

            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        } catch (IOException e) {
            System.err.println("Error reading config file: " + e.getMessage());
            System.exit(1);
        } catch (NumberFormatException e) {
            System.err.println("Invalid betting amount: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException e) {
            System.err.println("Game error: " + e.getMessage());
            System.exit(1);
        }
    }
} 