public class Board {
    private final byte[][] board;
    private final int size;
    private final int offset;

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

    public void makeMove(int x, int y, byte side) {
        board[x][y] = side;
    }

    public void unmakeMove(int x, int y) {
        board[x][y] = 0;
    }

    public boolean hasRowWing(byte side) {
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

    public boolean hasColumWin(byte side) {
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
        int isPlaced = 0;
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (board[i][j] == side) {
                    isPlaced++;
                }

                if (isPlaced == this.size - this.offset) {
                    isWon = true;
                    break;
                }
            }
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
        return stringBuilder.toString();
    }

    public void setBoardNotation(String boardNotation) {
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
