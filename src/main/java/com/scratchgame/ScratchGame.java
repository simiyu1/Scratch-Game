package com.scratchgame;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scratchgame.model.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ScratchGame {
    private final GameConfig config;
    private final Random random;

    public ScratchGame(GameConfig config) {
        this.config = config;
        this.random = new Random();
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
        // Find probability configuration for this position
        GameConfig.StandardSymbolProbability probability = config.getProbabilities()
                .getStandardSymbols()
                .stream()
                .filter(p -> p.getRow() == row && p.getColumn() == col)
                .findFirst()
                .orElse(config.getProbabilities().getStandardSymbols().get(0));

        // Calculate total probability
        int totalProbability = probability.getSymbols().values().stream().mapToInt(Integer::intValue).sum();
        
        // Generate random number
        int randomValue = random.nextInt(totalProbability);
        
        // Find symbol based on probability
        int currentSum = 0;
        for (Map.Entry<String, Integer> entry : probability.getSymbols().entrySet()) {
            currentSum += entry.getValue();
            if (randomValue < currentSum) {
                return entry.getKey();
            }
        }
        
        // This should never happen, but just in case
        return probability.getSymbols().keySet().iterator().next();
    }

    private Map<String, List<String>> findWinningCombinations(List<List<String>> matrix) {
        Map<String, List<String>> winningCombinations = new HashMap<>();
        
        // Check each symbol in the matrix
        for (int row = 0; row < matrix.size(); row++) {
            for (int col = 0; col < matrix.get(row).size(); col++) {
                String symbol = matrix.get(row).get(col);
                if (config.getSymbols().get(symbol).getType().equals("standard")) {
                    List<String> combinations = findWinningCombinationsForSymbol(matrix, symbol);
                    if (!combinations.isEmpty()) {
                        winningCombinations.put(symbol, combinations);
                    }
                }
            }
        }
        
        return winningCombinations;
    }

    private List<String> findWinningCombinationsForSymbol(List<List<String>> matrix, String symbol) {
        List<String> combinations = new ArrayList<>();
        
        // Count occurrences of the symbol
        int count = 0;
        for (List<String> row : matrix) {
            for (String s : row) {
                if (s.equals(symbol)) {
                    count++;
                }
            }
        }
        
        // Check same symbol combinations
        for (Map.Entry<String, WinCombination> entry : config.getWinCombinations().entrySet()) {
            WinCombination combination = entry.getValue();
            if (combination.getWhen().equals("same_symbols") && count >= combination.getCount()) {
                combinations.add(entry.getKey());
            }
        }
        
        // Check linear combinations
        for (Map.Entry<String, WinCombination> entry : config.getWinCombinations().entrySet()) {
            WinCombination combination = entry.getValue();
            if (combination.getWhen().equals("linear_symbols")) {
                for (List<String> area : combination.getCoveredAreas()) {
                    boolean isWinning = true;
                    for (String position : area) {
                        String[] coords = position.split(":");
                        int r = Integer.parseInt(coords[0]);
                        int c = Integer.parseInt(coords[1]);
                        if (!matrix.get(r).get(c).equals(symbol)) {
                            isWinning = false;
                            break;
                        }
                    }
                    if (isWinning) {
                        combinations.add(entry.getKey());
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

        double totalReward = 0;
        
        for (Map.Entry<String, List<String>> entry : winningCombinations.entrySet()) {
            String symbol = entry.getKey();
            List<String> combinations = entry.getValue();
            
            double symbolReward = betAmount * config.getSymbols().get(symbol).getRewardMultiplier();
            
            for (String combination : combinations) {
                symbolReward *= config.getWinCombinations().get(combination).getRewardMultiplier();
            }
            
            totalReward += symbolReward;
        }
        
        return totalReward;
    }

    private String applyBonusSymbol(List<List<String>> matrix, double currentReward) {
        // Generate random bonus symbol based on probabilities
        Map<String, Integer> bonusSymbols = config.getProbabilities().getBonusSymbols().getSymbols();
        int totalProbability = bonusSymbols.values().stream().mapToInt(Integer::intValue).sum();
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
            if (symbol.getImpact().equals("multiply_reward")) {
                currentReward *= symbol.getRewardMultiplier();
            } else if (symbol.getImpact().equals("extra_bonus")) {
                currentReward += symbol.getExtra();
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
        }
    }
} 