import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class GUI extends JFrame {

    private final JButton[][] buttons;
    private final Board board;
    private final int size;
    private final JLabel view;
    private final JCheckBox shouldCopy = new JCheckBox("Copy text", true);
    private final Search search = new Search();

    public GUI(int size, Board board) {

        this.buttons = new CustomButton[size][size];
        this.board = board;
        this.size = size;
        this.view = new JLabel("Turn: " + (board.getSideToMove() == 1 ? "X" : "O"));

        JPanel panel = new JPanel();
        JPanel buttonPanel = new JPanel();

        buttonPanel.setLayout(new GridLayout(size, size));

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                CustomButton button = createCustomButton(board, i, j);
                buttonPanel.add(button);
                this.buttons[i][j] = button;
            }
        }

        JPanel debugPanel = new JPanel();
        debugPanel.setLayout(new GridLayout(2, 2));

        JButton importBoardNotation = new JButton("Import Board");
        importBoardNotation.setPreferredSize(new Dimension(130, 30));

        JTextField boardNotationInput = new JTextField();

        importBoardNotation.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                String input = boardNotationInput.getText();
                if (input.isEmpty()) {
                    System.err.println("The provided String is empty!");
                    return;
                }

                setBoardNotation(input);
            }
        });

        JButton resetButton = new JButton("Reset");
        resetButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                board.reset();
                setBoardNotation(board.getBoardNotation());
            }
        });

        // Add everything to the panel
        debugPanel.add(importBoardNotation);
        debugPanel.add(getExportBoardNotation(board));
        debugPanel.add(resetButton);
        debugPanel.add(boardNotationInput);
        debugPanel.add(shouldCopy);

        // Add all features to the panel
        panel.add(debugPanel);
        panel.add(buttonPanel);
        panel.add(this.view);

        add(panel);
        setSize(size * size, size * size);
        pack();

        // Diagonal Position
        //printBestMoveFromPosition("200010000x");

        // Tricky Position
        // printBestMoveFromPosition("121000000o");

        new Thread(this::playAgainstBot).start();
        //new Thread(this::playGame).start();
    }

    @SuppressWarnings("unused")
    private void printBestMoveFromPosition(@SuppressWarnings("SameParameterValue") String notation) {
        setBoardNotation(notation);

        // Get the bestmove
        int[] bestMove = search.getBestMove(board, 2);

        System.out.println("The bestmove is: " + Arrays.toString(bestMove));

        board.makeMove(bestMove);
        setBoardNotation(board.getBoardNotation());
    }

    @SuppressWarnings("unused")
    private void playAgainstBot() {

        // Play until the board is full
        while (!board.isFull() && !board.isGameOver()) {
            // Wait for the player to make an input
            try {
                while (board.getSideToMove() == 1) {
                    //noinspection BusyWait
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Get the bestmove
            int[] bestMove = search.getBestMove(board, 3);

            // Make the move
            board.makeMove(bestMove);

            System.out.println(board);

            // Update the board
            setBoardNotation(board.getBoardNotation());
        }
    }

    /**
     * The bot plays against himself
     */
    @SuppressWarnings("unused")
    private void playGame() {

        // Play until the board is full
        while (!board.isFull()) {
            // Get the bestmove
            int[] bestMove = search.getBestMove(board, 4);

            // Make the move
            board.makeMove(bestMove);

            // Update the board
            setBoardNotation(board.getBoardNotation());
        }
    }

    /**
     * Creates the export button
     *
     * @param board The actual board
     * @return The export button
     */
    private JButton getExportBoardNotation(Board board) {
        JButton exportBoardNotation = new JButton("Export Board");

        // Set the default size
        exportBoardNotation.setPreferredSize(new Dimension(130, 30));

        // Add the mouse listener
        exportBoardNotation.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                // Get the board notation
                String input = board.getBoardNotation();

                // Copy the board notation to the clipboard but only if enabled
                if (shouldCopy.isSelected()) {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(input), null);
                }

                System.out.println("Exported the board: " + input);
            }
        });

        return exportBoardNotation;
    }

    /**
     * Update the GUI based on the board
     *
     * @param notation The new board notation
     */
    private void setBoardNotation(String notation) {
        board.setBoardNotation(notation);
        updateBoard(notation);
    }

    /**
     * Creates a button on the frame
     *
     * @param board The actual board
     * @param i     The first iteration value
     * @param j     The second iteration value
     * @return The newly created Button
     */
    private CustomButton createCustomButton(Board board, int i, int j) {
        CustomButton button = new CustomButton(" - ", i, j);

        // Set the default size
        button.setPreferredSize(new Dimension(80, 80));

        // Add the mouse listener
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                // Update the text
                buttons[button.i][button.j].setText(board.getSideToMove() == 1 ? "X" : "O");

                // Make the move on the actual board
                board.makeMove(button.i, button.j);

                // Update the turn view
                view.setText("Turn: " + (board.getSideToMove() == 1 ? "X" : "O"));
            }
        });
        return button;
    }

    /**
     * Updates the GUI
     *
     * @param updateNotation The Board notation
     */
    private void updateBoard(String updateNotation) {
        // Converts the input to an array
        char[] input = updateNotation.toCharArray();
        int counter = 0;

        // Loop over every char
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                char token = input[counter];
                counter++;
                if (token == '1') {
                    buttons[i][j].setText("X");
                } else if (token == '2') {
                    buttons[i][j].setText("O");
                } else {
                    buttons[i][j].setText("-");
                }
            }
        }
        pack();
    }

    static class CustomButton extends JButton {

        private final int i;
        private final int j;

        public CustomButton(String text, int i, int j) {
            setText(text);
            this.i = i;
            this.j = j;
        }
    }

}
