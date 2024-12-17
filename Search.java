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


public class Search {

    private final Stack[] stack = new Stack[256];
    int nodes = 0;
    int[] bestMove = new int[2];
    private boolean isNormalSearch = true;
    private long startTime;
    private long thinkTime;
    private boolean shouldStop = false;

    public int evaluate(Board board, int ply) {
        final byte xSide = 1;
        final byte oSide = 2;

        short xEval = 0;
        short oEval = 0;

        if (board.hasWinWithFurtherOffset(1, xSide)) {
            xEval += 1000;
        }

        if (board.hasWinWithFurtherOffset(1, oSide)) {
            oEval += 1000;
        }

        if (board.hasDiagonalWin(xSide) || board.hasRowColumnWin(xSide)) {
            xEval += (short) (10000 - ply);
        }

        if (board.hasDiagonalWin(oSide) || board.hasRowColumnWin(oSide)) {
            oEval += (short) (10000 - ply);
        }

        int diff = xEval - oEval;
        return (board.getSideToMove() == 2 ? -diff : diff);
    }

    public int negamax(Board board, int depth, int ply, int alpha, int beta) {

        if (shouldStop) {
            return beta;
        }

        if (!isNormalSearch) {
            if (nodes % 4096 == 0) {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > thinkTime) {
                    shouldStop = true;
                }
            }
        }

        nodes++;
        if (depth == 0 || board.isGameOver()) {
            return evaluate(board, ply);
        }

        boolean pvNode = beta > alpha + 1;

        int staticEval = evaluate(board, ply);

        //Reverse futility pruning
        if (depth <= 4 && staticEval - 72 * depth >= beta) {
            return (staticEval + beta) / 2;
        }

        /*
        if (!pvNode && depth >= 3 && staticEval >= beta)
        {
            board.makeNullMove();
            int depthReduction = 3 + depth / 3;
            int score = -negamax(board, depth - depthReduction,-beta, -alpha, ply + 1);
            board.unmakeNullMove();
            if (score >= beta)
            {
                return score;
            }
        }

         */

        int bestScore = -30000;
        int[][] legalMoves = board.generateLegalMoves();
        int[] scores = board.scoreMoves(legalMoves, stack[ply].killer);

        for (int i = 0; i < legalMoves.length; i++) {

            int[] move = board.getSortedMove(legalMoves, scores, i);

            board.makeMove(move);

            int score;
            if (i == 0) {
                score = -negamax(board, depth - 1, ply + 1, -beta, -alpha);
            } else {
                score = -negamax(board, depth - 1, ply + 1, -alpha - 1, -alpha);
                if (score > alpha && beta - alpha > 1) {
                    score = -negamax(board, depth - 1, ply + 1, -beta, -alpha);
                }
            }
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
                    stack[ply].killer = move;
                    return bestScore;
                }
            }
        }
        return bestScore;
    }

    private int qs(Board board, int alpha, int beta, int ply) {
        int standPat = evaluate(board, ply);

        if (standPat >= beta) {
            return beta;
        }

        if (alpha < standPat) {
            alpha = standPat;
        }

        int[][] legalMoves = board.generateNoiseMoves(board.getSideToMove());

        int bestScore = standPat;
        for (int[] move : legalMoves) {

            board.makeMove(move);
            int score = -qs(board, -beta, -alpha, ply + 1);
            board.unmakeMove(move);

            //Our current Score is better than the previous bestScore so we update it
            if (score > bestScore) {
                bestScore = score;

                //The Score is greater than alpha, so we update alpha to the score
                if (score > alpha) {
                    alpha = score;
                }

                //Beta cutoff
                if (score >= beta) {
                    break;
                }
            }
        }
        return alpha;
    }

    public int bench() {
        initStack();
        startTime = System.currentTimeMillis();
        shouldStop = false;
        short benchDepth = 6;

        Board board = new Board(10, 6);
        int nodeCount = 0;

        board.setBoardNotation("0000000000000000200000001000000200000010000100020000000000000001000100000000000000002000000000000000o");
        getBestMove(board, benchDepth);
        nodeCount += this.nodes;

        board.setBoardNotation("0000000000002000010000001102210000020100001000201000202000000012011020000021201000200000000000000000o");
        getBestMove(board, benchDepth);
        nodeCount += this.nodes;

        board.setBoardNotation("0000000000010010200001201000100200202000000010002000001200000020000010002000000000100000000000000000x");
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

    public void initStack() {
        for (int i = 0; i < stack.length; i++) {
            stack[i] = new Stack();
        }
    }

    public void speedtest() {
        int amount = 10;
        int nps = 0;
        for (int i = 0; i < amount; i++) {
            nps += bench();
        }
        System.out.println("Average speed of " + amount + " Benchmarks is: " + Math.round((float) nps / amount) + " NPS");
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

        for (int i = 1; i < 256; i++) {
            depth = i;
            score = negamax(board, i, 0, -30000, 30000);
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
        int score = negamax(board, depth, 0, -30000, 30000);
        System.out.println("Score: " + score);
        return bestMove;
    }
}