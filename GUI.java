import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GUI extends JFrame {

    private final JButton[][] buttons;
    private final Board board;
    private final int size;
    private final JLabel view;

    static class CustomButton extends JButton {

        private final int i;
        private final int j;

        public CustomButton(String text, int i, int j) {
            setText(text);
            this.i = i;
            this.j = j;
        }
    }

    public GUI(int size, Board board) {

        this.buttons = new CustomButton[size][size];
        this.board = board;
        this.size = size;
        this.view = new JLabel("Turn: " + (board.getSideToMove() == 1 ? "X" : "O"));

        Search search = new Search();

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
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                String input = boardNotationInput.getText();
                if (input.isEmpty()) {
                    System.err.println("The provided String is empty!");
                    return;
                }

                setBoardNotation(input);
            }
        });

        JButton exportBoardNotation = getExportBoardNotation(board);

        JCheckBox shouldCopy = new JCheckBox("Copy text", true);

        debugPanel.add(importBoardNotation);
        debugPanel.add(exportBoardNotation);
        debugPanel.add(boardNotationInput);
        debugPanel.add(shouldCopy);

        panel.add(debugPanel);
        panel.add(buttonPanel);
        panel.add(this.view);

        add(panel);
        setSize(size * size, size * size);
        pack();

        //setBoardNotation("000000000x");
        setBoardNotation("000000000x");
        System.out.println(board);


    }

    private static JButton getExportBoardNotation(Board board) {
        JButton exportBoardNotation = new JButton("Export Board");
        exportBoardNotation.setPreferredSize(new Dimension(130, 30));
        exportBoardNotation.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String input = board.getBoardNotation();
                StringSelection stringSelection = new StringSelection(input);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                System.out.println("Exported the board: " + input);
            }
        });
        return exportBoardNotation;
    }

    private void setBoardNotation(String notation) {
        board.setBoardNotation(notation);
        updateBoard(notation);
    }

    private CustomButton createCustomButton(Board board, int i, int j) {
        CustomButton button = new CustomButton(" - ", i, j);
        button.setPreferredSize(new Dimension(80, 80));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                buttons[button.i][button.j].setText(board.getSideToMove() == 1 ? "X" : "O");
                board.makeMove(button.i, button.j);
            }
        });
        return button;
    }

    private void updateBoard(String updateNotation) {
        char[] input = updateNotation.toCharArray();
        int counter = 0;
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

        this.view.setText("Turn: " + (board.getSideToMove() == 1 ? "X" : "O"));
        pack();
    }
}
