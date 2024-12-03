import java.util.Arrays;

public class Search {

    int nodes = 0;
    int searchDepth = 1;

    public int evaluate(Board board) {
        byte sideToMove = board.getSideToMove();
        byte opponent = (byte) (sideToMove == 1 ? 2 : 1);

        if (board.isGameOver()) {
            return -100;
        }

        int score = 0;

        if (board.size() % 2 == 1) {
            int center = board.size() / 2;

            if (board.get(center, center) == sideToMove) {
                score+= 5;
            } else if (board.get(center, center) == opponent) {
                score -=5;
            }
        }

        if (board.hasDiagonalWin(sideToMove) || board.hasRowWin(sideToMove) || board.hasColumnWin(sideToMove)) {
            score += 10;
        }

        if (board.hasDiagonalWin(opponent) || board.hasRowWin(opponent) || board.hasColumnWin(opponent)) {
            score -=10;
        }

        return score;
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

                // TODO switched from alpha
                alpha = Math.max(beta, bestScore);
            } else {
                bestScore = Math.min(bestScore, score);

                // TODO switched from beta
                beta = Math.min(alpha, bestScore);
            }

            if (alpha >= beta) {
                break;
            }
        }

        return bestScore;
    }

    public int[] getBestMove(Board board, int depth) {
        int bestScore = -30000;
        int[] bestMove = new int[2];
        nodes = 0;

        int[][] legalMoves = board.generateLegalMoves();
        int[] scores = board.scoreMoves(legalMoves);

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

        for (int[] move : legalMoves) {
            board.makeMove(move[0], move[1]);
            int score = minimax(board, depth, -30000, 30000, false);

            System.out.println("Score: " + bestScore + " | Move: " + Arrays.toString(move));
            board.unmakeMove(move[0], move[1]);

            if (score > bestScore) {
                bestMove[0] = move[0];
                bestMove[1] = move[1];
                bestScore = score;
            }
        }

        System.out.println("Depth: " + depth);
        System.out.println("Nodes: " + nodes);

        return bestMove;
    }

    public int[] getBestMove(Board board, long thinkTime) {
        int bestScore = -30000;
        int[] bestMove = new int[2];
        nodes = 0;
        searchDepth = 1;

        long startTime = System.currentTimeMillis();

        while (!((System.currentTimeMillis() - startTime) >= thinkTime)) {
            int[][] legalMoves = board.generateLegalMoves();
            int[] scores = board.scoreMoves(legalMoves);

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