/*
  This file is part of the TicTacToe AI written by Jochengehtab

  Copyright (C) 2024-2025 Jochengehtab

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package src.Engine;

public class Evaluation {
    public int evaluate(Board board, int ply) {
        final byte sideToMove = board.getSideToMove();

        int xEval = getEvalForSide(board, board.X_SIDE, ply);
        int oEval = getEvalForSide(board, board.O_SIDE, ply);

        int diff = xEval - oEval;
        int perspective = sideToMove == board.O_SIDE ? -1 : 1;

        return perspective * diff;
    }

    private int getEvalForSide(Board board, byte side, int ply) {
        int eval = 0;

        // Stuff for the distance calculation
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
                byte square = board.get(i, j);

                // If the square is empty, we continue with the loop
                if (square == board.NO_SIDE) {
                    continue;
                }

                int distanceToCenter = Math.abs(i - center) + Math.abs(j - center);

                // 10 is the maximum bonus
                int centralBonus = Math.max(0, 10 - distanceToCenter);

                if (square == side) {
                    eval += centralBonus;
                }
            }
        }

        if (board.hasWinWithFurtherOffset(1, side)) {
            eval += 1000 - ply;
        }

        return eval;
    }
}
