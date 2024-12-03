import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.security.BasicPermission;

public class Tests {
    private final Search search = new Search();
    @Test
    public void test3x3() {

        Board board = execute("121000000o", 6);
        Assertions.assertEquals("121020000x", board.getBoardNotation());

        board = execute("100020001o", 6);
        Assertions.assertTrue("120020001x".equals(board.getBoardNotation()) || "100020021x".equals(board.getBoardNotation()));

        board = execute("100010000o", 3);
        Assertions.assertEquals("100010002x", board.getBoardNotation());
    }

    public Board execute(String boardNotation, int depth) {
        Board board = new Board(3, 0);
        board = new Board(3, 0);
        board.setBoardNotation(boardNotation);
        board.makeMove(search.getBestMove(board, depth));
        return board;
    }
}
