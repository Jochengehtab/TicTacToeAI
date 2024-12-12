import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class UTI {

    private final static Search search = new Search();
    private static final Board board = new Board(10, 7);

    public static void main(String[] args) {
        if (args.length != 0 && Objects.equals(args[0], "bench"))  {
            new Search().bench();
            return;
        }

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            String token = scanner.nextLine();

            if (token.equals("token")) {
                break;
            } else if (token.equals("bench")) {
                search.bench();
            } else if (token.contains("go")) {
                handleGo(token);
            } else if (token.contains("position")) {
                board.setBoardNotation(token.substring(token.indexOf("pos") + 8).trim());
            } else if (token.equals("d")) {
                System.out.println(board);
            }
        }
    }

    private static void handleGo(String token) {
        if (token.contains("depth")) {
            String depthValueStr = token.substring(token.indexOf("depth") + 5).trim();

            try {
                int depth = Integer.parseInt(depthValueStr);
                System.out.println("bestmove " + Arrays.toString(search.getBestMove(board, depth)));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid number entered as depth");
            }
        } else if (token.contains("xTime")) {

        }
    }
}
