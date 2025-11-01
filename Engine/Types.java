package src.Engine;

public class Types {
    public static final int EVAL_MATE = 30000;
    public static final int EVAL_INFINITE = 31000;
    public static final int EVAL_NONE = 31100;
    public static final int MAX_PLY = 246;
    public static final int EVAL_MATE_IN_MAX_PLY = EVAL_MATE - MAX_PLY;
    public static final byte NO_SIDE = 0;
    public static final byte X_SIDE = 1;
    public static final byte O_SIDE = 2;
}
