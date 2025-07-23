package src.Engine.BitboardMovegen;
public class Move
{
    public static final Move NULL_MOVE = new Move((short) 0b0000000100000001);

    // High 8 bits are the x coordinate
    // Low 8 bits are the y coordinate
    private short moveData = 0;

    public Move() {
        moveData = 0;
    }

    public Move(int x, int y) {
        setX(x);
        setY(y);
    }

    public Move(short newMoveData) {
        moveData = newMoveData;
    }

    public int x()  {
        return moveData >> 8;
    }

    public int y()  {
        return moveData & 0xFF;
    }

    public void setX(int x) {
        assert(x < 0xFF);

        // Clear the high 7 bits
        moveData &= 0x00FF;

        // Since our x are the high 8 bits we need to offset by 8
        moveData |= (short) (x << 8);
    }

    public void setY(int y) {
        assert(y < 0xFF);

        // Clear the low 8 bits
        moveData &= (short) 0xFF00;

        // Store the data
        moveData |= (short) y;
    }

    public static int getX(short moveData) {
        return moveData >> 8;
    }

    public static int getY(short moveData) {
        return moveData & 0xFF;
    }

    public short getMoveData() {
        return moveData;
    }

    public boolean equals(Move other) {
        return (this.x() == other.x()) && (this.y() == other.y());
    }

    @Override
    public String toString() {
        return "[" + x() + ", " + y() + "]";
    }
}
