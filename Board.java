/*
    TicTacToeAI
    Copyright (C) 2024 Jochengehtab

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

import java.util.Arrays;

public class Board {
    private final byte[][] board;
    private final int size;
    private int offset;

    private byte sideToMove = 1;

    public Board(int size, int offset) {
        board = new byte[size][size];
        this.size = size;
        this.offset = offset;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = 0;
            }
        }
    }

    public void makeMove(int[] move) {
        board[move[0]][move[1]] = this.sideToMove;
        updateTurn();
    }

    public void makeMove(int x, int y) {
        board[x][y] = this.sideToMove;
        updateTurn();
    }

    public void unmakeMove(int[] move) {
        board[move[0]][move[1]] = 0;
        updateTurn();
    }

    public void unmakeMove(int x, int y) {
        board[x][y] = 0;
        updateTurn();
    }

    public void makeNullMove() {
        updateTurn();
    }

    public void unmakeNullMove() {
        updateTurn();
    }

    public boolean hasWinWithFurtherOffset(int offset, byte side) {

        int tempOffset = this.offset;
        this.offset += offset;

        if (hasDiagonalWin(side) || hasRowColumnWin(side)) {
            this.offset = tempOffset;
            return true;
        }

        this.offset = tempOffset;
        return false;
    }

    public int[][] generateNoiseMoves(byte sideToMove) {

        int arraySize = 0;
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                byte token = board[i][j];
                if (token == 0) {
                    makeMove(i, j);
                    if (hasRowColumnWin(sideToMove) || hasDiagonalWin(sideToMove)) {

                        arraySize++;
                    }
                    unmakeMove(i, j);
                }
            }
        }

        int[][] legalMoves = new int[arraySize][2];

        int counter = 0;
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                byte token = board[i][j];
                if (token == 0) {
                    makeMove(i, j);
                    if (hasRowColumnWin(sideToMove) || hasDiagonalWin(sideToMove)) {

                        legalMoves[counter][0] = i;
                        legalMoves[counter][1] = j;
                        counter++;
                    }
                    unmakeMove(i, j);
                }
            }
        }
        return legalMoves;
    }

    public int[][] generateLegalMoves() {
        int arraySize = 0;
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                byte token = board[i][j];
                if (token == 0) {
                    arraySize++;
                }
            }
        }

        int[][] legalMoves = new int[arraySize][2];

        int counter = 0;
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                byte token = board[i][j];
                if (token == 0) {
                    legalMoves[counter][0] = i;
                    legalMoves[counter][1] = j;
                    counter++;
                }
            }
        }

        return legalMoves;
    }

    public int[] scoreMoves(int[][] legalMoves, int[] killer) {

        int[] scores = new int[legalMoves.length];

        for (int i = 0; i < legalMoves.length; i++) {
            if (Arrays.equals(legalMoves[i], killer)) {
                scores[i] = 500000;
            } else {
                scores[i] = 0;
            }
        }

        return scores;
    }

    public int[] getSortedMove(int[][] legalMoves, int[] scores, int i) {
        for (int j = i + 1; j < legalMoves.length; j++) {
            if (scores[j] > scores[i]) {
                int[] temp = legalMoves[j];
                legalMoves[j] = legalMoves[i];
                legalMoves[i] = temp;

                int temp2 = scores[j];
                scores[j] = scores[i];
                scores[i] = temp2;
            }
        }

        return legalMoves[i];
    }

    public void reset() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = 0;
            }
        }

        this.sideToMove = 1;
    }

    public void updateTurn() {
        this.sideToMove = (byte) (this.sideToMove == 1 ? 2 : 1);
    }

    public boolean hasRowColumnWin(byte side) {
        for (int i = 0; i < this.size; i++) {

            int isPlacedRow = 0;
            int isPlacedColumn = 0;
            for (int j = 0; j < this.size; j++) {
                if (board[i][j] == side) {
                    isPlacedRow++;
                } else {
                    isPlacedRow = 0;
                }

                if (board[j][i] == side) {
                    isPlacedColumn++;
                } else {
                    isPlacedColumn = 0;
                }

                if (isPlacedRow == this.size - this.offset || isPlacedColumn == this.size - this.offset) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean isGameOver() {
        if (hasRowColumnWin((byte) 1) || hasDiagonalWin((byte) 1)) {
            return true;
        }
        if (hasRowColumnWin((byte) 2) || hasDiagonalWin((byte) 2)) {
            return true;
        }
        return isFull();
    }

    public boolean hasDiagonalWin(byte side) {
        for (int startRow = 0; startRow < this.size; startRow++) {
            int tempCount = 0;
            for (int i = 0; i < this.size - startRow; i++) {
                if (board[startRow + i][i] == side) {
                    tempCount++;
                } else {
                    tempCount = 0;
                }

                if (tempCount == this.size - this.offset) {
                    return true;
                }
            }
        }

        for (int startCol = 1; startCol < this.size; startCol++) {
            int tempCount = 0;
            for (int i = 0; i < this.size - startCol; i++) {
                if (board[i][startCol + i] == side) {
                    tempCount++;
                } else {
                    tempCount = 0;
                }

                if (tempCount == this.size - this.offset) {
                    return true;
                }
            }
        }

        for (int startRow = 0; startRow < this.size; startRow++) {
            int tempCount = 0;
            for (int i = 0; i < this.size - startRow; i++) {
                if (board[startRow + i][this.size - 1 - i] == side) {
                    tempCount++;
                } else {
                    tempCount = 0;
                }

                if (tempCount == this.size - this.offset) {
                    return true;
                }
            }
        }

        for (int startCol = this.size - 2; startCol >= 0; startCol--) {
            int tempCount = 0;
            for (int i = 0; i <= startCol; i++) {
                if (board[i][startCol - i] == side) {
                    tempCount++;
                } else {
                    tempCount = 0;
                }

                if (tempCount == this.size - this.offset) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isFull() {
        boolean isDraw = true;

        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (board[i][j] == 0) {
                    isDraw = false;
                    break;
                }
            }
        }

        return isDraw;
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
        // Determine the side to move
        if (boardNotation.charAt(boardNotation.length() - 1) == 'x') {
            this.sideToMove = 1;
        } else {
            this.sideToMove = 2;
        }

        // Parse the board notation
        char[] input = boardNotation.toCharArray();
        int inputIndex = 0;
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                char token = input[inputIndex];
                switch (token) {
                    case '0':
                        board[i][j] = 0;
                        break;
                    case '1':
                        board[i][j] = 1;
                        break;
                    case '2':
                        board[i][j] = 2;
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
}
