/*
    TicTacToeAI
    Copyright (C) 2024 Jochengehtab

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/


package src.Engine;

public class Search {

    private final Stack[] stack = new Stack[256];
    private final Evaluation evaluate = new Evaluation();
    private final MoveOrder moveOrder = new MoveOrder();
    private int nodes = 0;
    private int[] bestMove = new int[2];
    private boolean isNormalSearch = true, shouldStop = false;
    private long startTime, thinkTime;

    public int negamax(Board board, int depth, int ply, int alpha, int beta) {

        // If we are told to stop, we return beta to exit the loop as fast as possible
        if (shouldStop) {
            return beta;
        }

        if (!isNormalSearch) {
            // Every 4096 nodes we check for a time-out
            if (nodes % 4096 == 0) {

                // Check for a time-out
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > thinkTime) {
                    shouldStop = true;
                }
            }
        }

        // We increase the node count
        nodes++;

        // Either the depth is zero or the game is over (terminal node) we return a static evaluation of the position
        if (depth == 0 || board.isGameOver()) {
            return evaluate.evaluate(board, ply);
        }

        // TODO evaluation is bugged with mate score fixing this will bring hundreds of elo
        int staticEval = evaluate.evaluate(board, ply);

        boolean pvNode = beta > alpha + 1;

        /*
        LLR        : 2.98
        ELO        : 95.7 +- 21.85
        Games      : [153, 203, 340]
         */

        //Reverse futility pruning
        if (depth <= 4 && staticEval - 72 * depth >= beta) {
            return (staticEval + beta) / 2;
        }

        int bestScore = -30000;
        int[][] legalMoves = board.generateLegalMoves();
        int[] scores = moveOrder.scoreMoves(legalMoves, stack[ply].killer);

        for (int i = 0; i < legalMoves.length; i++) {

            int[] move = moveOrder.getSortedMove(legalMoves, scores, i);

            // We make our move
            board.makeMove(move);

            int score;
            // PVS
            // We assume our first move in the movelist is the best move in the position
            // So we search this move with a full window (-beta, -alpha)
            if (i == 0) {
                score = -negamax(board, depth - 1, ply + 1, -beta, -alpha);
            } else {
                // Since we think that we already searched our best move in the position, we search the other moves
                // with a very small window (-alpha - 1, -alpha)
                score = -negamax(board, depth - 1, ply + 1, -alpha - 1, -alpha);

                // If our search reruns a score, that was not our assumption i.e., our first move wasn't the best
                // we need to do an expensive re-search with a full window (-beta, -alpha)
                if (score > alpha && score < beta) {
                    score = -negamax(board, depth - 1, ply + 1, -beta, -alpha);
                }
            }

            // We unmake the move
            board.unmakeMove(move);

            // If our score is greater than the bestscore, we update the bestscore
            if (score > bestScore) {
                bestScore = score;

                // If our score exceeds alpha, we update alpha
                if (score > alpha) {
                    alpha = score;
                }

                // If we are at the root position, we update our best move
                if (ply == 0) {
                    bestMove = move;
                }

                // The score was too good for the opponent, so we cut that out
                if (score >= beta) {
                    // Update the killer move
                    stack[ply].killer = move;
                    return bestScore;
                }
            }
        }

        return bestScore;
    }

    /*
    LLR        : 2.95
    ELO        : 28.87 +- 12.95
    Games      : [1133, 146, 1351]
     */

    int aspiration(int depth, int score, Board board) {
        int delta = 27;
        int alpha = Math.max(-30000, score - delta);
        int beta = Math.min(30000, score + delta);
        double finalASPMultiplier = 121 / 100.0;

        while (true) {
            score = negamax(board, depth, 0, alpha, beta);
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > thinkTime) {
                return score;
            }

            if (score >= beta) {
                beta = Math.min(beta + delta, 30000);
            } else if (score <= alpha) {
                beta = (alpha + beta) / 2;
                alpha = Math.max(alpha - delta, -30000);
            } else {
                break;
            }

            delta *= finalASPMultiplier;
        }

        return score;
    }

    public int[] getBestMove(Board board, long thinkTime) {
        initStack();
        int[] tempBestMove = new int[2];

        if (thinkTime < 0) {
            negamax(board, 1, 0, -30000, 30000);
            return this.bestMove;
        }
        this.thinkTime = thinkTime;
        isNormalSearch = false;
        shouldStop = false;
        nodes = 0;
        startTime = System.currentTimeMillis();
        int score = 0;
        int depth = 0;

        int previousScore = 0;

        for (int i = 1; i < 256; i++) {
            depth = i;
            score = i >= 6 ? aspiration(i, previousScore, board) : negamax(board, i, 0, -30000, 30000);
            previousScore = score;
            if (shouldStop) {
                break;
            }
            tempBestMove = this.bestMove;
        }

        System.out.println("Score: " + score);
        System.out.println("Depth: " + depth);
        isNormalSearch = true;
        shouldStop = false;
        return tempBestMove;
    }

    public int[] getBestMove(Board board, int depth) {
        initStack();
        nodes = 0;
        negamax(board, depth, 0, -30000, 30000);
        return bestMove;
    }

    public void initStack() {
        for (int i = 0; i < stack.length; i++) {
            stack[i] = new Stack();
        }
    }

    public int bench() {
        initStack();
        startTime = System.currentTimeMillis();
        shouldStop = false;
        short benchDepth = 6;

        Board board = new Board(10, 5);
        int nodeCount = 0;

        board.setBoardNotation("0000000000000000200000001000000200000010000100020000000000000001000100000000000000002" +
                "000000000000000o");
        getBestMove(board, benchDepth);
        nodeCount += this.nodes;

        board.setBoardNotation("0000000000002000010000001102210000020100001000201000202000000012011020000021201000200" +
                "000000000000000o");
        getBestMove(board, benchDepth);
        nodeCount += this.nodes;

        board.setBoardNotation("0000000000010010200001201000100200202000000010002000001200000020000010002000000000100" +
                "000000000000000x");
        getBestMove(board, benchDepth);
        nodeCount += this.nodes;

        long elapsedTime = System.currentTimeMillis() - startTime;
        double elapsedTimeSeconds = elapsedTime / 1000.0;
        int NPS = (int) Math.round(nodeCount / elapsedTimeSeconds);

        System.out.println("Time  : " + elapsedTime + " ms");
        System.out.println("Nodes : " + nodeCount);
        System.out.println("NPS   : " + NPS);
        return NPS;
    }

    public void speedtest() {
        int amount = 20;
        int nps = 0;
        for (int i = 0; i < amount; i++) {
            nps += bench();
        }
        System.out.println("Average speed of " + amount + " Benchmarks is: " + Math.round((float) nps / amount) +
                " NPS");
    }
}