# TicTacToe AI

This project aims to build a bot that can play a symmetric [mnk-Game](https://en.wikipedia.org/wiki/M,n,k-game) at a high level.

# Features
- Search
    - Principal Variation Search (PVS)
    - Reverse Futility Pruning (RFP)
    - Transposition Table
    - Null Move Pruning (NMP)
    - Internal Iterative Reduction (IIR)
- Move generation
    - Bitboards
- Move order
    - Hash moves
    - Killer moves
    - Central Bonus

# Testing
There are two kinds of testing in this project.

## Game Manager
This is the main testing platform to test functional and non-functional changes to the engine. It uses an [SPRT](https://www.chessprogramming.org/Sequential_Probability_Ratio_Test) test.
## Unit Tests
Unit tests are used to verify that the board is working and some basic search verification.

# TODO
- GUI for the SPRT test
- Finish the SPSA algorithm