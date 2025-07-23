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

import src.Engine.BitboardMovegen.Board;
import src.Engine.BitboardMovegen.Move;

public class MoveOrder {
    public int[] scoreMoves(Move[] legalMoves, Move killer, Move hashMove, Board board, History history) {

        int[] scores = new int[legalMoves.length];

        for (int i = 0; i < legalMoves.length; i++) {
            scores[i] = 0;

            final Move move = legalMoves[i];

            /*
            LLR        : 2.98
            ELO        : 48.91 +- 16.21
            Games      : [438, 311, 631]
             */
            if (!hashMove.equals(Move.NULL_MOVE) && move.equals(hashMove)) {
                scores[i] = 10000000;
                continue;
            }
            if (!killer.equals(Move.NULL_MOVE) && move.equals(killer)) {
                scores[i] += 500000;
            } else {
                int center = board.getSize() / 2;

                int distanceToCenter = Math.abs(move.x() - center) + Math.abs(move.y() - center);

                // 10 is the maximum bonus
                int centralBonus = Math.max(0, 100 - distanceToCenter);

                scores[i] += centralBonus;
            }

            //scores[i] += history.getQuietHistory(board.getSideToMove(), legalMoves[i]);
        }

        return scores;
    }

    public void sort(int i, Move[] legalMoves, int[] scores) {
        for (int j = i + 1; j < legalMoves.length; j++) {
            if (scores[j] > scores[i]) {
                Move temp = legalMoves[j];
                legalMoves[j] = legalMoves[i];
                legalMoves[i] = temp;

                int temp2 = scores[j];
                scores[j] = scores[i];
                scores[i] = temp2;
            }
        }
    }
}
