package com.scratchgame.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class GameConfig {
    private int columns = 3;
    private int rows = 3;
    private Map<String, Symbol> symbols;
    
    @JsonProperty("win_combinations")
    private Map<String, WinCombination> winCombinations;
    
    private Probabilities probabilities;

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public Map<String, Symbol> getSymbols() {
        return symbols;
    }

    public void setSymbols(Map<String, Symbol> symbols) {
        this.symbols = symbols;
    }

    public Map<String, WinCombination> getWinCombinations() {
        return winCombinations;
    }

    public void setWinCombinations(Map<String, WinCombination> winCombinations) {
        this.winCombinations = winCombinations;
    }

    public Probabilities getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(Probabilities probabilities) {
        this.probabilities = probabilities;
    }

    public static class Probabilities {
        @JsonProperty("standard_symbols")
        private List<StandardSymbolProbability> standardSymbols;
        
        @JsonProperty("bonus_symbols")
        private BonusSymbolProbability bonusSymbols;

        public List<StandardSymbolProbability> getStandardSymbols() {
            return standardSymbols;
        }

        public void setStandardSymbols(List<StandardSymbolProbability> standardSymbols) {
            this.standardSymbols = standardSymbols;
        }

        public BonusSymbolProbability getBonusSymbols() {
            return bonusSymbols;
        }

        public void setBonusSymbols(BonusSymbolProbability bonusSymbols) {
            this.bonusSymbols = bonusSymbols;
        }
    }

    public static class StandardSymbolProbability {
        private int column;
        private int row;
        private Map<String, Integer> symbols;

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public Map<String, Integer> getSymbols() {
            return symbols;
        }

        public void setSymbols(Map<String, Integer> symbols) {
            this.symbols = symbols;
        }
    }

    public static class BonusSymbolProbability {
        private Map<String, Integer> symbols;

        public Map<String, Integer> getSymbols() {
            return symbols;
        }

        public void setSymbols(Map<String, Integer> symbols) {
            this.symbols = symbols;
        }
    }
} 