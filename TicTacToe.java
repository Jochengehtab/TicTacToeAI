import javax.swing.*;

public class TicTacToe  {

    public static void main(String[] args) {
        final int SIZE = 3;

        Board board = new Board(SIZE, 0);
        GUI gui = new GUI(SIZE, board);
        gui.setVisible(true);
        gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
