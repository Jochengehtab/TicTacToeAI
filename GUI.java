import javax.swing.*;
import java.awt.*;

public class GUI extends JFrame {

    private final JButton[] buttons;
    private final Board board;

    public GUI(int size, Board board) {
        this.buttons = new JButton[size * size];
        this.board = board;

        JPanel panel = new JPanel();

        JPanel buttonPanel = new JPanel();

        buttonPanel.setLayout(new GridLayout(size, size));

        for (int i = 0; i < size * size; i++) {
            JButton button = new JButton(" - ");
            button.setPreferredSize(new Dimension(80, 80));
            buttonPanel.add(button);
            this.buttons[i] = button;
        }

        JPanel debugPanel = new JPanel();
        debugPanel.setLayout(new GridLayout(1, 1));

        JButton importBoardNotation = new JButton("Import Board");
        importBoardNotation.setPreferredSize(new Dimension(130, 30));

        JButton exportBoardNotation = new JButton("Export Board");
        exportBoardNotation.setPreferredSize(new Dimension(130, 30));

        debugPanel.add(importBoardNotation);
        debugPanel.add(exportBoardNotation);

        panel.add(debugPanel);
        panel.add(buttonPanel);

        add(panel);
        setSize(size * size, size * size);
        pack();
    }

    private void test() {
        System.out.println(board.getBoardNotation());
        //board.setBoardNotation("00000000000000000000000000");
        board.setBoardNotation("00000010000010000010000000");
        //board.setBoardNotation("1000100001");
        System.out.println(board);
        System.out.println(board.isDraw());
        System.out.println(board.hasRowWing((byte) 1));
        System.out.println(board.hasColumWin((byte) 1));
        System.out.println(board.hasDiagonalWin((byte) 1));
    }
}
