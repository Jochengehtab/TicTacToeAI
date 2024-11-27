

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

    public void makeMove(int x, int y) {
        board[x][y] = this.sideToMove;
        updateTurn();
    }

    public void unmakeMove(int x, int y) {
        board[x][y] = 0;
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
            makeMove(legalMoves[i][0], legalMoves[i][1]);

            if (hasColumnWin(this.sideToMove) || hasDiagonalWin(this.sideToMove) || hasRowWin(this.sideToMove)) {
                scores[i] = 100000;
            }

            if (isDraw()) {
                scores[i] = 1;
            }

            unmakeMove(legalMoves[i][0], legalMoves[i][1]);
        }

        return scores;
    }

    public byte get(int i, int j) {
        return board[i][j];
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

    public boolean hasRowWin(byte side) {
        boolean isWon = false;
        for (int i = 0; i < this.size; i++) {

            int isPlaced = 0;

            for (int j = 0; j < this.size; j++) {
                if (board[i][j] == side) {
                    isPlaced++;
                }
            }

            if (isPlaced == this.size) {
                isWon = true;
                break;
            }
        }
        return isWon;
    }

    public boolean hasColumnWin(byte side) {
        boolean isWon = false;
        for (int i = 0; i < this.size; i++) {

            int isPlaced = 0;

            for (int j = 0; j < this.size; j++) {
                if (board[j][i] == side) {
                    isPlaced++;
                }
            }

            if (isPlaced == this.size) {
                isWon = true;
                break;
            }
        }
        return isWon;
    }

    public boolean hasDiagonalWin(byte side) {
        boolean isWon = false;

        int sum = 0;
        for (int i = 0; i < this.size; i++) {
            if (board[i][i] == side) {
                sum++;
            }
        }

        if (sum == this.size - this.offset) {
            return true;
        }

        sum = 0;

        for (int i = this.size - 1; i > 0; i--) {
            if (board[i][i] == side) {
                sum++;
            }
        }

        if (sum == this.size - this.offset) {
            return true;
        }

        return isWon;
    }

    public boolean isDraw() {
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

        if (this.sideToMove == 1) {
            stringBuilder.append("x");
        } else {
            stringBuilder.append("o");
        }

        return stringBuilder.toString();
    }

    public void setBoardNotation(String boardNotation) {

        if (boardNotation.charAt(boardNotation.length() - 1) == 'x') {
            this.sideToMove = 1;
        } else {
            this.sideToMove = 2;
        }

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
