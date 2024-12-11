import java.util.Arrays;

public class Search {

    int nodes = 0;
    int[] bestMove = new int[2];

    public int evaluate(Board board, int ply) {
        final byte xSide = 1;
        final byte oSide = 2;

        short xEval = 0;
        short oEval = 0;

        if (board.size() % 2 == 1) {
            int center = board.size() / 2;

            if (board.get(center, center) == xSide) {
                xEval += 5;
            }

            if (board.get(center, center) == oSide) {
                oEval += 5;
            }
        }

        if (board.hasDiagonalWin(xSide) || board.hasRowColumnWin(xSide)) {
            xEval += 10000;
        }

        if (board.hasDiagonalWin(oSide) || board.hasRowColumnWin(oSide)) {
            oEval += 10000;
        }

        int diff = xEval - oEval;
        return (board.getSideToMove() == 2 ? -diff : diff);
    }

    public int negamax(Board board, int depth, int ply, int alpha, int beta) {

        nodes++;
        if (depth == 0 || board.isGameOver()) {
            return evaluate(board, ply);
        }

        int bestScore = -30000;
        int[][] legalMoves = board.generateLegalMoves();

        for (int[] move : legalMoves) {
            board.makeMove(move);

            int score = -negamax(board, depth - 1, ply + 1, -beta, -alpha);

            board.unmakeMove(move);

            if (score > bestScore) {
                bestScore = score;

                if (score > alpha) {
                    alpha = score;
                }

                if (ply == 0) {
                    bestMove = move;
                }

                if (score >= beta) {
                    return bestScore;
                }
            }
        }
        return bestScore;
    }

    public int[] getBestMove(Board board, int depth) {
        negamax(board, depth, 0, -30000, 30000);
        System.out.println("Bestmove: " + Arrays.toString(bestMove));
        return bestMove;
    }
}