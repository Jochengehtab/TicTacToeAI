package src.GameManager.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Checkpoint {

    private final File file;

    public Checkpoint() {
        try {
            // Ensure GameManager/ exists
            Path ENGINE_DIR = Paths.get("GameManager");
            Files.createDirectories(ENGINE_DIR);

            // Initialize File in constructor (as requested)
            file = ENGINE_DIR.resolve("Util/checkpoint.txt").toFile();

            // Create the file if it doesn't exist
            if (!file.exists() && !file.createNewFile()) {
                throw new IOException("Could not create file: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize checkpoint file", e);
        }
    }

    public void save(TestStats testStats) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(testStats.getWins() + ";" + testStats.getDraws() + ";" + testStats.getLosses());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadCheckpoint(TestStats testStats) {
        StringBuilder stringBuilder;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File was not found!", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        String[] games = stringBuilder.toString().trim().split(";");
        testStats.setWins(Integer.parseInt(games[0]));
        testStats.setDraws(Integer.parseInt(games[1]));
        testStats.setLosses(Integer.parseInt(games[2]));
    }

    public void clear() {
        save(new TestStats());
    }
}