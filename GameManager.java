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


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class GameManager {

    private final Random random = new Random();
    private final Engine firstEngine = new Engine();
    private final Engine secondEngine = new Engine();
    private final String FIRST_NAME = "dev.jar";
    private final String SECOND_NAME = "base.jar";

    public static void main(String[] args) {
        GameManager gameManager = new GameManager();
        Elo elo = new Elo();
        LLR llr = new LLR();

        //noinspection ConstantValue
        if (gameManager.FIRST_NAME.equals(gameManager.SECOND_NAME)) {
            System.err.println("The two engines are the same!");
        }

        gameManager.firstEngine.openEngine(gameManager.FIRST_NAME);
        gameManager.secondEngine.openEngine(gameManager.SECOND_NAME);

        int[] games = new int[3];
        /*
        Resume form checkpoint

        games[0] = 2166;
        games[1] = 2;
        games[2] = 2230;
         */

        double currentLLR;

        while (true) {
            int[] game = gameManager.playGame();
            games[0] += game[0];
            games[1] += game[1];
            games[2] += game[2];

            int wins = games[2];
            int draws = games[1];
            int losses = games[0];

            currentLLR = llr.getLLR(wins, draws, losses);

            System.out.println("LLR        : " + currentLLR);
            System.out.println("ELO        : " + elo.getElo(wins, losses, draws));
            System.out.println("Games      : " + Arrays.toString(games));
            System.out.println();

            if (currentLLR >= 2.95) {
                break;
            }

            if (currentLLR <= -2.91) {
                break;
            }
        }

        gameManager.firstEngine.close();
        gameManager.secondEngine.close();
    }

    private int[] playGame() {
        int[] wdl = new int[3];
        Board board = new Board(10, 5);

        boolean isValidBoard = false;
        String boardNotation = "";

        // Generate a random position
        while (!isValidBoard) {
            board.reset();
            for (int i = 0; i < 4; i++) {
                int[][] legalMoves = board.generateLegalMoves();
                board.makeMove(legalMoves[random.nextInt(legalMoves.length - 1)]);
            }

            if (!board.isGameOver()) {
                isValidBoard = true;
                boardNotation = board.getBoardNotation();
            }
        }

        int xTime = 8000;
        int oTime = 8000;
        int xInc = 8;
        int oInc = 8;

        long startTime = System.currentTimeMillis();

        while (!board.isGameOver()) {
            firstEngine.sendCommand("position " + board.getBoardNotation());
            firstEngine.sendCommand("go xTime " + xTime + " oTime " + oTime + " xInc " + xInc + " oInc " + oInc);
            makeMove(board, firstEngine);
            xTime -= (int) (System.currentTimeMillis() - startTime);
            xTime += xInc;

            if (board.hasDiagonalWin((byte) (board.getSideToMove() == 1 ? 2 : 1)) || board.hasRowColumnWin((byte) (board.getSideToMove() == 1 ? 2 : 1))) {
                wdl[2] += 1;
                break;
            }

            if (board.isFull()) {
                wdl[1] += 1;
                break;
            }

            secondEngine.sendCommand("position " + board.getBoardNotation());
            secondEngine.sendCommand("go xTime " + xTime + " oTime " + oTime + " xInc " + xInc + " oInc " + oInc);
            makeMove(board, secondEngine);
            oTime -= (int) (System.currentTimeMillis() - startTime);
            oTime += oInc;

            if (board.hasDiagonalWin((byte) (board.getSideToMove() == 1 ? 2 : 1)) || board.hasRowColumnWin((byte) (board.getSideToMove() == 1 ? 2 : 1))) {
                wdl[0] += 1;
                break;
            }

            if (board.isFull()) {
                wdl[1] += 1;
                break;
            }
        }

        // Reset everything
        board.setBoardNotation(boardNotation);

        startTime = System.currentTimeMillis();

        while (!board.isGameOver()) {
            secondEngine.sendCommand("position " + board.getBoardNotation());
            secondEngine.sendCommand("go xTime " + xTime + " oTime " + oTime + " xInc " + xInc + " oInc " + oInc);
            makeMove(board, secondEngine);
            xTime -= (int) (System.currentTimeMillis() - startTime);
            xTime += xInc;

            if (board.hasDiagonalWin((byte) (board.getSideToMove() == 1 ? 2 : 1)) || board.hasRowColumnWin((byte) (board.getSideToMove() == 1 ? 2 : 1))) {
                wdl[0] += 1;
                break;
            }

            if (board.isFull()) {
                wdl[1] += 1;
                break;
            }

            firstEngine.sendCommand("position " + board.getBoardNotation());
            firstEngine.sendCommand("go xTime " + xTime + " oTime " + oTime + " xInc " + xInc + " oInc " + oInc);
            makeMove(board, firstEngine);
            oTime -= (int) (System.currentTimeMillis() - startTime);
            oTime += oInc;

            if (board.hasDiagonalWin((byte) (board.getSideToMove() == 1 ? 2 : 1)) || board.hasRowColumnWin((byte) (board.getSideToMove() == 1 ? 2 : 1))) {
                wdl[2] += 1;
                break;
            }

            if (board.isFull()) {
                wdl[1] += 1;
                break;
            }
        }

        return wdl;
    }

    private void makeMove(Board board, Engine engine) {
        ArrayList<String> output = engine.getOutput("bestmove");
        String bestMoveLine = output.getLast();
        String[] numbers = bestMoveLine.substring(bestMoveLine.indexOf("[") + 1, bestMoveLine.indexOf("]")).split(", ");
        board.makeMove(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]));
    }

    private final static class Engine {
        private Process process;
        private BufferedWriter commandWriter;
        private BufferedReader outputReader;

        /**
         * Opens a JAR file in a new process.
         *
         * @param jarFilePath Path to the JAR file to be executed.
         */
        public void openEngine(String jarFilePath) {
            if (process != null && process.isAlive()) {
                throw new IllegalStateException("This process already exists.");
            }

            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", jarFilePath);

            try {
                process = processBuilder.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            commandWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        }

        /**
         * Sends a command to the running process.
         *
         * @param command The command to send.
         * @throws IllegalStateException If no process is running.
         */
        public void sendCommand(String command) {
            if (process == null || !process.isAlive()) {
                throw new IllegalStateException("No process is running.");
            }

            try {
                commandWriter.write(command);
                commandWriter.newLine();
                commandWriter.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Reads the output from the running process.s
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
                    if ((line = outputReader.readLine()) == null) break;
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
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
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
