package src.Engine.BitboardMovegen;

import static src.Engine.Types.*;

public class Board {
    private Bitboard[] xBitboards;
    private Bitboard[] oBitboards;
    private final int size;
    private int offset;
    private int winningSize;
    public int freeSquares;
    private int sideToMove = 1;

    public Board(int size, int offset) {
        assert size < 64;

        this.xBitboards = new Bitboard[size];
        for (int i = 0; i < xBitboards.length; i++) {
            xBitboards[i] = new Bitboard();
        }
        this.oBitboards = new Bitboard[size];
        for (int i = 0; i < oBitboards.length; i++) {
            oBitboards[i] = new Bitboard();
        }
        this.freeSquares = size * size;
        this.size = size;
        this.offset = offset;
        this.winningSize = size - offset;
    }

    public void makeMove(Move move) {
        if (sideToMove == X_SIDE) {
            xBitboards[move.y()].set(move.x());
        } else {
            oBitboards[move.y()].set(move.x());
        }
        updateTurn();
        freeSquares--;
    }

    public void unmakeMove(Move move) {
        if (sideToMove == X_SIDE) {
            xBitboards[move.y()].clear(move.x());
        } else {
            oBitboards[move.y()].clear(move.x());
        }
        updateTurn();
        freeSquares++;
    }

    public void updateTurn() {
        sideToMove ^= 3;
    }
    public boolean hasRowColumnWin(int side) {
        for (int i = 0; i < this.size; i++) {

            int isPlacedRow = 0;
            int isPlacedColumn = 0;
            if (side == X_SIDE) {
                for (int j = 0; j < this.size; j++) {

                    if (!xBitboards[i].isSet(j)) {
                        isPlacedRow = 0;
                        continue;
                    } else {
                        isPlacedRow++;
                    }

                    if (isPlacedRow == this.winningSize) {
                        return true;
                    }
                }
            } else if (side == O_SIDE) {
                for (int j = 0; j < this.size; j++) {

                    if (!oBitboards[i].isSet(j)) {
                        isPlacedRow = 0;
                        continue;
                    } else {
                        isPlacedRow++;
                    }

                    if (isPlacedRow == this.winningSize) {
                        return true;
                    }
                }
            } else {
                throw new RuntimeException("NO_SIDE was passed to rowColumnWin!");
            }

            if (side == X_SIDE) {
                for (int j = 0; j < this.size; j++) {

                    if (!xBitboards[j].isSet(i)) {
                        isPlacedColumn = 0;
                        continue;
                    } else {
                        isPlacedColumn++;
                    }

                    if (isPlacedColumn == this.winningSize) {
                        return true;
                    }
                }
            } else if (side == O_SIDE) {
                for (int j = 0; j < this.size; j++) {

                    if (!oBitboards[j].isSet(i)) {
                        isPlacedColumn = 0;
                        continue;
                    } else {
                        isPlacedColumn++;
                    }

                    if (isPlacedColumn == this.winningSize) {
                        return true;
                    }
                }
            } else {
                throw new RuntimeException("NO_SIDE was passed to rowColumnWin!");
            }
        }
        return false;
    }

