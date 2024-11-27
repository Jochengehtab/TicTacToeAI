public class Search {

    int nodes = 0;

    private long startTime = System.currentTimeMillis();

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

        if ((System.currentTimeMillis() - startTime) >= 1000) {
            return beta;
        }

        int staticEval = evaluate(board);
        nodes++;
        if (staticEval == 10) {
            return staticEval - depth;
        }

        if (staticEval == -10) {
            return staticEval + depth;
        }

        if (board.isDraw()) {
            return 0;
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
        int bestVal = -30000;
        int[] bestMove = new int[2];
        nodes = 0;

        int[][] legalMoves = board.generateLegalMoves();
        startTime = System.currentTimeMillis();
        System.out.println(startTime);
        for (int[] move : legalMoves) {
            board.makeMove(move[0], move[1]);

            int moveVal = minimax(board, 0,-30000, 30000, false);

            board.unmakeMove(move[0], move[1]);

            if (moveVal > bestVal) {
                bestMove[0] = move[0];
                bestMove[1] = move[1];
                bestVal = moveVal;
            }
        }
        System.out.println("Nodes: " + nodes);

        return bestMove;
    }
}