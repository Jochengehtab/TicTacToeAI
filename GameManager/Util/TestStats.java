package src.GameManager.Util;

public class TestStats {
    private int wins;
    private int draws;
    private int losses;

    public TestStats() {
        this(0, 0, 0);
    }

    public TestStats(int win, int draws, int losses) {
        this.wins = win;
        this.draws = draws;
        this.losses = losses;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void incrementWins(int amount) {
        this.wins += amount;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public void incrementDraws(int amount) {
        this.draws += amount;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void incrementLosses(int amount) {
        this.losses += amount;
    }

    public boolean isEmpty() {
        return this.wins == 0 && this.draws == 0 && this.losses == 0;
    }
}