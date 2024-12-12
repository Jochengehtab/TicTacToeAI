import javax.swing.*;

public class TicTacToe {

    public static void main(String[] args) {
        final int SIZE = 10;
        final int OFFSET = 7;

        Board board = new Board(SIZE, OFFSET);
        GUI gui = new GUI(SIZE, board);
        gui.setVisible(true);
        gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
