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


package src.GUI;

import src.Engine.Movegen.Board;
import src.Engine.Movegen.Move;
import src.Engine.Search;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GUI extends JFrame {

    private final int size;
    private final Board board;
    private final Search search;
    private final JButton[][] buttons;
    private final JLabel view;
    private final JCheckBox shouldCopy = new JCheckBox("Copy text", true);
    private final JCheckBox isFirstTurnX = new JCheckBox("Start first", true);
    private final JCheckBox debugBoardMode = new JCheckBox("Debug board", false);

    public GUI(int size, Board board) {
        this.buttons = new CustomButton[size][size];
        this.board = board;
        this.size = size;
        this.view = new JLabel("Turn: " + (board.getSideToMove() == 1 ? "X" : "O"));
        this.search = new Search();

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
        debugPanel.setLayout(new GridLayout(3, 3));

        JButton importBoardNotation = new JButton("Import Board");
        importBoardNotation.setPreferredSize(new Dimension(130, 30));

        JTextField boardNotationInput = new JTextField();

        importBoardNotation.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                String input = boardNotationInput.getText();
                boardNotationInput.setText(null);
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

        JButton botGame = new JButton("Bot game");
        botGame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                new Thread(() -> playGame()).start();
            }
        });

        JButton playAgainstBot = new JButton("Play against Bot");
        playAgainstBot.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                new Thread(() -> playAgainstBot()).start();
                System.out.println("Now playing against the bot");
            }
        });

        // Add everything to the panel
        debugPanel.add(importBoardNotation);
        debugPanel.add(getExportBoardNotation(board));
        debugPanel.add(resetButton);
        debugPanel.add(boardNotationInput);
        debugPanel.add(botGame);
        debugPanel.add(playAgainstBot);
        debugPanel.add(shouldCopy);
        debugPanel.add(isFirstTurnX);
        debugPanel.add(debugBoardMode);

        // Add all features to the panel
        panel.add(debugPanel);
        panel.add(buttonPanel);
        panel.add(this.view);

        add(panel);
        setSize(size * size, size * size);
        pack();
    }

    private void playAgainstBot() {

        // Play until the board is full
        while (!board.isFull() && !board.isGameOver()) {

            if (isFirstTurnX.isSelected()) {
                // Wait for the player to make an input
                try {
                    while (board.getSideToMove() == 1) {
                        //noinspection BusyWait
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // Get the bestmove
            Move bestMove = search.getBestMove(board, (long) 10000, false);

            // Make the move
            board.makeMove(bestMove);

            // Update the board
            setBoardNotation(board.getBoardNotation());

            // Update the turn
            updateTurn();

            if (!isFirstTurnX.isSelected()) {
                // Wait for the player to make an input
                try {
                    while (board.getSideToMove() == 2) {
                        //noinspection BusyWait
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * The bot plays against itself
     */
    private void playGame() {
        System.out.println("Playing a bot game");

        // Play until the board is full
        while (!board.isFull() && !board.isGameOver()) {
            // Get the bestmove
            Move bestMove = search.getBestMove(board, (long) 10000, false);

            // Make the move
            board.makeMove(bestMove);

            // Update the board
            setBoardNotation(board.getBoardNotation());

            // Update the turn
            updateTurn();
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

                JButton customButton = buttons[button.getI()][button.getJ()];

                // Update the text
                customButton.setText(board.getSideToMove() == 1 ? "X" : "O");

                // Set the background color
                if (board.getSideToMove() == 1) {
                    customButton.setBackground(Color.RED);
                } else {
                    customButton.setBackground(Color.GREEN);
                }

                // Make the move on the actual board
                board.makeMove(button.getI(), button.getJ());

                // Update the turn view
                view.setText("Turn: " + (board.getSideToMove() == 1 ? "X" : "O"));

                // If the debug mode is activated, we disable the button
                if (!debugBoardMode.isSelected()) {
                    customButton.removeMouseListener(this);
                }
            }
        });
        return button;
    }

    /**
     * Updates the turn shown on the side
     */
    private void updateTurn() {
        view.setText("Turn: " + (board.getSideToMove() == 1 ? "X" : "O"));
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
                    buttons[i][j].setBackground(Color.RED);
                } else if (token == '2') {
                    buttons[i][j].setText("O");
                    buttons[i][j].setBackground(Color.GREEN);
                } else {
                    buttons[i][j].setText("-");
                    buttons[i][j].setBackground(null);
                }
            }
        }
        pack();
    }

    public static void main(String[] args) {
        final int SIZE = 9;
        final int OFFSET = 4;

        Board board = new Board(SIZE, OFFSET);

        GUI gui = new GUI(SIZE, board);
        gui.setVisible(true);
        gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
