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

import src.Engine.Board;

import java.io.*;
import java.time.DateTimeException;
import java.util.*;

public class GameManager {
    // This has the following Layout
    // Dev Engine Losses | Draws | Dev Engine Wins
    private static final int[] games = new int[]{2259, 1404, 2477};
    private static final int generateHalfMoves = 6;
    private static final int AMOUNT_THREADS = 5;
    private final Elo elo = new Elo();
    private final String currentPath = System.getProperty("user.dir");
    private final String DEV = (!currentPath.contains("GameManager") ?
            currentPath + "\\GameManager" : "") + "\\dev.jar";
    private final String BASE = (!currentPath.contains("GameManager") ?
            currentPath + "\\GameManager" : "") + "\\base.jar";

    Checkpoint checkpoint = new Checkpoint();

    public static void main(String[] args) {
        GameManager gameManager = new GameManager();

        LLR llr = new LLR();

        if (gameManager.DEV.equals(gameManager.BASE)) {
            System.err.println("The two engines are the same!");
        }

        //noinspection ConstantValue
        if (generateHalfMoves % 2 != 0) {
            System.err.println("When generating a random position, it is not X-Turn");
        }

        double currentLLR;
        int iteration = 0;

        // Check if we start from a checkpoint
        if (games[0] != 0) {
            // Calculate the iteration based on the number of games
            iteration = (games[2] + games[1] + games[0]) / (AMOUNT_THREADS * 2);
            System.out.println("Resuming from checkpoint.");
        }

        // Play games until our LLR exceeds one of the two bounds
        do {
            // Generate a list of all Game Player threads
            ArrayList<Thread> threads = getThreads(gameManager);

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
            int wins = games[2];
            int draws = games[1];
            int losses = games[0];
            int total = wins + draws + losses;

            // If we don't get the amount AMOUNT_THREADS * 2, something went wrong
            if (total / iteration != AMOUNT_THREADS * 2) {
                System.err.println("Expected " + AMOUNT_THREADS * 2 + " games, got " + total / iteration + " games!");
            }

            // Calculate the new LLR based on the new stats
            currentLLR = llr.getLLR(wins, draws, losses);

            // Log the current stats to the console
            gameManager.logStats(currentLLR, wins, losses, draws);

        } while (!(currentLLR > 2.95) && !(currentLLR <= -2.95));
    }

    /**
     * Create and starts all Threads for the {@link GamePlayer}
     *
     * @param gameManager An Instance of {@link GameManager}
     * @return Returns a {@link ArrayList} of all the startet Threads
     */
    private static ArrayList<Thread> getThreads(GameManager gameManager) {
        ArrayList<Thread> threads = new ArrayList<>();

        // Create for every thread a Game Player Object
        for (int i = 0; i < AMOUNT_THREADS; i++) {

            // Spawn the two engines
            Engine devEngine = new Engine(gameManager.DEV, "dev");
            Engine baseEngine = new Engine(gameManager.BASE, "base");

            // Create the thread and start them
            Thread thread = new Thread(() -> new GamePlayer(devEngine, baseEngine).playGame());
            thread.start();

            // Adds the thread to the list
            threads.add(thread);
        }
        return threads;
    }

