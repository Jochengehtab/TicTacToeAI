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

public class TranspositionTable {
    public static final byte LOWER_BOUND = 1;
    public static final byte UPPER_BOUND = 2;
    public static final byte EXACT = 3;
    private static final int ENTRY_SIZE = 10;
    private int size;
    private int mask;
    private Entry[] data;
    public TranspositionTable(int size) {
        size *= (int) (1048576.0 / ENTRY_SIZE);
        this.size = Integer.highestOneBit(size);
        this.mask = this.size - 1;
        this.data = new Entry[this.size];
    }

    public Entry probe(int key) {
        int index = key & mask;
        Entry entry = data[index];
        if (entry != null && entry.key() == key) {
            return entry;
        }
        return null;
    }

    public void write(int key, byte type, int staticEval, int score, int[] move, int depth) {
        int index = key & mask;
        data[index] = new Entry(key, type, staticEval, score, move, depth);
    }

    public int scoreToTT(int score, int ply) {
        return score >= 30000 ? score + ply
                : score <= -30000 ? score - ply
                : score;
    }

    public int scoreFromTT(int score, int ply) {
        return score >= 30000 ? score - ply
                : score <= -30000 ? score + ply
                : score;
    }

    public void resize(int size) {
        size *= (int) (1048576.0 / ENTRY_SIZE);
        this.size = Integer.highestOneBit(size);
        this.mask = this.size - 1;
        this.data = new Entry[this.size];
    }

    public record Entry(int key, byte type, int staticEval, int score, int[] move, int depth) {
    }
}