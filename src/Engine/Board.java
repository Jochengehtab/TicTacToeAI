/*
  This file is part of the TicTacToe AI written by Jochengehtab

  Copyright (C) 2024-2025 Jochengehtab

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package src.Engine;

public class Board {
    private final byte[][] board;
    private final int size;
    public int offset;
    public final byte NO_SIDE = 0;
    public final byte X_SIDE = 1;
    public final byte O_SIDE = 2;
    private int winningSize;
    private int freeSquares;
    private byte sideToMove = 1;

    public Board(int size, int offset) {
        board = new byte[size][size];
        this.size = size;
        this.offset = offset;
        this.winningSize = size - offset;
        this.freeSquares = size * size;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = NO_SIDE;
            }
        }
    }

    /**
     * Makes a move on the board
     * @param move The move array
     */
    public void makeMove(int[] move) {
        board[move[0]][move[1]] = this.sideToMove;
        updateTurn();
        this.freeSquares--;
    }

    /**
     * Makes a move on the board
     * @param x The X-Axis
     * @param y The Y-Axis
     */
    public void makeMove(int x, int y) {
        board[x][y] = this.sideToMove;
        updateTurn();
        this.freeSquares--;
    }

    public void makeNullMove() {
        updateTurn();
    }

    /**
     * Unmakes a move on the board
     * @param move The move array
     */
    public void unmakeMove(int[] move) {
        board[move[0]][move[1]] = NO_SIDE;
        updateTurn();
        this.freeSquares++;
    }

    /**
     * Unmakes a move on the board
     * @param x The X-Axis
     * @param y The Y-Axis
     */
    public void unmakeMove(int x, int y) {
        board[x][y] = 0;
        updateTurn();
        this.freeSquares++;
    }

    public void unmakeNullMove() {
        updateTurn();
    }

    public boolean hasWinWithFurtherOffset(int offset, byte side) {

        int tempOffset = this.offset;
        this.offset += offset;
        this.winningSize = this.size - this.offset;

        if (hasRowColumnWin(side) || hasDiagonalWin(side)) {
            this.offset = tempOffset;
            this.winningSize = this.size - this.offset;
            return true;
        }

        this.offset = tempOffset;
        this.winningSize = this.size - this.offset;
        return false;
    }

    public int[][] generateLegalMoves() {

        int[][] legalMoves = new int[freeSquares][2];

        int counter = 0;
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                byte token = board[i][j];
                if (token == NO_SIDE) {
                    legalMoves[counter][0] = i;
                    legalMoves[counter][1] = j;
                    counter++;
                }
            }
        }

        return legalMoves;
    }

    /**
     * This resets the board
     */
    public void reset() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = NO_SIDE;
            }
        }

        this.sideToMove = X_SIDE;
    }

    /**
     * Updates the move with an XOR since it can only be 1 or 2
     */
    public void updateTurn() {
        this.sideToMove = (byte) (this.sideToMove ^ 3);
    }

    public boolean hasRowColGapWin(int gapSize, byte side) {
        for (int i = 0; i < this.size; i++) {

            int isPlacedRow = 0;
            int isPlacedColumn = 0;
            int gapCountRow = 0;
            int gapCountColum = 0;
            for (int j = 0; j < this.size; j++) {

                if (board[i][j] != side) {
                    if (gapCountRow == gapSize) {
                        isPlacedRow = 0;
                    } else{
                        gapCountRow++;
                        isPlacedRow++;
                    }
                    continue;
                } else {
                    isPlacedRow++;
                    gapCountRow = 0;
                }

                if (isPlacedRow == this.winningSize) {
                    return true;
                }
            }

            for (int j = 0; j < this.size; j++) {

                if (board[j][i] != side) {
                    if (gapCountColum == gapSize) {
                        isPlacedColumn = 0;
                    } else{
                        gapCountColum++;
                        isPlacedColumn++;
                    }
                    continue;
                } else {
                    isPlacedColumn++;
                    gapCountColum = 0;
                }

                if (isPlacedColumn == this.winningSize) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasRowColumnWin(byte side) {
        for (int i = 0; i < this.size; i++) {

            int isPlacedRow = 0;
            int isPlacedColumn = 0;
            for (int j = 0; j < this.size; j++) {

                if (board[i][j] != side) {
                    isPlacedRow = 0;
                    continue;
                } else {
                    isPlacedRow++;
                }

                if (isPlacedRow == this.winningSize) {
                    return true;
                }
            }

            for (int j = 0; j < this.size; j++) {

                if (board[j][i] != side) {
                    isPlacedColumn = 0;
                    continue;
                } else {
                    isPlacedColumn++;
                }

                if (isPlacedColumn == this.winningSize) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean isGameOver() {
        if (hasWin(X_SIDE)) {
            return true;
        }
        if (hasWin(O_SIDE)) {
            return true;
        }
        return isFull();
    }

    public boolean hasWin(byte side) {
        return hasRowColumnWin(side) || hasDiagonalWin(side);
    }

    public boolean hasDiagonalWin(byte side) {
        for (int startRow = 0; startRow < this.size; startRow++) {
            int tempCount = 0;
            int tempCount2 = 0;
            int tempCount3 = 0;

            for (int i = 0; i < this.size - startRow; i++) {
                if (board[startRow + i][i] != side) {
                    tempCount = 0;
                    continue;
                } else {
                    tempCount++;
                }

                if (tempCount == this.winningSize) {
                    return true;
                }
            }

            for (int i = 0; i < this.size - startRow; i++) {
                if (board[startRow + i][this.size - 1 - i] != side) {
                    tempCount2 = 0;
                    continue;
                } else {
                    tempCount2++;
                }

                if (tempCount2 == this.winningSize) {
                    return true;
                }
            }

            for (int i = 0; i < this.size - startRow; i++) {
                if (board[i][startRow + i] != side) {
                    tempCount3 = 0;
                    continue;
                } else {
                    tempCount3++;
                }

                if (tempCount3 == this.winningSize) {
                    return true;
                }
            }
        }

        for (int startCol = this.size - 2; startCol >= 0; startCol--) {
            int tempCount = 0;
            for (int i = 0; i <= startCol; i++) {
                if (board[i][startCol - i] != side) {
                    tempCount = 0;
                    continue;
                } else {
                    tempCount++;
                }

                if (tempCount == this.winningSize) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isFull() {
        return freeSquares == 0;
    }

    public int getKey() {
        int key = 0;

        for (byte[] bytes : board) {
            for (byte aByte : bytes) {
                key = 27644437 * key + aByte;
            }
        }
        return key;
    }

    public String getBoardNotation() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                byte token = board[i][j];
                stringBuilder.append(token);
            }
        }

        // Append the side to move
        if (this.sideToMove == 1) {
            stringBuilder.append("x");
        } else {
            stringBuilder.append("o");
        }

        return stringBuilder.toString();
    }

    public void setBoardNotation(String boardNotation) {

        this.freeSquares = this.size * this.size;

        // Determine the side to move
        if (boardNotation.charAt(boardNotation.length() - 1) == 'x') {
            this.sideToMove = X_SIDE;
        } else {
            this.sideToMove = O_SIDE;
        }

        // Parse the board notation
        char[] input = boardNotation.toCharArray();
        int inputIndex = 0;
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                char token = input[inputIndex];
                switch (token) {
                    case '0':
                        board[i][j] = NO_SIDE;
                        break;
                    case '1':
                        board[i][j] = X_SIDE;
                        this.freeSquares--;
                        break;
                    case '2':
                        board[i][j] = O_SIDE;
                        this.freeSquares--;
                        break;
                }
                inputIndex++;
            }
        }
    }

    public byte getSideToMove() {
        return sideToMove;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                byte token = board[i][j];
                switch (token) {
                    case 0:
                        stringBuilder.append("-");
                        break;
                    case 1:
                        stringBuilder.append("X");
                        break;
                    case 2:
                        stringBuilder.append("O");
                        break;
                }
                stringBuilder.append("\t");
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    public int getSize() {
        return size;
    }

    public byte get(int i, int j) {
        return board[i][j];
    }
}