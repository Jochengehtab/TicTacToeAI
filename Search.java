import java.util.Arrays;

public class Search {

    int nodes = 0;
    int searchDepth = 1;

    public int evaluate(Board board) {
        byte sideToMove = board.getSideToMove();
        byte opponent = (byte) (sideToMove == 1 ? 2 : 1);

        if (board.hasDiagonalWin(sideToMove) || board.hasRowWin(sideToMove) || board.hasColumnWin(sideToMove)) {
            return 10;
        }

        if (board.hasDiagonalWin(opponent) || board.hasRowWin(opponent) || board.hasColumnWin(opponent)) {
            return -10;
        }


        return 0;
    }

    public int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer) {


        int staticEval = evaluate(board);

        // TODO make a proper exit
        if (depth == searchDepth) {
            if (staticEval == 10) {
                return staticEval - depth;
            }

            if (staticEval == -10) {
                return staticEval + depth;
            }
            return 0;
        }

        nodes++;

        if (staticEval == 10) {
            return staticEval - depth;
        }

        if (staticEval == -10) {
            return staticEval + depth;
        }

        if (board.isDraw()) {
            return staticEval;
        }

        if (isMaximizingPlayer) {
            int bestScore = -30000;

            int[][] legalMoves = board.generateLegalMoves();
            for (int[] move : legalMoves) {
                board.makeMove(move[0], move[1]);

                bestScore = Math.max(bestScore, minimax(board, depth + 1, alpha, beta, false));

                board.unmakeMove(move[0], move[1]);

                alpha = Math.max(alpha, bestScore);
                if (bestScore >= beta) {
                    break;
                }
            }
            return bestScore;
        } else {
            int bestScore = 30000;
            int[][] legalMoves = board.generateLegalMoves();
            for (int[] move : legalMoves) {
                board.makeMove(move[0], move[1]);

                bestScore = Math.min(bestScore, minimax(board, depth + 1, alpha, beta, true));

                board.unmakeMove(move[0], move[1]);

                beta = Math.min(beta, bestScore);
                if (bestScore <= alpha) {
                    break;
                }
            }
            return bestScore;
        }
    }

    public int[] getBestMove(Board board) {
        int bestScore = -30000;
        int[] bestMove = new int[2];
        nodes = 0;
        searchDepth = 1;

        long startTime = System.currentTimeMillis();

        while (!((System.currentTimeMillis() - startTime) >= 1000)) {
            int[][] legalMoves = board.generateLegalMoves();

            for (int[] move : legalMoves) {
                board.makeMove(move[0], move[1]);
                int score = minimax(board, 0,-30000, 30000, false);
                board.unmakeMove(move[0], move[1]);

                if (score > bestScore) {
                    bestMove[0] = move[0];
                    bestMove[1] = move[1];
                    bestScore = score;
                    System.out.println(Arrays.toString(move));
                }
            }
            searchDepth++;
            if (searchDepth == 256) {
                break;
            }
        }

        System.out.println("Depth: " + searchDepth);
        System.out.println("Nodes: " + nodes);

        return bestMove;
    }
}