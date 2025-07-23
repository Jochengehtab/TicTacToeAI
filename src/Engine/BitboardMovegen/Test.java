package src.Engine.BitboardMovegen;

public class Test {
    public static void main(String[] args) {
        Board board = new Board(10, 5);
        board.setBoardNotation("0000000000000000200000001000000200000010000100020000000000000001000100000000000000002000000000000000x");
        System.out.println(board.getBitboards());
        Move[] moves = board.generateLegalMoves();
        for (Move m : moves) {
            System.out.println(m);
        }
    }
}
