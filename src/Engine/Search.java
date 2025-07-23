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

import src.Engine.BitboardMovegen.Board;
import src.Engine.BitboardMovegen.Move;

import static src.Engine.Types.*;

public class Search {

    private final Stack[] stack = new Stack[256];
    private final Evaluation evaluate = new Evaluation();
    private final MoveOrder moveOrder = new MoveOrder();
    private final TranspositionTable transpositionTable = new TranspositionTable(16);

    // Indexed by Depth | Move counter
    short[][] reductions;
    private int nodes = 0;
    private Move bestMove = Move.NULL_MOVE;
    private History history;
    private boolean isNormalSearch = true, shouldStop = false;
    private double startTime, thinkTime;

    public int negamax(Board board, short depth, int ply, int alpha, int beta, boolean isCutNode) {

        // We want to make sure that alpha is not smaller than infinity and beta not above infinity
        // also alpha can never be greater than beta. If this assertion triggers, something seriously went wrong.
        assert(-EVAL_INFINITE <= alpha && alpha < beta && beta <= EVAL_INFINITE);

        // Increment nodes
        nodes++;

        if (!isNormalSearch) {
            // Every 4096 nodes we check for a time-out
            if (nodes % 4096 == 0) {

                // Check for a time-out
                double elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > thinkTime) {
                    shouldStop = true;
                }
            }
        }

        final boolean root = (ply == 0);

        // Make sure that depth is always lower than MAX_PLY
        if (depth >= MAX_PLY - 1) {
            depth = MAX_PLY - 1;
        }

        if (!root && shouldExit(board, ply)) {
            return ply >= MAX_PLY - 1 && !board.hasWin((byte) (board.getSideToMove() ^ 3)) ? evaluate.evaluate(board) : 0;
        }

        // We check if the opponent has won
        if (board.hasWin((byte) (board.getSideToMove() ^ 3))) {
            return -EVAL_MATE + ply;
        }  else if (board.isFull()) {
            return 0;
        }

