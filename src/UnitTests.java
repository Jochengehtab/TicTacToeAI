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


package src;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import src.Engine.Board;
import src.Engine.Search;
import src.Engine.TranspositionTable;

import java.util.Arrays;

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

        board.setBoardNotation("2121212121211222112000210o");
        Assertions.assertTrue(board.hasDiagonalWin((byte) 1) && !board.hasDiagonalWin((byte) 2));

        board.setBoardNotation("2121212121211222112000220x");
        System.out.println(Arrays.deepToString(board.generateLegalMoves()));

        board.setBoardNotation("2202211101000000000000000o");
        Assertions.assertFalse(board.hasDiagonalWin((byte) 1) && board.hasDiagonalWin((byte) 2));
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

    @Test
    public void testTranspositionTable() {
        TranspositionTable transpositionTable = new TranspositionTable(8);
        transpositionTable.write(200, (byte) 2, (short) 9, 20000, new int[]{5, 5}, (short) 9);
        TranspositionTable.Entry probed = transpositionTable.probe(200);
        Assertions.assertEquals(9, probed.staticEval());
        Assertions.assertEquals(20000, probed.score());
        Assertions.assertArrayEquals(probed.move(), new int[]{5, 5});

        board = new Board(10, 6);
        board.setBoardNotation("2000000001000000001010000000000000002000002000000000200000000001000020000001000000000000002000000001x");
        Assertions.assertEquals(1592120758, board.getKey());
        Assertions.assertNull(transpositionTable.probe(2));
    }
}
