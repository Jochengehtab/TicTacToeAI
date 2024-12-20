public class TranspositionTable {
    public record Entry(int key, byte type, int staticEval, int score, int[] move, int depth) {
    }

    private int size;
    private int mask;
    private Entry[] data;
    private static final int ENTRY_SIZE = 10;
    public static final byte LOWER_BOUND = 1;
    public static final byte UPPER_BOUND = 2;
    public static final byte EXACT = 3;

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
        return score >= 11000 ? score + ply
                : score <= -11000 ? score - ply
                : score;
    }

    public int scoreFromTT(int score, int ply) {
        return score >= 11000 ? score - ply
                : score <= -11000 ? score + ply
                : score;
    }

    public void resize(int size)
    {
        size *= (int) (1048576.0 / ENTRY_SIZE);
        this.size = Integer.highestOneBit(size);
        this.mask = this.size - 1;
        this.data = new Entry[this.size];
    }
}