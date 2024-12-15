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

    int nodes = 0;
    int[] bestMove = new int[2];

    public int evaluate(Board board, int ply) {
        final byte xSide = 1;
        final byte oSide = 2;

        short xEval = 0;
        short oEval = 0;

        if (board.hasWinWithFurtherOffset(1, xSide)) {
            xEval += 50;
        }

        if (board.hasWinWithFurtherOffset(1, oSide)) {
            oEval += 50;
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

        nodes++;
        if (depth == 0 || board.isGameOver()) {
            return evaluate(board, ply);
        }

        int bestScore = -30000;
        int[][] legalMoves = board.generateLegalMoves();

        for (int i = 0; i < legalMoves.length; i++) {

            int[] move = legalMoves[i];

            board.makeMove(move);

            int score;
            if (i == 0) {
                score = -negamax(board, depth - 1, ply + 1, -beta, -alpha);
            } else {
                score = -negamax(board, depth - 1, ply + 1, -alpha-1, -alpha);
                if ( score > alpha && beta - alpha > 1 ) {
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
                    return bestScore;
                }
            }
        }
        return bestScore;
    }

    public void bench() {

        long startTime = System.currentTimeMillis();

        Board board = new Board(10, 7);
        int nodeCount = 0;

        board.setBoardNotation("0000000000000000200000001000000200000010000100020000000000000001000100000000000000002000000000000000o");
        getBestMove(board, 5);
        nodeCount += this.nodes;

        board.setBoardNotation("0000000000002000010000001102210000020100001000201000202000000012011020000021201000200000000000000000o");
        getBestMove(board, 5);
        nodeCount += this.nodes;

        board.setBoardNotation("0000000000010010200001201000100200202000000010002000001200000020000010002000000000100000000000000000x");
        getBestMove(board, 5);
        nodeCount += this.nodes;

        long elapsedTime = System.currentTimeMillis() - startTime;
        double elapsedTimeSeconds = elapsedTime / 1000.0;

        System.out.println("Time  : " + elapsedTime + " ms");
        System.out.println("Nodes : " + nodeCount);
        System.out.println("NPS   : " + Math.round(nodeCount / elapsedTimeSeconds));
    }

    public int[] getBestMove(Board board, long thinkTime) {
        int[] tempBestMove = new int[2];
        nodes = 0;
        int searchDepth = 1;

        long startTime = System.currentTimeMillis();

        while (!((System.currentTimeMillis() - startTime) >= thinkTime)) {

            negamax(board, searchDepth, 0, -30000, 30000);
            tempBestMove = this.bestMove;
            searchDepth++;

            if (searchDepth == 256) {
                break;
            }
        }

        System.out.println("Depth: " + searchDepth);
        System.out.println("Nodes: " + nodes);

        return tempBestMove;
    }

    public int[] getBestMove(Board board, int depth) {
        nodes = 0;
        int score = negamax(board, depth, 0, -30000, 30000);
        System.out.println("Score: " + score);
        return bestMove;
    }
}