        // Either the depth is zero or the game is over (terminal node) we return a static evaluation of the position
        if (depth <= 0) {
            return evaluate.evaluate(board);
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
        Move hashedMove = Move.NULL_MOVE;
        int staticEval = EVAL_NONE;

        // Check if we actually got a transposition entry
        if (entry != null) {

            if (entry.key() == key) {
                hashedScore = transpositionTable.scoreFromTT(entry.score(), ply);
                hashedType = entry.type();
                hashedDepth = entry.depth();
                staticEval = entry.staticEval();
                hashedMove = new Move(entry.move());
            }

            //Check if we can return a stored score
            if (!pvNode && hashedDepth >= depth && ply > 0) {
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
        if (staticEval == EVAL_NONE) {
            staticEval = evaluate.evaluate(board);
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
            int score = -negamax(board, (short) (depth - reducedDepth), ply + 1, -beta, -alpha, !isCutNode);
            board.unmakeNullMove();

            // Check for a beta cutoff
            if (score >= beta) {
                if (depth < 12)
                {
                    return score;
                }

                // Verification Search
                score = negamax(board, (short) (depth - reducedDepth), ply, beta - 1, beta, !isCutNode);

                if (score >= beta)
                {
                    return score;
                }
            }
        }

        int bestScore = -EVAL_INFINITE;

        // Generate all legal moves of the position
        Move[] legalMoves = board.generateLegalMoves();
        System.out.println(board.getBoardNotation());

        // Assign a score to all the moves
        int[] scores = moveOrder.scoreMoves(legalMoves, stack[ply].killer, hashedMove, board, history);

        Move bestMovePVS = Move.NULL_MOVE;
        short type = TranspositionTable.LOWER_BOUND;
        int moveCounter = 0;

        for (int i = 0; i < board.freeSquares; i++) {

            // Get a move based on the score of the move
            moveOrder.sort(i, legalMoves, scores);
            Move move = legalMoves[i];

            assert(move != Move.NULL_MOVE);

            // We make our move
            board.makeMove(move);

            moveCounter++;

            int score;
            // PVS
            // We assume our first move in the movelist is the best move in the position
            // So we search this move with a full window (-beta, -alpha)
            if (i == 0) {
                score = -negamax(board, (short) (depth - 1), ply + 1, -beta, -alpha, false);
            } else {

                // Late Move Reductions
                // Moves that are ordered closer to the end typically are worse,
                // So we search these types of moves with less depth
                int lmr = 0;
                if (depth > 2) {
                    lmr = reductions[depth][moveCounter];

                    // Reduce less if we are in a PvNode
                    lmr -= pvNode ? 2 : 0;

                    /*
                    LLR        : 1.69
                    ELO        : 20.94 +- 13.68
                    Games      : [677, 441, 792] | Total: 1910 | Draw Percent: 23.09
                    */
                    lmr += isCutNode ? 1 : 0;
                    lmr = Math.clamp(lmr, 0, depth - 1);
                }

                // Since we think that we already searched our best move in the position, we search the other moves
                // with a very small window (-alpha - 1, -alpha)
                score = -negamax(board, (short) (depth - lmr - 1), ply + 1, -alpha - 1, -alpha, true);

                // If our search reruns a score, that was not our assumption i.e., our first move wasn't the best
                // we need to do an expensive re-search with a full window (-beta, -alpha)
                if (score > alpha && (score < beta || lmr > 0)) {
                    score = -negamax(board, (short) (depth - 1), ply + 1, -beta, -alpha, false);
                }
            }

            // We unmake the move
            board.unmakeMove(move);

            assert(score > -EVAL_INFINITE && score < EVAL_INFINITE);

            if (shouldStop && this.bestMove != null) {
                return 0;
            }

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

                    /*
                    if (staticEval < 20 && staticEval > -20) {
                        int quietHistoryBonus = Math.min(4 + 20 * depth, 100);
                        int quietHistoryMalus = Math.min(2 + 10 * depth, 200);

                        history.updateQuietHistory(board.getSideToMove(), move, quietHistoryBonus);

                        // History malus
                        // Since we don't want the history scores to be over saturated, and we want to
                        // penalize all other quiet moves since they are not promising, we apply a negative
                        // bonus to all other quiet moves so they get lower ranked in move ordering
                        for (int x = 0; x < moveCounter; x++) {
                            int[] madeMove = legalMoves[x];
                            if (madeMove == bestMovePVS) {
                                continue;
                            }

                            history.updateQuietHistory(board.getSideToMove(), madeMove, -quietHistoryMalus);
                        }
                    }

                     */
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
                bestMovePVS.getMoveData(), depth);


        return bestScore;
    }

    /*
    LLR        : 2.95
    ELO        : 28.87 +- 12.95
    Games      : [1133, 146, 1351]
     */

    int aspiration(short depth, int score, Board board) {
        int delta = 27;
        int alpha = Math.max(-EVAL_INFINITE, score - delta);
        int beta = Math.min(EVAL_INFINITE, score + delta);
        double finalASPMultiplier = 121 / 100.0;
        // TODO failed with 15 elo

        while (true) {
            score = negamax(board, depth, 0, alpha, beta, false);
            double elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > thinkTime) {
                return score;
            }

            if (score >= beta) {
                beta = Math.min(beta + delta, EVAL_INFINITE);
            } else if (score <= alpha) {
                beta = (alpha + beta) / 2;
                alpha = Math.max(alpha - delta, -EVAL_INFINITE);
            } else {
                break;
            }

            delta *= finalASPMultiplier;
        }

        return score;
    }

    public Move getBestMove(Board board, long thinkTime) {
        initStack();
        Move tempBestMove = Move.NULL_MOVE;
        this.bestMove = Move.NULL_MOVE;

        if (thinkTime < 0) {
            negamax(board, (short) 1, 0, -EVAL_INFINITE, EVAL_INFINITE, false);
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
            score = i >= 6 ? aspiration(i, previousScore, board) : negamax(board, i, 0, -EVAL_INFINITE, EVAL_INFINITE, false);
            previousScore = score;
            if (shouldStop) {
                break;
            }
            System.out.println("info depth " + i + scoreToUci(score) + " nodes " + nodes + " nps " + (int) Math.round(nodes / (((System.currentTimeMillis() - startTime) / 1000.0) + 1)) + " pv " + this.bestMove);
            tempBestMove = this.bestMove;
        }

        System.out.println("info depth " + depth + scoreToUci(score)+ " nodes " + nodes + " nps " + (int) Math.round(nodes / (((System.currentTimeMillis() - startTime) / 1000.0) + 1))  + " hashfull " + transpositionTable.hashfull() + " pv " + tempBestMove);
        isNormalSearch = true;
        shouldStop = false;
        return tempBestMove;
    }

    String scoreToUci(int score)  {/*
        if (score >= EVAL_MATE_IN_MAX_PLY) {
            return " mate " + (EVAL_MATE - score) / 2 + 1;
        }
        if (score <= -EVAL_MATE_IN_MAX_PLY) {
            return " mate " + (-(EVAL_MATE + score) / 2);
        }*/

        return " score cp " + score;
    }


    public Move getBestMove(Board board, int depth) {
        this.bestMove = Move.NULL_MOVE;
        initStack();
        nodes = 0;
        negamax(board, (short) depth, 0, -EVAL_INFINITE, EVAL_INFINITE, false);
        return bestMove;
    }

    public void initStack() {
        for (int i = 0; i < stack.length; i++) {
            stack[i] = new Stack();
        }
    }

    public void initHistory(Board board) {
        history = new History(board.getSize());
    }

    public int bench() {
        initStack();
        startTime = System.currentTimeMillis();
        shouldStop = false;
        isNormalSearch = true;
        short benchDepth = 9;

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

        double elapsedTime = System.currentTimeMillis() - startTime;
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
            transpositionTable.resize(16);
        }
        System.out.println("Average speed of " + amount + " Benchmarks is: " + Math.round((float) nps / amount) +
                " NPS");
    }

    public void initLMR(Board board) {
        this.reductions = new short[MAX_PLY][(board.getSize() * board.getSize()) + 1];
        double lmrBaseFinal = 45 / 100.0;
        double lmrDivisorFinal = 200 / 100.0;
        for (int depth = 0; depth < MAX_PLY; depth++) {
            for (int moveCount = 0; moveCount < (board.getSize() * board.getSize()) + 1; moveCount++) {
                reductions[depth][moveCount] = (short) Math.clamp(lmrBaseFinal + Math.log(depth) * Math.log(moveCount) / lmrDivisorFinal, -32678.0, 32677.0);
            }
        }
    }

    private boolean shouldExit(Board board, int ply) {
        return (shouldStop || ply >= MAX_PLY - 1 || board.isFull()) && this.bestMove != Move.NULL_MOVE;
    }
}