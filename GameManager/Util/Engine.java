package src.GameManager.Util;

import java.io.*;
import java.util.ArrayList;

public class Engine {
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