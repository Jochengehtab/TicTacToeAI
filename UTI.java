import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class UTI {

    private final static Search search = new Search();
    private static final Board board = new Board(10, 7);

    private static int xTime, xInc, oTime, oInc;

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

    private static void handleGo(String t) {
        String token = t;
        if (token.contains("depth")) {
            String depthValueStr = token.substring(token.indexOf("depth") + 5).trim();

            try {
                int depth = Integer.parseInt(depthValueStr);
                System.out.println("bestmove " + Arrays.toString(search.getBestMove(board, depth)));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid number entered as depth");
            }
        } else if (token.contains("xTime")) {
            String time = token.substring(token.indexOf("xTime ") + 5).trim();
            time = time.split("\\s+")[0];
            try {
                xTime = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid number entered as xTime");
            }

            time = token.substring(token.indexOf("oTime ") + 5).trim();
            time = time.split("\\s+")[0];
            try {
                oTime = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid number entered as oTime");
            }

            time = token.substring(token.indexOf("xInc ") + 5).trim();
            time = time.split("\\s+")[0];
            try {
                xInc = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid number entered as xInc");
            }

            time = token.substring(token.indexOf("oInc ") + 5).trim();
            time = time.split("\\s+")[0];
            try {
                oInc = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid number entered as oInc");
            }
        } else {
            throw new RuntimeException("Invalid input for the go command: " + token);
        }
    }
}
