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

public class TranspositionTable {
    public static final byte LOWER_BOUND = 1;
    public static final byte UPPER_BOUND = 2;
    public static final byte EXACT = 3;

    /*
        int key: 4 bytes
        byte type: 1 byte
        short staticEval: 2 bytes
        int score: 4 bytes
        int[] move: (4 * 2) 8 bytes
        short depth: 2 bytes
     */
    private static final int ENTRY_SIZE = 21;
    private int size;
    private int mask;

    private Entry[] data;

    public TranspositionTable(int size) {
        size *= (int) (1_048_576.0 / ENTRY_SIZE);
        this.size = Integer.highestOneBit(size);
        this.mask = this.size - 1;
        this.data = new Entry[this.size];
    }

    /**
     * Probes the transposition table for an Entry
     * @param key The key of the current position
     * @return If a {@link Entry} was found it returns an Entry otherwise
     */
    public Entry probe(int key) {
        int index = key & mask;
        return data[index];
    }

    /**
     * Writes an {@link Entry} to the transposition table
     * @param key The key of the current position
     * @param type The type. Either LOWER_BOUND, UPPER_BOUND or EXACT
     * @param staticEval The static Eval of the position
     * @param score The current correct adjusted score
     * @param move The best move
     * @param depth The current depth
     */
    public void write(int key, byte type, short staticEval, int score, int[] move, short depth) {
        int index = key & mask;
        data[index] = new Entry(key, type, staticEval, score, move, depth);
    }

    /**
     * @param score The score that should be stored
     * @param ply The current ply
     * @return The corrected score for the transposition table
     */
    public int scoreToTT(int score, int ply) {
        return score >= 30000 ? score + ply
                : score <= -30000 ? score - ply
                : score;
    }

    /**
     * @param score The score that should be stored
     * @param ply The current ply
     * @return The corrected score from the transposition table
     */
    public int scoreFromTT(int score, int ply) {
        return score >= 30000 ? score - ply
                : score <= -30000 ? score + ply
                : score;
    }

    /**
     * Resizes the transposition table
     * @param size The new size in MB
     */
    public void resize(int size) {
        size *= (int) (1_048_576.0 / ENTRY_SIZE);
        this.size = Integer.highestOneBit(size);
        this.mask = this.size - 1;
        this.data = new Entry[this.size];
    }

    /**
     * Estimates how full our hash is
     * @return The estimated hash full
     */
    public int hashfull() {
        int filled = 0;
        int minimum_hash = 1048576 / ENTRY_SIZE;

        for (int i = 0; i < minimum_hash; i++)
        {
            if (data[i] != null)
            {
                filled++;
            }
        }

        return filled * 1000 / minimum_hash;
    }


    /**
     * Record for a transportation table Entry
     * @param key The key of the current position
     * @param type The type. Either LOWER_BOUND, UPPER_BOUND or EXACT
     * @param staticEval The static Eval of the position
     * @param score The current correct adjusted score
     * @param move The best move
     * @param depth The current depth
     */
    public record Entry(int key, byte type, short staticEval, int score, int[] move, short depth) {}
}