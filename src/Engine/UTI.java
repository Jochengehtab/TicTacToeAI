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


package src.Engine;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class UTI {

    private final static Search search = new Search();
    private static final Board board = new Board(10, 5);

    public static void main(String[] args) {

        if (args.length != 0 && Objects.equals(args[0], "bench")) {
            search.bench();
            return;
        }

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            String token = scanner.nextLine();
            if (token.equals("stop")) {
                System.exit(0);
                break;
            } else if (token.equals("bench")) {
                search.bench();
            } else if (token.contains("go")) {
                new Thread(() -> handleGo(token)).start();
            } else if (token.contains("position")) {
                board.setBoardNotation(token.substring(token.indexOf("pos") + 8).trim());
            } else if (token.equals("d")) {
                System.out.println(board);
            } else if (token.equals("speedtest")) {
                search.speedtest();
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
            String time = token.substring(token.indexOf("xTime ") + 5).trim();
            time = time.split("\\s+")[0];
            int xTime;
            try {
                xTime = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid number entered as xTime");
            }

            time = token.substring(token.indexOf("oTime ") + 5).trim();
            time = time.split("\\s+")[0];
            int oTime;
            try {
                oTime = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid number entered as oTime");
            }

            time = token.substring(token.indexOf("xInc ") + 5).trim();
            time = time.split("\\s+")[0];
            int xInc;
            try {
                xInc = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid number entered as xInc");
            }

            time = token.substring(token.indexOf("oInc ") + 5).trim();
            time = time.split("\\s+")[0];
            int oInc;
            try {
                oInc = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid number entered as oInc");
            }

            int timeForMove;

            String notation = board.getBoardNotation();

            if (notation.charAt(notation.length() - 1) == 'x') {
                timeForMove = xTime / 20 + (xInc / 2);
            } else {
                timeForMove = oTime / 20 + (oInc / 2);
            }

            System.out.println("bestmove " + Arrays.toString(search.getBestMove(board, (long) timeForMove)));

        } else {
            throw new RuntimeException("Invalid input for the go command: " + token);
        }
    }
}