    public void makeNullMove() {
        updateTurn();
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

    public boolean hasWin(int side) {
        return hasRowColumnWin(side) || hasDiagonalWin(side);
    }

    public boolean hasDiagonalWin(int side) {

        for (int startRow = 0; startRow < this.size; startRow++) {
            int tempCount = 0;
            int tempCount2 = 0;
            int tempCount3 = 0;

            if (side == X_SIDE) {
                for (int i = 0; i < this.size - startRow; i++) {
                    if (!xBitboards[startRow + i].isSet(i)) {
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
                    if (!xBitboards[startRow + i].isSet(this.size - 1 - i)) {
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
                    if (!xBitboards[i].isSet(startRow + i)) {
                        tempCount3 = 0;
                        continue;
                    } else {
                        tempCount3++;
                    }

                    if (tempCount3 == this.winningSize) {
                        return true;
                    }
                }
            } else if (side == O_SIDE) {
                for (int i = 0; i < this.size - startRow; i++) {
                    if (!oBitboards[startRow + i].isSet(i)) {
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
                    if (!oBitboards[startRow + i].isSet(this.size - 1 - i)) {
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
                    if (!oBitboards[i].isSet(startRow + i)) {
                        tempCount3 = 0;
                        continue;
                    } else {
                        tempCount3++;
                    }

                    if (tempCount3 == this.winningSize) {
                        return true;
                    }
                }
            } else {
                throw new RuntimeException("NO_SIDE was passed to diagonal win!");
            }
        }

        if (side == X_SIDE) {
            for (int startCol = this.size - 2; startCol >= 0; startCol--) {
                int tempCount = 0;
                for (int i = 0; i <= startCol; i++) {
                    if (!xBitboards[i].isSet(startCol - i)) {
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
        } else if (side == O_SIDE) {
            for (int startCol = this.size - 2; startCol >= 0; startCol--) {
                int tempCount = 0;
                for (int i = 0; i <= startCol; i++) {
                    if (!oBitboards[i].isSet(startCol - i)) {
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
        } else {
            throw new RuntimeException("NO_SIDE was passed to diagonal win at the last loop!");
        }

        return false;
    }
    public int getSize() {
        return size;
    }

    public boolean get(int i, int j, int side) {
        if (side == X_SIDE) {
            return xBitboards[i].isSet(j);
        } else if (side == O_SIDE) {
            return oBitboards[i].isSet(j);
        } else {
            throw new RuntimeException("NO_SIDE was passed to get function!");
        }
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

    public void reset() {
        this.xBitboards = new Bitboard[size];
        for (int i = 0; i < xBitboards.length; i++) {
            xBitboards[i] = new Bitboard();
        }
        this.oBitboards = new Bitboard[size];
        for (int i = 0; i < oBitboards.length; i++) {
            oBitboards[i] = new Bitboard();
        }

        this.sideToMove = X_SIDE;
    }

    public boolean isFull() {
        return freeSquares == 0;
    }

    public int getKey() {
        long hash = 0;

        for (int i = 0; i < size; i++) {
            hash = 27644437L * hash + xBitboards[i].getBits();
            hash = 27644437L * hash + oBitboards[i].getBits();
        }

        return Long.hashCode(hash);
    }

    // TODO instead of query both bitboards, make an occupied bitboard that keeps track of that
    // TODO so the we can save one isSet method call
    public Move[] generateLegalMoves() {
        Move[] legalMoves = new Move[freeSquares];

        for (int i = 0; i < legalMoves.length; i++) {
            legalMoves[i] = new Move();
        }

        int counter = 0;
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (!xBitboards[i].isSet(j) && !oBitboards[i].isSet(j)) {
                    legalMoves[counter].setX(i);
                    legalMoves[counter].setY(j);
                    System.out.println(legalMoves[counter]);
                    counter++;
                }
            }
        }

        return legalMoves;
    }

    public String getBoardNotation() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (xBitboards[i].isSet(j)) {
                    stringBuilder.append(X_SIDE);
                } else if (oBitboards[i].isSet(j)) {
                    stringBuilder.append(O_SIDE);
                } else {
                    stringBuilder.append(NO_SIDE);
                }
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
                    case '1':
                        xBitboards[i].set(j);
                        this.freeSquares--;
                        break;
                    case '2':
                        oBitboards[j].set(i);
                        this.freeSquares--;
                        break;
                }
                inputIndex++;
            }
        }
    }

    public String getBitboards() {
        StringBuilder boards = new StringBuilder();
        for (Bitboard xBitboard : xBitboards) {
            for (int j = 0; j < size; j++) {
                if (xBitboard.isSet(j)) {
                    boards.append(1);
                } else {
                    boards.append(0);
                }
                boards.append(" ");
            }
            boards.append("\n");
        }

        boards.append("\n\n");

        for (Bitboard oBitboard : oBitboards) {
            for (int j = 0; j < size; j++) {
                if (oBitboard.isSet(j)) {
                    boards.append(1);
                } else {
                    boards.append(0);
                }
                boards.append(" ");
            }
            boards.append("\n");
        }
        return boards.toString();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (xBitboards[i].isSet(j)) {
                    stringBuilder.append("X");
                } else if (oBitboards[i].isSet(j)) {
                    stringBuilder.append("O");
                } else {
                    stringBuilder.append("-");
                }
                stringBuilder.append("\t");
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    public int getSideToMove() {
        return sideToMove;
    }
}
