package src.SPSA;

import src.Engine.Movegen.Board;
import src.Engine.Search;

import java.lang.reflect.Field;

import static src.Engine.Types.MAX_PLY;

public class TunableSearch extends Search {
    private double lmrBase = 0.45;
    private double lmrDivisor = 2.00;

    public void setLmrBase(int percent) {
        this.lmrBase = percent / 100.0;
    }

    public void setLmrDivisor(int percent) {
        this.lmrDivisor = Math.max(0.01, percent / 100.0);
    }

    @Override
    public void initLMR(Board board) {
        super.initLMR(board);
        try {
            int sideSize = board.getSize() * board.getSize();
            short[][] reductions = new short[MAX_PLY][sideSize];
            for (int depth = 0; depth < MAX_PLY; depth++) {
                for (int moveCount = 0; moveCount < sideSize; moveCount++) {
                    double v = (depth <= 0 || moveCount <= 0)
                            ? lmrBase
                            : lmrBase + Math.log(depth) * Math.log(moveCount) / lmrDivisor;
                    v = Math.max(-32678.0, Math.min(32678.0, v));
                    reductions[depth][moveCount] = (short) Math.round(v);
                }
            }
            Field f = Search.class.getDeclaredField("reductions");
            f.setAccessible(true);
            f.set(this, reductions);
        } catch (Exception e) {
            throw new RuntimeException("TunableSearch: failed to override reductions: " + e.getMessage(), e);
        }
    }
}
