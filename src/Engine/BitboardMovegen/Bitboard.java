package src.Engine.BitboardMovegen;

public class Bitboard {
    private long bits = 0;

    public void set(int index) {
        bits |= (1L << index);
    }

    public void clear(int index) {
        bits &= ~(1L << index);
    }

    public boolean isSet(int index) {
        return ((1L << index) & bits) != 0;
    }

    public long getBits() {
        return bits;
    }
}
