# Scratch Game

A Java implementation of a scratch game that generates a matrix of symbols based on probabilities and calculates rewards based on winning combinations.

## Requirements

- JDK 1.8 or higher
- Maven 3.6 or higher

## Building the Project

To build the project, run:

```bash
mvn clean package
```

This will create a JAR file in the `target` directory named `scratch-game-1.0-SNAPSHOT-jar-with-dependencies.jar`.

## Running the Game

To run the game, use the following command:

```bash
java -jar target/scratch-game-1.0-SNAPSHOT-jar-with-dependencies.jar --config config.json --betting-amount 100
```

### Parameters

- `--config`: Path to the configuration file (required)
- `--betting-amount`: The amount to bet (required)

## Running Tests

To run the tests, use:

```bash
mvn test
```

## Project Structure

- `src/main/java/com/scratchgame/model/`: Contains the data models
  - `Symbol.java`: Represents a game symbol
  - `WinCombination.java`: Represents a winning combination
  - `GameConfig.java`: Represents the game configuration
  - `GameResult.java`: Represents the game output
- `src/main/java/com/scratchgame/ScratchGame.java`: Main game logic
- `src/test/java/com/scratchgame/ScratchGameTest.java`: Test cases

## Configuration File Format

The configuration file should be in JSON format and include:

- Matrix dimensions (rows and columns)
- Symbol definitions with reward multipliers
- Probability configurations for standard and bonus symbols
- Winning combinations with their rules and rewards

See the provided `config.json` file for an example configuration. 