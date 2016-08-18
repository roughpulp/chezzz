package rpulp.chezzz.board;

public final class Move {
    public static final int MOVE_TYPE_NORMAL = 0;
    public static final int MOVE_TYPE_O_O = 1;
    public static final int MOVE_TYPE_O_O_O = 2;
    public static final int MOVE_TYPE_ENPASSANT_X_MINUS_1 = 3;
    public static final int MOVE_TYPE_ENPASSANT_X_PLUS_1 = 4;
    public static final int MOVE_TYPE_PAWN_PROMOTION = 5;

    public Move() {
    }

    public Move(long hash) {
        hash_ = hash;
    }

    public Move(final Move move) {
        copy_from(move);
    }

    public void copy_from(final Move move) {
        type_ = move.type_;
        src_idx_ = move.src_idx_;
        dst_idx_ = move.dst_idx_;
        captured_piece_ = move.captured_piece_;
        flags_before_ = move.flags_before_;
        hash_ = move.hash_;
    }

    public int type_;
    public int src_idx_;
    public int dst_idx_;
    public int captured_piece_;
    public int flags_before_;
    public long hash_;
}
