

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

    public boolean hasRowColumnWing(byte side) {
        boolean isWon = false;
        for (int i = 0; i < this.size; i++) {

            int isPlacedRow = 0;
            int isPlacedColumn = 0;

            for (int j = 0; j < this.size; j++) {
                if (board[i][j] == side) {
                    isPlacedRow++;
                }

                if (board[j][i] == side) {
                    isPlacedColumn++;
                }
            }

            if (isPlacedRow == this.size || isPlacedColumn == this.size) {
                isWon = true;
                break;
            }
        }
        return isWon;
    }

    public void testBoard() {

        if (this.size == 3) {


            System.out.println("Testing for board size: " + this.size);

            // Test Column win
            setBoardNotation("100100100o");
            testSuit(hasColumnWin((byte) 1), "Column win for X");

            setBoardNotation("200200200o");
            testSuit(hasColumnWin((byte) 2), "Column win for O");

            // Test Row win
            setBoardNotation("111000000o");
            testSuit(hasRowWin((byte) 1), "Row win for X");

            setBoardNotation("222000000o");
            testSuit(hasRowWin((byte) 2), "Row win for O");

            // Test Diagonal win
            setBoardNotation("221212122x");
            testSuit(hasDiagonalWin((byte) 1), "Diagonal win for X");

            setBoardNotation("112121210x");
            testSuit(hasDiagonalWin((byte) 2), "Diagonal win for O");

            setBoardNotation("100010001o");
            testSuit(hasDiagonalWin((byte) 1), "Diagonal win for X");

            setBoardNotation("200020002x");
            testSuit(hasDiagonalWin((byte) 2), "Diagonal win for O");

            // Check if we got non-false positives
            setBoardNotation("122212222x");
            testSuit(!hasDiagonalWin((byte) 1), "Invalid diagonal win for X");

            setBoardNotation("211121111o");
            testSuit(!hasDiagonalWin((byte) 2), "Invalid diagonal win for O");
        }

        if (this.size == 5) {
            System.out.println("Testing for board size: " + this.size);
            setBoardNotation("1222201100001000001000001o");
            testSuit(hasDiagonalWin((byte) 1), "Diagonal win for X");
        }
    }

    public boolean isGameOver() {
        if (hasRowWin((byte) 1) || hasColumnWin((byte) 1) || hasDiagonalWin((byte) 1)) {
            return true;
        }
        if (hasRowWin((byte) 2) || hasColumnWin((byte) 2) || hasDiagonalWin((byte) 2)) {
            return true;
        }
        return isDraw();
    }


    private void testSuit(boolean result, String message) {
        if (!result) {
            throw new RuntimeException("Error while testing: " + message + "!");
        }
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

        int sum = 0;

        // Top left to bottom right
        for (int i = 0; i < this.size; i++) {
            if (board[i][i] == side) {
                sum++;
            }
        }

        // Check if we got a win
        if (sum == this.size - this.offset) {
            return true;
        }

        // Reset sum
        sum = 0;

        // This value gets incremented while i gets decremented
        int indexHelper = 0;

        // Top right to bottom left
        for (int i = this.size - 1; i > -1; i--) {
            if (board[indexHelper][i] == side) {
                sum++;
            }
            indexHelper++;
        }

        // Check if we got a win
        return sum == this.size - this.offset;
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
