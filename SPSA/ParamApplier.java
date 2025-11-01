package src.SPSA;

import src.Engine.Movegen.Board;
import src.Engine.Search;

public @FunctionalInterface
interface ParamApplier {
    void apply(Search s, Board b, Param p);
}