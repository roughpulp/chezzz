package rpulp.chezzz.board;

public final class MoveResult {
    public enum Check {
        NONE,
        CHECK,
        CHECKMATE
    }

    public MoveResult() {
    }

    public Check check_ = Check.NONE;
    public boolean capture_ = false;
}