    /**
     * Synchronized method that receives updated wdl from different threads
     *
     * @param wdl The wdl to add
     */
    private static synchronized void updateScore(int[] wdl) {
        games[0] += wdl[0];
        games[1] += wdl[1];
        games[2] += wdl[2];
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
        System.out.println("Games      : " + Arrays.toString(GameManager.games) +
                " | Total: " + total +
                " | Draw Percent: " + Math.round((((float) draws / total) * 100) * 100.0) / 100.0);
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

    private final static class GamePlayer {
        private final Engine devEngine;
        private final Engine baseEngine;
        private final Random random = new Random();
        private final Board board = new Board(10, 5);

        /**
         * Default constructor
         *
         * @param devEngine  The dev {@link Engine}
         * @param baseEngine The base {@link Engine}
         */
        public GamePlayer(Engine devEngine, Engine baseEngine) {
            this.devEngine = devEngine;
            this.baseEngine = baseEngine;
        }

        /**
         * Plays a game pair between the dev and base engine
         */
        private void playGame() {
            int[] wdl = new int[3];

            boolean isValidBoard = false;
            String boardNotation = "";

            // Generate a random starting position
            while (!isValidBoard) {
                // Reset the board that in case we are in a second iteration or higher
                board.reset();

                // Make x (as defined above) random moves
                for (int i = 0; i < generateHalfMoves; i++) {

                    // Generate all legal moves
                    int[][] legalMoves = board.generateLegalMoves();

                    // Make a random move on the board
                    board.makeMove(legalMoves[random.nextInt(legalMoves.length)]);
                }

                // Check for a noise position, either the game is over or we are near a game over
                if (!board.isGameOver() &&
                        !board.hasWinWithFurtherOffset(1, board.X_SIDE) &&
                        !board.hasWinWithFurtherOffset(1, board.O_SIDE)) {
                    isValidBoard = true;
                    boardNotation = board.getBoardNotation();
                }
            }

            // Set up the time management stuff
            int xTime = 8000;
            int oTime = 8000;
            int xInc = 8;
            int oInc = 8;
            long startTime;

            // Play the first game where the dev Engine starts first
            while (!board.isGameOver()) {

                // Get the best move from the dev Engine
                devEngine.sendCommand("position " + board.getBoardNotation());
                devEngine.sendCommand("go xTime " + xTime + " oTime " + oTime + " xInc " + xInc + " oInc " + oInc);
                startTime = System.currentTimeMillis();

                // We wait for the engine to respond
                makeMove(devEngine);

                // Update the time
                xTime -= (int) (System.currentTimeMillis() - startTime);
                xTime += xInc;

                // X made a move so we check for an X win
                if (board.hasWin(board.X_SIDE)) {
                    wdl[2] += 1;
                    break;
                }

                // Check for a draw
                if (board.isFull()) {
                    wdl[1] += 1;
                    break;
                }

                // Get the best move from the base Engine
                baseEngine.sendCommand("position " + board.getBoardNotation());
                baseEngine.sendCommand("go xTime " + xTime + " oTime " + oTime + " xInc " + xInc + " oInc " + oInc);
                startTime = System.currentTimeMillis();

                // We wait for the engine to respond
                makeMove(baseEngine);

                // Update the time
                oTime -= (int) (System.currentTimeMillis() - startTime);
                oTime += oInc;

                // O made a move so we check for an O win
                if (board.hasWin(board.O_SIDE)) {
                    wdl[0] += 1;
                    break;
                }

                // Check for a draw
                if (board.isFull()) {
                    wdl[1] += 1;
                    break;
                }
            }

            // Reset the board
            board.setBoardNotation(boardNotation);

            // Reset the time for both sides
            xTime = 8000;
            oTime = 8000;

            // Play the second game where the base Engine starts first
            while (!board.isGameOver()) {

                // Get the best move from the base Engine
                baseEngine.sendCommand("position " + board.getBoardNotation());
                baseEngine.sendCommand("go xTime " + xTime + " oTime " + oTime + " xInc " + xInc + " oInc " + oInc);
                startTime = System.currentTimeMillis();

                // We wait for the engine to respond
                makeMove(baseEngine);

                // Update the time
                xTime -= (int) (System.currentTimeMillis() - startTime);
                xTime += xInc;


                // X made a move so we check for an X win
                if (board.hasWin(board.X_SIDE)) {
                    wdl[0] += 1;
                    break;
                }

                // Check for a draw
                if (board.isFull()) {
                    wdl[1] += 1;
                    break;
                }

                // Get the best move from the dev Engine
                devEngine.sendCommand("position " + board.getBoardNotation());
                devEngine.sendCommand("go xTime " + xTime + " oTime " + oTime + " xInc " + xInc + " oInc " + oInc);
                startTime = System.currentTimeMillis();

                // We wait for the engine to respond
                makeMove(devEngine);

                // Update the time
                oTime -= (int) (System.currentTimeMillis() - startTime);
                oTime += oInc;

                // O made a move so we check for an O win
                if (board.hasWin(board.O_SIDE)) {
                    wdl[2] += 1;
                    break;
                }

                // Check for a draw
                if (board.isFull()) {
                    wdl[1] += 1;
                    break;
                }
            }

            // Update the wdl
            updateScore(wdl);

            // We need to close the engine processes or else we run out of memory
            devEngine.close();
            baseEngine.close();
        }

        /**
         * Waits for the engine output and makes the move on the board
         *
         * @param engine The engine which should make the move
         */
        private void makeMove(Engine engine) {
            ArrayList<String> output = engine.getOutput("bestmove");
            //noinspection ExtractMethodRecommender
            String bestMoveLine;

            try {
                // The best move is always in the last line
                bestMoveLine = output.getLast();
            } catch (NoSuchElementException e) {
                // It could be that we got no last element which indicates
                // that the engine process was not open probable
                throw new RuntimeException("No element was found, probably the engine '" + engine.getName() + "' isn't opened properly!", e);
            }

            // Extract the two coordinates
            String[] numbers = bestMoveLine.substring(bestMoveLine.indexOf("[") + 1, bestMoveLine.indexOf("]")).split(", ");

            // Parse the two coordinates
            int x = Integer.parseInt(numbers[0]);
            int y = Integer.parseInt(numbers[1]);

            // Check if the engine made an illegal move
            if (board.get(x, y) != 0) {
                System.err.println("The engine: " + engine.getName() + " made an illegal move!");
            }

            // Make the move on the board
            board.makeMove(x, y);
        }
    }

    final static class Engine {
        private final Process process;
        private final BufferedWriter commandWriter;
        private final BufferedReader outputReader;
        private final BufferedReader errorReader;
        private final String name;

        /**
         * Opens a Process with the corresponding engine
         *
         * @param jarFilePath The Path to the jar file
         * @param name        The name of the engine
         */
        public Engine(String jarFilePath, String name) {

            this.name = name;

            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", jarFilePath);

            // Spawn the new process
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Open all the streams
            commandWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Start a thread to read and print the error stream
            new Thread(() -> {
                String line;
                try {
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println(line);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }


        /**
         * Sends a command to the running process.
         *
         * @param command The command to send.
         * @throws IllegalStateException If no process is running.
         */
        public void sendCommand(String command) {

            // Check if the process is still running
            if (process == null || !process.isAlive()) {
                throw new IllegalStateException("No process is running.");
            }

            // Send the command to the console
            try {
                commandWriter.write(command);
                commandWriter.newLine();
                commandWriter.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Reads the output from the running process.
         *
         * @return A list of output lines from the process.
         */
        public ArrayList<String> getOutput(String stopSignal) {
            if (process == null || !process.isAlive()) {
                throw new IllegalStateException("No process is running.");
            }

            ArrayList<String> outputLines = new ArrayList<>();
            String line;
            while (true) {
                try {
                    line = outputReader.readLine();
                    if (line == null) {
                        break;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                outputLines.add(line);
                if (line.contains(stopSignal)) {
                    break;
                }
            }

            return outputLines;
        }

        /**
         * Closes the running process.
         */
        public void close() {
            if (process != null) {
                if (process.isAlive()) {
                    process.destroy();
                }
                try {
                    commandWriter.close();
                    outputReader.close();
                    errorReader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * Gets the name of the engine
         */
        public String getName() {
            return this.name;
        }
    }

    private final class Checkpoint {
        private final UUID uuid = UUID.randomUUID();
        private final File file = new File("checkpoint\\" + uuid);
        public Checkpoint() {

        }

        public void save() {

        }

        public void loadFromLatestCheckPoint() {

        }
    }

    private final static class LLR {

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

            double elo0 = 0.0;
            double elo1 = 5.0;

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

    private final static class Elo {
        // Important: This is heavily inspired by fastchess
        private String getElo(int wins, int losses, int draws) {
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
}