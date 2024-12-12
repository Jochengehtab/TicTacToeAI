import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UnitTests {
    private final Search search = new Search();
    private Board board;

    @Test
    public void test3x3() {
        board = new Board(3, 0);

        board = execute("121000000o", 99);
        Assertions.assertEquals("121020000x", board.getBoardNotation());

        board = execute("100020001o", 99);
        Assertions.assertTrue("120020001x".equals(board.getBoardNotation()) || "100020021x".equals(board.getBoardNotation()));
    }

    public Board execute(String boardNotation, int depth) {
        board = new Board(3, 0);
        board.setBoardNotation(boardNotation);
        board.makeMove(search.getBestMove(board, depth));
        return board;
    }

    @Test
    public void testOffset3x3() {
        board = new Board(3, 1);
        board.setBoardNotation("000010100o");
        Assertions.assertTrue(board.hasDiagonalWin((byte) 1));
        Assertions.assertFalse(board.hasDiagonalWin((byte) 2));

        board.setBoardNotation("100010000o");
        Assertions.assertTrue(board.hasDiagonalWin((byte) 1));
        Assertions.assertFalse(board.hasDiagonalWin((byte) 2));

        board.setBoardNotation("002020000x");
        Assertions.assertTrue(board.hasDiagonalWin((byte) 2));
        Assertions.assertFalse(board.hasDiagonalWin((byte) 1));
    }

    @Test
    public void testOffset() {
        board = new Board(5, 2);
        board.setBoardNotation("0010000010000210000020000x");
        Assertions.assertTrue(board.hasDiagonalWin((byte) 1));

        board.setBoardNotation("0000000010002010200020000x");
        Assertions.assertTrue(board.hasDiagonalWin((byte) 2));

        board.setBoardNotation("0000000010000010000000000o");
        Assertions.assertFalse(board.hasDiagonalWin((byte) 1));

        board = new Board(5, 1);
        board.setBoardNotation("0000100001000000000100001o");
        Assertions.assertFalse(board.hasRowColumnWin((byte) 1));

        board.setBoardNotation("0000100001000010000100000o");
        Assertions.assertTrue(board.hasRowColumnWin((byte) 1));
    }

    @Test
    public void testBoard() {

        board = new Board(3, 0);

        board.setBoardNotation("111022210o");
        Assertions.assertTrue(board.hasRowColumnWin((byte) 1));

        board.setBoardNotation("211211211o");
        Assertions.assertTrue(board.hasRowColumnWin((byte) 2));

        board.setBoardNotation("121211212o");
        Assertions.assertFalse(board.hasRowColumnWin((byte) 1));

        board.setBoardNotation("121211212o");
        Assertions.assertFalse(board.hasRowColumnWin((byte) 2));

        // Test Diagonal win
        board.setBoardNotation("221212122x");
        Assertions.assertTrue(board.hasDiagonalWin((byte) 1));

        board.setBoardNotation("112121210x");
        Assertions.assertTrue(board.hasDiagonalWin((byte) 2));

        board.setBoardNotation("100010001o");
        Assertions.assertTrue(board.hasDiagonalWin((byte) 1));

        board.setBoardNotation("200020002x");
        Assertions.assertTrue(board.hasDiagonalWin((byte) 2));

        // Check if we got non-false positives
        board.setBoardNotation("122212222x");
        Assertions.assertFalse(board.hasDiagonalWin((byte) 1));

        board.setBoardNotation("211121111o");
        Assertions.assertFalse(board.hasDiagonalWin((byte) 2));

        board = new Board(5, 0);
        board.setBoardNotation("1222201100001000001000001o");
        Assertions.assertTrue(board.hasDiagonalWin((byte) 1));
    }
}
