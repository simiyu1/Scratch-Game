# Scratch Game Implementation

[![Coverage Status](https://coveralls.io/repos/github/simiyu1/Scratch-Game/badge.svg?branch=feat/add-coverage-report)](https://coveralls.io/github/simiyu1/Scratch-Game?branch=feat/add-coverage-report)

## Overview
This is a Java implementation of a scratch card game that generates a matrix of symbols, calculates winning combinations, and determines rewards based on betting amounts and bonus symbols.

## Core Features
1. **Matrix Generation**
   - Configurable dimensions (rows and columns)
   - Probability-based symbol generation
   - Support for standard and bonus symbols
   - Position-specific probability configurations

2. **Winning Combinations**
   - Same symbol combinations (e.g., 3 or more of the same symbol)
   - Linear combinations (horizontal, vertical, diagonal)
   - Overlapping combinations support
   - Configurable reward multipliers

3. **Reward Calculation**
   - Base reward based on symbol multipliers
   - Win combination multipliers
   - Bonus symbol effects (multiply_reward, extra_bonus)
   - Overflow protection for large numbers

4. **Configuration System**
   - JSON-based configuration
   - Symbol definitions and probabilities
   - Win combination rules
   - Bonus symbol settings

## Implementation Details

### Game Flow
1. **Initialization**
   - Load and validate game configuration
   - Set up random number generator
   - Validate matrix dimensions and symbol configurations

2. **Matrix Generation**
   - Generate symbols based on position-specific probabilities
   - Validate generated symbols against configuration
   - Handle missing or invalid probability configurations

3. **Winning Combination Detection**
   - Check for same symbol combinations
   - Validate linear combinations
   - Handle overlapping winning patterns
   - Track used positions to prevent double-counting

4. **Reward Calculation**
   - Calculate base rewards for winning symbols
   - Apply win combination multipliers
   - Process bonus symbol effects
   - Handle arithmetic overflow cases

### Error Handling
1. **Configuration Validation**
   - Null checks for required configurations
   - Dimension validation
   - Symbol configuration validation
   - Probability distribution validation

2. **Runtime Validation**
   - Invalid symbol detection
   - Out-of-bounds position handling
   - Missing configuration detection
   - Arithmetic overflow protection

## Test Coverage

### Unit Tests
1. **Configuration Tests**
   - Null configuration handling
   - Invalid matrix dimensions
   - Missing symbol configurations
   - Missing win combination configurations
   - Missing probability configurations

2. **Matrix Generation Tests**
   - Invalid probability distribution
   - Symbol not found in configuration
   - Position-specific probability handling

3. **Winning Combination Tests**
   - Overlapping winning combinations
   - Invalid position format
   - Out-of-bounds position handling
   - Linear combination validation

4. **Reward Calculation Tests**
   - Missing symbol configuration
   - Missing win combination configuration
   - Reward calculation overflow
   - Bonus symbol handling

### Edge Cases Handled
1. **Configuration Edge Cases**
   - Empty configurations
   - Invalid probability distributions
   - Missing required fields
   - Invalid symbol types

2. **Gameplay Edge Cases**
   - Zero or negative bet amounts
   - Missing win combinations during gameplay
   - Invalid bonus symbol impacts
   - Arithmetic overflow in calculations

3. **Matrix Edge Cases**
   - Single symbol matrix
   - Maximum dimension matrix
   - Invalid position formats
   - Out-of-bounds positions

## Future Improvements

### Performance Optimizations
1. **Matrix Generation**
   - Implement parallel processing for large matrices
   - Optimize symbol probability calculations
   - Cache frequently used configurations

2. **Winning Combination Detection**
   - Implement more efficient pattern matching
   - Optimize position tracking
   - Add support for complex patterns

### Feature Enhancements
1. **Game Mechanics**
   - Add support for progressive jackpots
   - Implement multiple bonus symbol types
   - Add special symbol effects
   - Support for dynamic win combinations

2. **Configuration System**
   - Add support for dynamic configuration updates
   - Implement configuration validation rules
   - Add support for custom probability distributions
   - Implement configuration versioning

3. **User Experience**
   - Add detailed game statistics
   - Implement game history tracking
   - Add support for custom themes
   - Implement save/load game state

### Code Quality Improvements
1. **Architecture**
   - Implement proper dependency injection
   - Add support for different game modes
   - Implement proper event system
   - Add support for plugins

2. **Testing**
   - Add performance benchmarks
   - Implement stress testing
   - Add integration tests
   - Implement property-based testing

3. **Documentation**
   - Add API documentation
   - Create user guide
   - Add configuration guide
   - Document testing strategy

## Usage

### Building the Project
```bash
mvn clean install
```

### Running the Game
```bash
java -jar target/scratch-game-1.0-SNAPSHOT-jar-with-dependencies.jar --config <config-file> --betting-amount <amount>
```

### Configuration Format
```json
{
  "rows": 3,
  "columns": 3,
  "symbols": {
    "A": {
      "type": "standard",
      "rewardMultiplier": 1.0
    }
  },
  "winCombinations": {
    "same_symbols_3": {
      "when": "same_symbols",
      "count": 3,
      "rewardMultiplier": 2.0
    }
  },
  "probabilities": {
    "standardSymbols": [
      {
        "row": 0,
        "column": 0,
        "symbols": {
          "A": 100
        }
      }
    ],
    "bonusSymbols": {
      "symbols": {
        "MISS": 100
      }
    }
  }
}
```

## Contributing (Not Accepting contributions, but for feedback..)
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Project Structure and Configuration

### Project Layout
```
scratchgame/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── scratchgame/
│   │               ├── model/
│   │               │   ├── Symbol.java           # Symbol data model
│   │               │   ├── WinCombination.java   # Win combination model
│   │               │   ├── GameConfig.java       # Game configuration model
│   │               │   └── GameResult.java       # Game result model
│   │               └── ScratchGame.java          # Main game logic
│   └── test/
│       └── java/
│           └── com/
│               └── scratchgame/
│                   ├── ScratchGameTest.java      # Basic game tests
│                   └── ScratchGameEdgeCasesTest.java  # Edge case tests
├── config.json                                   # Game configuration
├── pom.xml                                       # Maven configuration
└── README.md                                     # This file
```

### Configuration Details

#### 1. Matrix Configuration
```json
{
  "rows": 3,        // Number of rows in the matrix
  "columns": 3      // Number of columns in the matrix
}
```

#### 2. Symbol Configuration
```json
{
  "symbols": {
    "A": {
      "type": "standard",           // Symbol type (standard or bonus)
      "rewardMultiplier": 1.0       // Base reward multiplier
    },
    "B": {
      "type": "bonus",
      "impact": "multiply_reward",  // Bonus impact type
      "extra": 2                    // Extra multiplier for bonus
    }
  }
}
```

#### 3. Win Combination Configuration
```json
{
  "winCombinations": {
    "same_symbols_3": {
      "when": "same_symbols",       // Combination type
      "count": 3,                   // Required count
      "rewardMultiplier": 2.0       // Win multiplier
    },
    "horizontal_1": {
      "when": "linear_symbols",     // Linear combination
      "covered_areas": [            // Positions to check
        ["0:0", "0:1", "0:2"]
      ],
      "rewardMultiplier": 3.0
    }
  }
}
```

#### 4. Probability Configuration
```json
{
  "probabilities": {
    "standardSymbols": [
      {
        "row": 0,
        "column": 0,
        "symbols": {
          "A": 50,                  // 50% chance for symbol A
          "B": 50                   // 50% chance for symbol B
        }
      }
    ],
    "bonusSymbols": {
      "symbols": {
        "MISS": 100                // 100% chance for MISS bonus
      }
    }
  }
}
```

### Running the Project

#### Prerequisites
- JDK 1.8 or higher
- Maven 3.6 or higher

#### Build Steps
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd scratchgame
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the game:
   ```bash
   java -jar target/scratch-game-1.0-SNAPSHOT-jar-with-dependencies.jar \
     --config config.json \
     --betting-amount 100
   ```

#### Command Line Arguments
- `--config`: Path to the configuration file (required)
- `--betting-amount`: The amount to bet (required)

#### Example Output
```json
{
  "matrix": [
    ["A", "B", "A"],
    ["B", "A", "B"],
    ["A", "B", "A"]
  ],
  "reward": 100.0,
  "applied_winning_combinations": {
    "same_symbols_3": ["A"],
    "horizontal_1": ["A"]
  },
  "applied_bonus_symbol": "MISS"
}
```

### Testing

#### Running Tests
```bash
mvn test
```

#### Test Categories
1. **Basic Game Tests** (`ScratchGameTest.java`)
   - Matrix generation
   - Basic win combinations
   - Reward calculations

2. **Edge Case Tests** (`ScratchGameEdgeCasesTest.java`)
   - Configuration validation
   - Error handling
   - Edge cases

#### Test Configuration
- Test configurations are in `src/test/resources/`
- Each test category has its own configuration file
- Edge case tests use specific configurations to test error conditions 

#### Running Tests
- Run a specific test class:
    `Runmvn test -Dtest=YourTestClassName`
- Example
    `mvn test -Dtest=ScratchGameCalculateRewardTest`
- Run all tests:
    `mvn test`

#### Generating Coverage Reports
- Run the tests first to collect coverage data:
      ` mvn test`
- Generate a JaCoCo coverage report:
      ` mvn jacoco:report`
This creates a coverage report in target/site/jacoco

#### Viewing Coverage Reports Locally
- Start a local web server to view the reports:
      `python3 -m http.server 8000 -d target/site/jacoco`
- Open a web browser and navigate to:
      `http://localhost:8000`
- Navigate through the report:
  - Click on package names to view classes
  - Click on classes to view methods and their coverage
  - Click on method names to view source code with coverage highlighting
  - To stop the server, press Ctrl+C in the terminal