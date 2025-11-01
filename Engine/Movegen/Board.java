package src.Engine.Movegen;

import static src.Engine.Types.*;

public class Board {
    private final int size;
    public int freeSquares;
    public int nodes = 0;
    private Bitboard[] xBitboards;
    private Bitboard[] oBitboards;
    private Bitboard[] occupied;
    private int offset;
    private int winningSize;
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

        this.occupied = new Bitboard[size];
        for (int i = 0; i < occupied.length; i++) {
            occupied[i] = new Bitboard();
        }
        this.freeSquares = size * size;
        this.size = size;
        this.offset = offset;
        this.winningSize = size - offset;
    }

    public void makeMove(Move move) {
        makeMove(move.x(), move.y());
    }

    public void makeMove(int x, int y) {
        if (sideToMove == X_SIDE) {
            xBitboards[x].set(y);
        } else {
            oBitboards[x].set(y);
        }
        occupied[x].set(y);
        updateTurn();
        freeSquares--;
    }

    public void unmakeMove(Move move) {
        unmakeMove(move.x(), move.y());
    }

    public void unmakeMove(int x, int y) {
        // Revert to the side that actually placed the piece
        updateTurn();
        if (sideToMove == X_SIDE) {
            xBitboards[x].clear(y);
        } else {
            oBitboards[x].clear(y);
        }
        occupied[x].clear(y);
        freeSquares++;
    }

    public void updateTurn() {
        sideToMove ^= 3;
    }

    public boolean hasRowColumnWin(int side) {
        if (side != X_SIDE && side != O_SIDE) {
            throw new IllegalArgumentException("NO_SIDE was passed to hasRowColumnWin!");
        }

        final Bitboard[] bb = (side == X_SIDE) ? xBitboards : oBitboards;

        for (int i = 0; i < size; i++) {
            int runCol = 0; // consecutive in column i
            int runRow = 0; // consecutive in row i

            for (int j = 0; j < size; j++) {
                // column i (vary y=j)
                runCol = bb[i].isSet(j) ? runCol + 1 : 0;
                if (runCol >= winningSize) return true;

                // row i (vary x=j)
                runRow = bb[j].isSet(i) ? runRow + 1 : 0;
                if (runRow >= winningSize) return true;
            }
        }
        return false;
    }

    public void perf(int depth) {
        if (depth == 0) {
            return;
        }

        Move[] legalMoves = generateLegalMoves();
        for (Move move : legalMoves) {
            nodes++;
            makeMove(move);
            perf(depth - 1);
            unmakeMove(move);
        }
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

    private boolean hasRunOnDiag(Bitboard[] bb, int x0, int y0, int dy, int n, int k) {
        int run = 0;
        int x = x0, y = y0;
        while (x >= 0 && x < n && y >= 0 && y < n) {
            run = bb[x].isSet(y) ? run + 1 : 0;
            if (run >= k) return true;
            x += 1;
            y += dy;
        }
        return false;
    }

    public boolean hasDiagonalWin(int side) {
        if (side != X_SIDE && side != O_SIDE) {
            throw new IllegalArgumentException("NO_SIDE was passed to diagonal win!");
        }

        final Bitboard[] bb = (side == X_SIDE) ? xBitboards : oBitboards;
        final int n = this.size;
        final int k = this.winningSize;

        // Direction: \ (down-right)
        // Start along top row (y=0, x in [0..n-k]) and left column (x=0, y in [1..n-k])
        for (int x = 0; x <= n - k; x++) {
            if (hasRunOnDiag(bb, x, 0, 1, n, k)) return true;
        }
        for (int y = 1; y <= n - k; y++) {
            if (hasRunOnDiag(bb, 0, y, 1, n, k)) return true;
        }

        // Direction: / (down-left)
        // Start along bottom row (y=n-1, x in [0..n-k]) and left column (x=0, y in [k-1..n-2])
        for (int x = 0; x <= n - k; x++) {
            if (hasRunOnDiag(bb, x, n - 1, -1, n, k)) return true;
        }
        for (int y = n - 2; y >= k - 1; y--) { // ensure diagonal length >= k
            if (hasRunOnDiag(bb, 0, y, -1, n, k)) return true;
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

    public boolean isEmpty(int x, int y) {
        return !occupied[x].isSet(y);
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

        this.occupied = new Bitboard[size];
        for (int i = 0; i < occupied.length; i++) {
            occupied[i] = new Bitboard();
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

    public Move[] generateLegalMoves() {
        Move[] legalMoves = new Move[freeSquares];

        int counter = 0;
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (!occupied[i].isSet(j)) {
                    legalMoves[counter] = new Move(i, j);
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
        reset();
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
                        occupied[i].set(j);
                        this.freeSquares--;
                        break;
                    case '2':
                        oBitboards[i].set(j);
                        occupied[i].set(j);
                        this.freeSquares--;
                        break;
                }
                inputIndex++;
            }
        }
    }

    private void appendLayer(StringBuilder sb, String title, Bitboard[] layer, int size) {
        sb.append(title).append('\n');
        for (int x = 0; x < size; x++) {
            Bitboard col = layer[x];
            for (int y = 0; y < size; y++) {
                sb.append(col.isSet(y) ? '1' : '0').append(' ');
            }
            sb.append('\n');
        }
        sb.append('\n');
    }

    public String getBitboards() {
        // Optional pre-sizing to reduce reallocations
        int approx = 3 * (size * (2 * size + 2) + 2) + 32;
        StringBuilder sb = new StringBuilder(approx);

        appendLayer(sb, "X Bitboard:", xBitboards, size);
        appendLayer(sb, "O Bitboard:", oBitboards, size);
        appendLayer(sb, "Occupied", occupied, size);

        return sb.toString();
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
