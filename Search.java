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
    private int nodes = 0;
    private int[] bestMove = new int[2];
    private boolean isNormalSearch = true, shouldStop = false;
    private long startTime, thinkTime;
    public TranspositionTable transpositionTable = new TranspositionTable(16);
    private final Evaluation evaluate = new Evaluation();
    private final MoveOrder moveOrder = new MoveOrder();

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
            return evaluate.evaluate(board, ply);
            //return qs(board, alpha, beta, ply);
        }

        /*
        int key = board.getKey();

        TranspositionTable.Entry entry = transpositionTable.probe(key);
        int hashedScore = 0;
        byte hashedType = 0;
        int hashedDepth = 0;
        int[] hashedMove = new int[0];
        int staticEval = -200000;
        if (entry != null) {
            if (entry.key() == key) {
                hashedScore = transpositionTable.scoreFromTT(entry.score(), ply);
                hashedType = entry.type();
                hashedDepth = entry.depth();
                staticEval = entry.staticEval();
                hashedMove = entry.move();
            }

            //Check if we can return a stored score
            if (hashedDepth >= depth && ply > 0 && key == entry.key())
            {
                if ((hashedType == TranspositionTable.EXACT) ||
                        (hashedType == TranspositionTable.UPPER_BOUND && hashedScore <= alpha) ||
                        (hashedType == TranspositionTable.LOWER_BOUND && hashedScore >= beta))
                {
                    return hashedScore;
                }
            }
        }

        if (staticEval == -200000) {
            staticEval = evaluate(board, ply);
        }

         */

        int staticEval = evaluate.evaluate(board, ply);

        boolean pvNode = beta > alpha + 1;

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
        int[] scores = moveOrder.scoreMoves(legalMoves, stack[ply].killer/*, hashedMove*/);
        int[] bestMovePVS = new int[0];

        byte type = TranspositionTable.LOWER_BOUND;

        for (int i = 0; i < legalMoves.length; i++) {

            int[] move = moveOrder.getSortedMove(legalMoves, scores, i);

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
                    bestMovePVS = move;
                    type = TranspositionTable.EXACT;
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

        /*
        byte finalType;

        //Calculate the node type
        if (bestScore >= beta) {
            finalType = TranspositionTable.LOWER_BOUND;
        }
        else if (pvNode && (type == TranspositionTable.EXACT)) {
            finalType = TranspositionTable.EXACT;
        }
        else {
            finalType = TranspositionTable.UPPER_BOUND;
        }


        transpositionTable.write(board.getKey(), finalType, staticEval, transpositionTable.scoreToTT(bestScore, ply),
        bestMovePVS, depth);


         */
        return bestScore;
    }

    private int qs(Board board, int alpha, int beta, int ply) {
        int standPat = evaluate.evaluate(board, ply);

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

    public void initStack() {
        for (int i = 0; i < stack.length; i++) {
            stack[i] = new Stack();
        }
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