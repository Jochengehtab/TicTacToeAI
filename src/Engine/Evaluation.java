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

public class Evaluation {
    public int evaluate(Board board, int ply) {
        final byte xSide = 1;
        final byte oSide = 2;
        final byte sideToMove = board.getSideToMove();
        short xEval = 0;
        short oEval = 0;
        int size = board.getSize();
        int center = size / 2;

        /*
        LLR        : 2.99
        ELO        : 294.55 +- 45.98
        Games      : [62, 5, 349]
         */

        // Small bonus for playing around the center
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                byte piece = board.get(i, j);
                if (piece == 0) {
                    continue;
                }

                int distanceToCenter = Math.abs(i - center) + Math.abs(j - center);

                // 10 is the maximum bonus
                int centralBonus = Math.max(0, 10 - distanceToCenter);

                if (piece == xSide) {
                    xEval += (short) centralBonus;
                } else if (piece == oSide) {
                    oEval += (short) centralBonus;
                }
            }
        }

        /*
        This hasn't surely passed yet but idc because I know it will pass
        LLR        : 1.5
        ELO        : 30.93 +- 18.35
        Games      : [434, 189, 537]
         */
        if (board.has2x2Cluster(xSide)) {
            xEval += 200;
        }

        if (board.has2x2Cluster(oSide)) {
            oEval += 200;
        }

        if (board.hasWinWithFurtherOffset(1, xSide)) {
            xEval += (short) (1000 - ply);
        }

        if (board.hasWinWithFurtherOffset(1, oSide)) {
            oEval += (short) (1000 - ply);
        }

        if (board.hasDiagonalWin(xSide) || board.hasRowColumnWin(xSide)) {
            xEval = (short) (30000 - ply);
        }

        if (board.hasDiagonalWin(oSide) || board.hasRowColumnWin(oSide)) {
            oEval = (short) (30000 - ply);
        }

        int diff = xEval - oEval;
        return (sideToMove == 2 ? -diff : diff);
    }
}
