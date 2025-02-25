/*
  This file is part of the TicTacToe AI written by Jochengehtab

  Copyright (C) 2024-2025 Jochengehtab

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/


package src.Engine;

import java.util.Arrays;

public class


Search {

    private final Stack[] stack = new Stack[256];
    private final Evaluation evaluate = new Evaluation();
    private final MoveOrder moveOrder = new MoveOrder();
    private final TranspositionTable transpositionTable = new TranspositionTable(16);
    private final short infinity = 30000;
    private final short MAX_PLY = 256;
    //LMR
    // Indexed by Depth | Move counter
    short[][] reductions;
    private int nodes = 0;
    private int[] bestMove = new int[2];
    private boolean isNormalSearch = true, shouldStop = false;
    private long startTime, thinkTime;

    public int negamax(Board board, short depth, int ply, int alpha, int beta) {

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

        // Check for a game over
        if (board.hasWin((byte) (board.getSideToMove() ^ 3))) {
            return -infinity + ply;
        } else if (board.isFull()) {
            return 0;
        }

        // Either the depth is zero or the game is over (terminal node) we return a static evaluation of the position
        if (depth == 0 || board.isGameOver()) {
            return evaluate.evaluate(board, ply);
        }

        int key = board.getKey();
        boolean pvNode = beta > alpha + 1;

        /*
        LLR        : 2.97
        ELO        : 26.79 +- 11.77
        Games      : [878, 607, 1075]
         */

        // Probe the transposition table
        TranspositionTable.Entry entry = transpositionTable.probe(key);

        // Set up values that are potentially stored in the transposition table
        int hashedScore = 0;
        byte hashedType = 0;
        int hashedDepth = 0;
        int[] hashedMove = null;
        int staticEval = -50000;

        // Check if we actually got a transposition entry
        if (entry != null) {

            if (entry.key() == key) {
                hashedScore = transpositionTable.scoreFromTT(entry.score(), ply);
                hashedType = entry.type();
                hashedDepth = entry.depth();
                staticEval = entry.staticEval();
                hashedMove = entry.move();
            }

            //Check if we can return a stored score
            if (!pvNode && hashedDepth >= depth && ply > 0 && key == entry.key()) {
                if ((hashedType == TranspositionTable.EXACT) ||
                        (hashedType == TranspositionTable.UPPER_BOUND && hashedScore <= alpha) ||
                        (hashedType == TranspositionTable.LOWER_BOUND && hashedScore >= beta)) {
                    return hashedScore;
                }
            }
        }

        /*
        LLR        : 2.97
        ELO        : 23.39 +- 11.11
        Games      : [1154, 565, 1361]
         */

        // If we got not a transposition, we can expect that move order is worse so we search at a reduced depth
        if (entry == null && depth >= 4 && pvNode) {
            depth -= 1;
        }

        // If no evaluation was found in the transposition table, we statically evaluate the position
        if (staticEval == -50000) {
            staticEval = evaluate.evaluate(board, ply);
        }

        /*
        LLR        : 2.98
        ELO        : 95.7 +- 21.85
        Games      : [153, 203, 340]
         */

        // Reverse futility pruning
        // We basically return the static evaluation (with a beta tweak) when
        // the static eval minus some margin, in our case staticEval - n * depth, is above beta
        if (depth <= 4 && staticEval - 72 * depth >= beta) {
            return (staticEval + beta) / 2;
        }

        /*
        LLR        : 2.97
        ELO        : 12.43 +- 7.63
        Games      : [2261, 1408, 2481] | Total: 6150 | Draw Percent: 22.89
         */

        // Null Move Pruning
        // We pass a move to our opponent and search with a reduced depth
        // If we got a beta cutoff
        if (!pvNode && depth > 4 && staticEval >= beta) {
            board.makeNullMove();
            int reducedDepth = 3 + depth / 3;
            int score = -negamax(board, (short) (depth - reducedDepth), ply + 1, -beta, -alpha);
            board.unmakeNullMove();

            // Check for a beta cutoff
            if (score >= beta) {
                return score;
            }
        }

        int bestScore = -infinity;

        // Generate all legal moves of the position
        int[][] legalMoves = board.generateLegalMoves();

        // Assign a score to all the moves
        int[] scores = moveOrder.scoreMoves(legalMoves, stack[ply].killer, hashedMove, board);

        int[] bestMovePVS = null;
        short type = TranspositionTable.LOWER_BOUND;
        int moveCounter = 0;

        for (int i = 0; i < legalMoves.length; i++) {

            // Get a move based on the score of the move
            moveOrder.sort(i, legalMoves, scores);
            int[] move = legalMoves[i];

            // We make our move
            board.makeMove(move);

            moveCounter++;

            int score;
            // PVS
            // We assume our first move in the movelist is the best move in the position
            // So we search this move with a full window (-beta, -alpha)
            if (i == 0) {
                score = -negamax(board, (short) (depth - 1), ply + 1, -beta, -alpha);
            } else {

                // Late Move Reductions
                // Moves that are ordered closer to the end typically are worse,
                // So we search these types of moves with less depth
                int lmr = 0;
                if (depth > 2) {
                    lmr = reductions[depth][moveCounter];

                    // Reduce less if we are in a PvNode
                    lmr -= pvNode ? 2 : 0;
                    lmr = Math.clamp(lmr, 0, depth - 1);
                }

                // Since we think that we already searched our best move in the position, we search the other moves
                // with a very small window (-alpha - 1, -alpha)
                score = -negamax(board, (short) (depth - lmr - 1), ply + 1, -alpha - 1, -alpha);

                // If our search reruns a score, that was not our assumption i.e., our first move wasn't the best
                // we need to do an expensive re-search with a full window (-beta, -alpha)
                if (score > alpha && (score < beta || lmr > 0)) {
                    score = -negamax(board, (short) (depth - 1), ply + 1, -beta, -alpha);
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
                    bestMovePVS = move;
                    type = TranspositionTable.EXACT;
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

        byte finalType;

        //Calculate the node type
        if (bestScore >= beta) {
            finalType = TranspositionTable.LOWER_BOUND;
        } else if (pvNode && (type == TranspositionTable.EXACT)) {
            finalType = TranspositionTable.EXACT;
        } else {
            finalType = TranspositionTable.UPPER_BOUND;
        }


        transpositionTable.write(board.getKey(), finalType, (short) staticEval, transpositionTable.scoreToTT(bestScore, ply),
                bestMovePVS, depth);


        return bestScore;
    }

    /*
    LLR        : 2.95
    ELO        : 28.87 +- 12.95
    Games      : [1133, 146, 1351]
     */

    int aspiration(short depth, int score, Board board) {
        int delta = 27;
        int alpha = Math.max(-infinity, score - delta);
        int beta = Math.min(infinity, score + delta);
        double finalASPMultiplier = 121 / 100.0;

        while (true) {
            score = negamax(board, depth, 0, alpha, beta);
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > thinkTime) {
                return score;
            }

            if (score >= beta) {
                beta = Math.min(beta + delta, infinity);
            } else if (score <= alpha) {
                beta = (alpha + beta) / 2;
                alpha = Math.max(alpha - delta, -infinity);
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
            negamax(board, (short) 1, 0, -infinity, infinity);
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

        for (short i = 1; i < 256; i++) {
            depth = i;
            score = i >= 6 ? aspiration(i, previousScore, board) : negamax(board, i, 0, -infinity, infinity);
            previousScore = score;
            if (shouldStop) {
                break;
            }
            System.out.println("info depth " + i + " score " + score + " pv " + Arrays.toString(this.bestMove));
            tempBestMove = this.bestMove;
        }

        System.out.println("info depth " + depth + " score " + score + " hashfull " + transpositionTable.hashfull() + " pv " + Arrays.toString(tempBestMove));
        isNormalSearch = true;
        shouldStop = false;
        return tempBestMove;
    }

    public int[] getBestMove(Board board, int depth) {
        initStack();
        nodes = 0;
        negamax(board, (short) depth, 0, -infinity, infinity);
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
        short benchDepth = 7;

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

        board.setBoardNotation("2111221221211211112212110122122222121122210112221112222111121222211212112212211211121112220221122211x");
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
        int amount = 10;
        int nps = 0;
        for (int i = 0; i < amount; i++) {
            nps += bench();
        }
        System.out.println("Average speed of " + amount + " Benchmarks is: " + Math.round((float) nps / amount) +
                " NPS");
    }

    public void initLMR(Board board) {
        this.reductions = new short[MAX_PLY][board.getSize() * board.getSize()];
        double lmrBaseFinal = 45 / 100.0;
        double lmrDivisorFinal = 200 / 100.0;
        for (int depth = 0; depth < MAX_PLY; depth++) {
            for (int moveCount = 0; moveCount < (board.getSize() * board.getSize()); moveCount++) {
                reductions[depth][moveCount] = (short) Math.clamp(lmrBaseFinal + Math.log(depth) * Math.log(moveCount) / lmrDivisorFinal, -32678.0, 32678.0);
            }
        }
    }
}