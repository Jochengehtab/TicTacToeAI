import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class GameManager {

    private final Random random = new Random();
    Engine firstEngine = new Engine();
    Engine secondEngine = new Engine();

    public static void main(String[] args) {
        GameManager gameManager = new GameManager();
        gameManager.firstEngine.openEngine("base.jar");
        gameManager.secondEngine.openEngine("base.jar");

        System.out.println(Arrays.toString(gameManager.playGame()));

        gameManager.firstEngine.close();
        gameManager.secondEngine.close();
    }


    /**
     * Plays a game pair
     * @return The wdl (devWin / draw/ devLosses)
     */
    private int[] playGame(){
        int[] wdl = new int[3];
        Board board = new Board(10, 7);

        boolean isValidBoard = false;

        String boardNotation = "";

        // Generate a random position
        while (!isValidBoard) {
            for (int i = 0; i < 6; i++) {
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

            if (board.isGameOver())  {
                wdl[0] += 1;
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

            if (board.isGameOver())  {
                wdl[2] += 1;
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

            if (board.isGameOver())  {
                wdl[2] += 1;
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

            if (board.isGameOver())  {
                wdl[0] += 1;
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
        String bestMoveLine = engine.getOutput("bestmove").getLast();
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
}
