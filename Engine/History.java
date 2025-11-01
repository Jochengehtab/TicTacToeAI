package src.Engine;

import src.Engine.Movegen.Move;

public class History {
    public int[][][] quietHistory;

    public History(int size) {
        quietHistory = new int[3][size][size];
    }

    public void updateQuietHistory(int side, Move move, int bonus) {
        quietHistory[side][move.x()][move.y()] += (bonus - getQuietHistory(side, move) * Math.abs(bonus) / 5000);
    }

    public int getQuietHistory(int side, Move move) {
        return quietHistory[side][move.x()][move.y()];
    }
}
