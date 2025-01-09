package src.Engine;

public class FasterBoard {
    private final byte[] rows;
    private final int winningLength;
    private final int size;
    private byte sideToMove;

    public FasterBoard(int size, int offset) {
        this.rows = new byte[size];
        this.winningLength = size - offset;
        this.size = size;
        this.sideToMove = 1;
    }

    public static void main(String[] args) {
        FasterBoard fasterBoard = new FasterBoard(3, 0);
        fasterBoard.setBoardNotation("001001001x");
        System.out.println(fasterBoard);
        System.out.println(fasterBoard.hasRowWin((byte) 1));
        System.out.println(fasterBoard.hasRowWin((byte) 2));
    }

    public boolean hasRowWin(byte sideToMove) {
        for (int i = 0; i < this.size; i++) {
                return true;

        }

        return false;
    }

    public String getBoardNotation() {
        StringBuilder notation = new StringBuilder();
        return notation.toString();
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
        boolean isRowAdd = true;
        for (int i = 0; i < this.size * this.size; i++) {
            char token = input[i];
            switch (token) {
                case '0':

                    break;
                case '1':

                    break;
                case '2':
                    break;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        return stringBuilder.toString();
    }
}
