public class Board {
    private final byte[][] board;
    private final int size;
    private final int offset;

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

    public int[] scoreMoves(int[][] legalMoves) {

        int[] scores = new int[legalMoves.length];

        for (int i = 0; i < legalMoves.length; i++) {
            makeMove(legalMoves[i]);

            if (hasRowColumnWin(this.sideToMove) || hasDiagonalWin(this.sideToMove)) {
                scores[i] = 100000;
            }

            if (isFull()) {
                scores[i] = 1;
            }

            unmakeMove(legalMoves[i]);
        }

        return scores;
    }

    public byte get(int i, int j) {
        return board[i][j];
    }

    public void reset() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = 0;
            }
        }

        this.sideToMove = 1;
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

    public int size() {
        return this.size;
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
