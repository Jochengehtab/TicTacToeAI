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

import java.util.Arrays;

public class MoveOrder {
    public int[] scoreMoves(int[][] legalMoves, int[] killer, int[] hashMove, Board board) {

        int[] scores = new int[legalMoves.length];

        for (int i = 0; i < legalMoves.length; i++) {
            scores[i] = 0;

            /*
            LLR        : 2.98
            ELO        : 48.91 +- 16.21
            Games      : [438, 311, 631]
             */
            if (Arrays.equals(legalMoves[i], hashMove) && hashMove != null) {
                scores[i] = 10000000;
                continue;
            }

            if (Arrays.equals(legalMoves[i], killer)) {
                scores[i] += 500000;
            } else {
                int center = board.getSize() / 2;

                int distanceToCenter = Math.abs(legalMoves[i][0] - center) + Math.abs(legalMoves[i][1] - center);

                // 10 is the maximum bonus
                int centralBonus = Math.max(0, 10 - distanceToCenter);

                scores[i] += centralBonus;
            }
        }

        return scores;
    }

    public void sort(int i, int[][] legalMoves, int[] scores) {
        for (int j = i + 1; j < legalMoves.length; j++) {
            if (scores[j] > scores[i]) {
                int[] temp = legalMoves[j];
                legalMoves[j] = legalMoves[i];
                legalMoves[i] = temp;

                int temp2 = scores[j];
                scores[j] = scores[i];
                scores[i] = temp2;
            }
        }
    }
}
