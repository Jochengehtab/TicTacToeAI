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

public class MoveOrder {
    public int[] scoreMoves(int[][] legalMoves, int[] killer/*, int[] hashedMove*/) {

        int[] scores = new int[legalMoves.length];

        for (int i = 0; i < legalMoves.length; i++) {
            /*
            if (hashedMove != null) {
                if (Arrays.equals(legalMoves[i], hashedMove)) {
                    scores[i] = 50000000;
                }
            }

             */
            if (Arrays.equals(legalMoves[i], killer)) {
                scores[i] = 500000;
            } else {
                scores[i] = 0;
            }
        }

        return scores;
    }

    public int[] getSortedMove(int[][] legalMoves, int[] scores, int i) {
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

        return legalMoves[i];
    }
}
