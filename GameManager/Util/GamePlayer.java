package src.GameManager.Util;

import src.Engine.Movegen.Board;
import src.Engine.Movegen.Move;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;

import static src.Engine.Types.O_SIDE;
import static src.Engine.Types.X_SIDE;

public class GamePlayer {
    private final Engine devEngine;
    private final Engine baseEngine;
    private final Random random = new Random();
    private final Board board = new Board(10, 5);
    private final Checkpoint checkpoint;
    private final TestStats testStats;

    /**
     * Default constructor
     *
     * @param devEngine  The dev {@link Engine}
     * @param baseEngine The base {@link Engine}
     */
    public GamePlayer(Engine devEngine, Engine baseEngine, Checkpoint checkpoint, TestStats testStats) {
        this.devEngine = devEngine;
        this.baseEngine = baseEngine;
        this.checkpoint = checkpoint;
        this.testStats = testStats;
    }

    /**
     * Plays a game pair between the dev and base engine
     */
    public void playGame(int amountHalfMoves) {
        int[] wdl = new int[3];

        boolean isValidBoard = false;
        String boardNotation = "";

        // Generate a random starting position
        while (!isValidBoard) {
            // Reset the board that in case we are in a second iteration or higher
            board.reset();

            // Make x (as defined above) random moves
            for (int i = 0; i < amountHalfMoves; i++) {

                // Generate all legal moves
                Move[] legalMoves = board.generateLegalMoves();

                // Make a random move on the board
                board.makeMove(legalMoves[random.nextInt(legalMoves.length)]);
            }

            // Check for a noise position, either the game is over or we are near a game over
            if (!board.isGameOver() &&
                    !board.hasWinWithFurtherOffset(1, X_SIDE) &&
                    !board.hasWinWithFurtherOffset(1, O_SIDE)) {
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
            if (board.hasWin(X_SIDE)) {
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
            if (board.hasWin(O_SIDE)) {
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
            if (board.hasWin(X_SIDE)) {
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
            if (board.hasWin(O_SIDE)) {
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
     * Synchronized method that receives updated wdl from different threads
     *
     * @param wdl The wdl to add
     */
    public synchronized void updateScore(int[] wdl) {
        testStats.incrementLosses(wdl[0]);
        testStats.incrementDraws(wdl[1]);
        testStats.incrementWins(wdl[2]);

        checkpoint.save(testStats);
    }

    /**
     * Waits for the engine output and makes the move on the board
     *
     * @param engine The engine which should make the move
     */
    private void makeMove(Engine engine) {
        ArrayList<String> output = engine.getOutput("bestmove");

        String bestMoveLine;

        try {
            // The best move is always in the last line
            bestMoveLine = output.getLast();
        } catch (NoSuchElementException e) {
            // It could be that we got no last element which indicates
            // that the engine process was not opened properly
            throw new RuntimeException("No element was found, probably the engine '" + engine.getName() + "' isn't opened properly!", e);
        }

        // Extract the two coordinates
        String[] numbers = bestMoveLine.substring(bestMoveLine.indexOf("[") + 1, bestMoveLine.indexOf("]")).split(", ");

        // Parse the two coordinates
        int x = Integer.parseInt(numbers[0]);
        int y = Integer.parseInt(numbers[1]);

        // Check if the engine made an illegal move
        if (board.get(x, y, board.getSideToMove())) {
            System.err.println("The engine: " + engine.getName() + " made an illegal move!");
        }

        // Make the move on the board
        board.makeMove(x, y);
    }
}