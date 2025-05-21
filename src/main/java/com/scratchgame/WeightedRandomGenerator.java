package com.scratchgame;

import java.util.*;

/**
 * Implements the Alias Method (Vose's algorithm) for efficient weighted random selection
 * with O(1) selection time after O(n) preprocessing.
 */
public class WeightedRandomGenerator {
    private final int[] alias;
    private final double[] probability;
    private final Random random;
    private final String[] symbols;
    
    public WeightedRandomGenerator(Map<String, Integer> symbols) {
        this.random = new Random();
        int n = symbols.size();
        this.symbols = symbols.keySet().toArray(new String[0]);
        this.alias = new int[n];
        this.probability = new double[n];
        
        // Calculate total weight
        int totalWeight = symbols.values().stream().mapToInt(Integer::intValue).sum();
        
        // Check for invalid probability distribution
        if (totalWeight <= 0) {
            throw new IllegalStateException("Invalid probability distribution: total weight must be positive");
        }
        
        // Create normalized probabilities and work lists
        double[] normalizedProbs = new double[n];
        List<Integer> small = new ArrayList<>();
        List<Integer> large = new ArrayList<>();
        
        // Normalize probabilities
        int i = 0;
        for (Map.Entry<String, Integer> entry : symbols.entrySet()) {
            normalizedProbs[i] = (double) entry.getValue() * n / totalWeight;
            if (normalizedProbs[i] < 1.0) {
                small.add(i);
            } else {
                large.add(i);
            }
            i++;
        }
        
        // Create alias table
        while (!small.isEmpty() && !large.isEmpty()) {
            int less = small.remove(small.size() - 1);
            int more = large.remove(large.size() - 1);
            
            probability[less] = normalizedProbs[less];
            alias[less] = more;
            
            normalizedProbs[more] = (normalizedProbs[more] + normalizedProbs[less]) - 1.0;
            if (normalizedProbs[more] < 1.0) {
                small.add(more);
            } else {
                large.add(more);
            }
        }
        
        // Handle any remaining entries
        for (int idx : small) {
            probability[idx] = 1.0;
        }
        for (int idx : large) {
            probability[idx] = 1.0;
        }
    }
    
    public String nextSymbol() {
        int column = random.nextInt(probability.length);
        boolean coinToss = random.nextDouble() < probability[column];
        return symbols[coinToss ? column : alias[column]];
    }
} 