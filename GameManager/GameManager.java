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

package src.GameManager;

import src.GameManager.Util.Checkpoint;
import src.GameManager.Util.Engine;
import src.GameManager.Util.GamePlayer;
import src.GameManager.Util.SPRT.Elo;
import src.GameManager.Util.SPRT.LLR;
import src.GameManager.Util.TestStats;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class GameManager {
    private final Checkpoint checkpoint = new Checkpoint();
    private final int generateHalfMoves = 6;
    private final int AMOUNT_THREADS = 4;
    private final TestStats testStats = new TestStats();
    private final Elo elo = new Elo();
    private final Path ENGINE_DIR = Paths.get("GameManager");
    private final String DEV = ENGINE_DIR.resolve("engines/dev.jar").toString();
    private final String BASE = ENGINE_DIR.resolve("engines/base.jar").toString();

    public static void main(String[] args) {
        new GameManager().execute();
    }

    private void execute() {
        LLR llr = new LLR();

        if (DEV.equals(BASE)) {
            System.err.println("The two engines are the same!");
        }

        //noinspection ConstantValue
        if (generateHalfMoves % 2 != 0) {
            System.err.println("When generating a random position, it is not X-Turn");
        }

        double currentLLR;
        int iteration = 0;

        // Check if we start from a checkpoint
        checkpoint.loadCheckpoint(testStats);
        if (!testStats.isEmpty()) {
            // Calculate the iteration based on the number of games
            iteration = (testStats.getWins() + testStats.getDraws() + testStats.getLosses()) / (AMOUNT_THREADS * 2);
            System.out.println("Resuming from checkpoint.");
        }

        // TODO when test finishes, clear the checkpoint
        // Play games until our LLR exceeds one of the two bounds
        do {
            // Generate a list of all Game Player threads
            ArrayList<Thread> threads = getThreads();

            // Increment the iteration
            iteration++;

            // Loop over all the threads and wait for them to finish
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // Get all the stats after all games have finished
            int wins = testStats.getWins();
            int draws = testStats.getDraws();
            int losses = testStats.getLosses();
            int total = wins + draws + losses;

            // If our total amount of games does not equal to AMOUNT_THREADS * 2, something went wrong
            if (total / iteration != AMOUNT_THREADS * 2) {
                System.err.println("Expected " + AMOUNT_THREADS * 2 + " games, got " + total / iteration + " games!");
            }

            // Calculate the new LLR based on the new stats
            currentLLR = llr.getLLR(wins, draws, losses);

            // Log the current stats to the console
            logStats(currentLLR, wins, losses, draws);

        } while (!(currentLLR > 2.95) && !(currentLLR <= -2.95));
    }

    /**
     * Create and starts all Threads for the {@link GamePlayer}
     *
     * @return Returns a {@link ArrayList} of all the startet Threads
     */
    private ArrayList<Thread> getThreads() {
        ArrayList<Thread> threads = new ArrayList<>();

        // Create for every thread a Game Player Object
        for (int i = 0; i < AMOUNT_THREADS; i++) {

            // Spawn the two engines
            Engine devEngine = new Engine(DEV, "dev");
            Engine baseEngine = new Engine(BASE, "base");

            // Create the thread and start them
            Thread thread = new Thread(() -> new GamePlayer(devEngine, baseEngine, checkpoint, testStats).playGame(generateHalfMoves));
            thread.start();

            // Adds the thread to the list
            threads.add(thread);
        }
        return threads;
    }

    /**
     * Logs the stats to the console prettily
     *
     * @param currentLLR The current LLR
     * @param wins       The number of wins from the dev engine
     * @param losses     The number of losses from the dev engine
     * @param draws      The number of draws
     */
    private void logStats(double currentLLR, int wins, int losses, int draws) {

        // Determent the color based on the LLR
        String llrColor = currentLLR >= 0 ? "\u001B[32;1m" : "\u001B[31;1m";

        final int total = wins + draws + losses;

        System.out.println("LLR        : " + llrColor + currentLLR + "\u001B[0m");
        System.out.println("ELO        : " + elo.getElo(wins, losses, draws));
        System.out.println("Games      : [" + testStats.getLosses() + ", " + testStats.getDraws() + ", " + testStats.getWins() + "]" +
                " | Total: " + total +
                " | Draw Percentage: " + Math.round((((float) draws / total) * 100) * 100.0) / 100.0);
        System.out.println("Progress   : " + getProgressBar(currentLLR));
    }

    /**
     * Creates a progress bar
     *
     * @param llr The current LLR on which the progress bar is based on
     * @return The final progress bar
     */
    private String getProgressBar(double llr) {
        short totalBars = 50;

        // Color the progress bar based on the value
        String progressBarColor = llr >= 0 ? "\u001B[32m" : "\u001B[31m";
        String resetColor = "\u001B[0m";

        // Calculate how many bars should be filled
        short progressAmount = (short) ((llr * 100) / 5.9);

        // If the LLR is negative, the progressAmount is also negative,
        // so we need to invert it
        if (progressAmount < 0) {
            progressAmount = (short) -progressAmount;
        }

        // If we don't fill up any bars, we return an empty bar
        if (progressAmount == 0) {
            // This inserts n white spaces with the String.format();
            return progressBarColor + "[" + String.format("%" + totalBars + "s", " ") + "]" + resetColor;
        }

        short filled = 0;

        StringBuilder progressBar = new StringBuilder("[");

        // Fill up the progress bar
        for (int i = 0; i < totalBars; i++) {
            if (filled <= progressAmount) {
                progressBar.append('█');
                filled++;
            } else {
                progressBar.append(" ");
            }
        }
        progressBar.append("]");

        return progressBarColor + progressBar + resetColor;
    }
}