package src.SPSA;

import src.Engine.Movegen.Board;
import src.Engine.Movegen.Move;
import src.Engine.Search;

import java.util.List;
import java.util.Random;

import static src.Engine.Types.O_SIDE;
import static src.Engine.Types.X_SIDE;

public class SelfPlay {

    private static void playRandomOpening(Board board, Random rng, int plies) {
        int p = 0;
        while (p < plies && !board.isGameOver()) {
            Move[] moves = board.generateLegalMoves();
            if (moves.length == 0) break;
            Move mv = moves[rng.nextInt(moves.length)];
            board.makeMove(mv);
            p++;
        }
    }

    private static long     allocateMoveTime(long remainingMs, long incMs) {
        long budget = (long) (remainingMs / 20.0 + incMs / 2.0);
        long safety = 2; // ms
        return Math.max(1, Math.min(budget, Math.max(1, remainingMs - safety)));
    }

    // Play ONE game with time control; returns X's score in {1.0, 0.5, 0.0}
    private static double playOneGameTimeControl(int size, int offset,
                                                 long baseTimeMs, long incMs,
                                                 int randomOpeningPlies,
                                                 List<Param> xParams, List<Param> oParams,
                                                 ParamApplier applier,
                                                 long seed) {

        Board board = new Board(size, offset);
        Random rng = new Random(seed);
        playRandomOpening(board, rng, randomOpeningPlies);


        TunableSearch sx = new TunableSearch();
        TunableSearch so = new TunableSearch();

        sx.initHistory(board);
        so.initHistory(board);

        for (Param p : xParams) applier.apply(sx, board, p);
        for (Param p : oParams) applier.apply(so, board, p);

        boolean xToMove = true; // Board starts with X to move
        long xClock = baseTimeMs;
        long oClock = baseTimeMs;
        int safetyPlyCap = 2048;

        while (!board.isGameOver() && safetyPlyCap-- > 0) {
            Search s = xToMove ? sx : so;
            long remaining = xToMove ? xClock : oClock;
            if (remaining <= 0) {
                // flag fall
                return xToMove ? 0.0 : 1.0;
            }
            long thinkMs = allocateMoveTime(remaining, incMs);
            long t0 = System.currentTimeMillis();
            Move mv = s.getBestMove(board, thinkMs, true);
            long elapsed = Math.max(0, System.currentTimeMillis() - t0);
            board.makeMove(mv);

            if (xToMove) {
                xClock -= elapsed;
                if (xClock < 0) return 0.0;
                xClock += incMs;
            } else {
                oClock -= elapsed;
                if (oClock < 0) return 1.0;
                oClock += incMs;
            }
            xToMove = !xToMove;
        }

        if (board.hasWin(X_SIDE)) return 1.0;
        if (board.hasWin(O_SIDE)) return 0.0;
        return 0.5;
    }

    // Play a PAIRED evaluation for one seed: θ+ as X vs θ−, then swap colors.
    // Returns (scorePlusAsX - scoreMinusAsX) in [-1, 1].
    public static double playPairTimeControl(int size, int offset,
                                             long baseTimeMs, long incMs,
                                             int randomOpeningPlies,
                                             List<Param> plus, List<Param> minus,
                                             ParamApplier applier,
                                             long seed) {

        double sA = playOneGameTimeControl(size, offset, baseTimeMs, incMs, randomOpeningPlies,
                plus, minus, applier, seed);
        double sB = playOneGameTimeControl(size, offset, baseTimeMs, incMs, randomOpeningPlies,
                minus, plus, applier, seed);
        return sA - sB; // equals f_plus - f_minus for this seed
    }
}