import java.util.Arrays;

public class Search {

    int nodes = 0;
    int searchDepth = 1;

    public int evaluate(Board board) {
        byte sideToMove = board.getSideToMove();
        byte opponent = (byte) (sideToMove == 1 ? 2 : 1);

        if (board.size() % 2 == 1) {
            int center = board.size() / 2;

            if (board.get(center, center) == sideToMove) {
                return 5;
            } else if (board.get(center, center) == opponent) {
                return -5;
            }
        }

        if (board.hasDiagonalWin(sideToMove) || board.hasRowWin(sideToMove) || board.hasColumnWin(sideToMove)) {
            return 10;
        }

        if (board.hasDiagonalWin(opponent) || board.hasRowWin(opponent) || board.hasColumnWin(opponent)) {
            return -10;
        }

        return 0;
    }

    public int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizingPlayer) {

        nodes++;
        if (depth == 0 || board.isGameOver()) {
            return evaluate(board);
        }

        int bestScore = isMaximizingPlayer ? -30000 : 30000;
        int[][] legalMoves = board.generateLegalMoves();

        for (int[] move : legalMoves) {
            board.makeMove(move[0], move[1]);

            int score = minimax(board, depth - 1, alpha, beta, !isMaximizingPlayer);

            board.unmakeMove(move[0], move[1]);

            if (isMaximizingPlayer) {
                bestScore = Math.max(bestScore, score);
                alpha = Math.max(alpha, bestScore);
            } else {
                bestScore = Math.min(bestScore, score);
                beta = Math.min(beta, bestScore);
            }

            if (alpha >= beta) {
                break;
            }
        }

        return bestScore;
    }

    public int[] getBestMove(Board board) {
        int bestScore = -30000;
        int[] bestMove = new int[2];
        nodes = 0;
        searchDepth = 1;

        long startTime = System.currentTimeMillis();

        while (!((System.currentTimeMillis() - startTime) >= 1000)) {
            int[][] legalMoves = board.generateLegalMoves();
            int[] scores = board.scoreMoves(legalMoves); // Add a method to score the moves

            // Sort moves by score (highest to lowest)
            for (int i = 0; i < legalMoves.length - 1; i++) {
                for (int j = i + 1; j < legalMoves.length; j++) {
                    if (scores[j] > scores[i]) {
                        int[] tempMove = legalMoves[j];
                        legalMoves[j] = legalMoves[i];
                        legalMoves[i] = tempMove;

                        int tempScore = scores[j];
                        scores[j] = scores[i];
                        scores[i] = tempScore;
                    }
                }
            }

            // Run the Minimax algorithm with sorted moves
            for (int[] move : legalMoves) {
                board.makeMove(move[0], move[1]);
                int score = minimax(board, searchDepth, -30000, 30000, false);
                System.out.println(Arrays.toString(move) + " | " + score);
                board.unmakeMove(move[0], move[1]);

                if (score > bestScore) {
                    bestMove[0] = move[0];
                    bestMove[1] = move[1];
                    bestScore = score;
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