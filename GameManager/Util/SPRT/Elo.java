package src.GameManager.Util.SPRT;

public class Elo {
    // Important: This is heavily inspired by fastchess
    public String getElo(int wins, int losses, int draws) {
        StringBuilder elo = new StringBuilder();

        int total = wins + losses + draws;
        elo.append(Math.round(((float) (-400 * Math.log(1 / ((wins + (float) draws / 2) / total) - 1) / Math.log(10))) * 100.0) / 100.0);

        double score = ((double) wins / total) + 0.5 * ((double) draws / total);
        double variance = ((double) wins / total) * Math.pow(1 - score, 2) + (double) (draws / total) * Math.pow(0.5 - score, 2) + (double) losses / total * Math.pow(-score, 2);
        final double CONST = 1.959963984540054d;
        double holder = CONST * Math.sqrt(variance / total);

        // Append the error margin
        elo.append(" +- ").append(Math.round(((scoreToEloDiff(score + holder) - scoreToEloDiff(score - holder)) / 2.0) * 100.0) / 100.0);
        return elo.toString();
    }

    private double scoreToEloDiff(double score) {
        return -400.0 * Math.log10(1.0 / score - 1.0);
    }
}
