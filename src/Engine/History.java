package src.Engine;

public class History {
    public int[][][] quietHistory;
    public History(int size) {
        quietHistory = new int[3][size][size];
    }

    public void updateQuietHistory(byte side, int[] move, int bonus) {
        quietHistory[side][move[0]][move[1]] += (bonus - getQuietHistory(side, move) * Math.abs(bonus) / 5000);
    }

    public int getQuietHistory(byte side, int[] move) {
        return quietHistory[side][move[0]][move[1]];
    }
}
