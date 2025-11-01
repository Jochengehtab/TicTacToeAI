package src.GameManager.Util.SPRT;

public class LLR {

    // Important: This is heavily inspired by fastchess
    public double getLLR(int win, int draw, int loss) {
        int zeroCount = (win == 0 ? 1 : 0) + (draw == 0 ? 1 : 0) + (loss == 0 ? 1 : 0);

        double regularize = (zeroCount >= 2 ? 1 : 0);

        double games = win + draw + loss + 1.5 * regularize;

        // If we got no games, we can't calculate a wdl
        if (games == 0) {
            return 0.0;
        }

        double W = (win + 0.5 * regularize) / games;
        double D = (draw + 0.5 * regularize) / games;
        double L = (loss + 0.5 * regularize) / games;

        double score0;
        double score1;

        double score = W + 0.5 * D;
        double variance = (W * Math.pow((1 - score), 2)) + (D * Math.pow((0.5 - score), 2)) + (L * Math.pow((0 - score), 2));

        // If we don't get any variance, we return 0.0
        if (variance == 0) {
            return 0.0;
        }

        double elo0 = -5.0;
        double elo1 = 0.0;

        score0 = nEloToScoreWDL(elo0, variance);
        score1 = nEloToScoreWDL(elo1, variance);

        double variance0 = (W * Math.pow((1 - score0), 2)) + (D * Math.pow((0.5 - score0), 2)) + (L * Math.pow((0 - score0), 2));
        double variance1 = (W * Math.pow((1 - score1), 2)) + (D * Math.pow((0.5 - score1), 2)) + (L * Math.pow((0 - score1), 2));

        if (variance0 == 0 || variance1 == 0) {
            return 0.0;
        }

        // For more information: http://hardy.uhasselt.be/Fishtest/support_MLE_multinomial.pdf
        return Math.round((0.5 * games * Math.log(variance0 / variance1)) * 100.0) / 100.0;
    }

    public double nEloToScoreWDL(double nElo, double variance) {
        return nElo * Math.sqrt(variance) / (800.0 / Math.log(10)) + 0.5;
    }
}