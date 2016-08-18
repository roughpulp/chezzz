package rpulp.chezzz.board;

public final class MoveNotationCodec {
    public static void encode(final Move move, StringBuilder strb) {
        final MoveResult move_res = new MoveResult();
        encode(move.src_idx_ % 8, move.src_idx_ / 8,
                move.dst_idx_ % 8, move.dst_idx_ / 8,
                move_res,
                strb);
    }

    public static void encode(int x0, int y0,
                              int x1, int y1,
                              MoveResult move_res,
                              StringBuilder strb) {
        strb.append(COORD_X[x0]);
        strb.append(COORD_Y[y0]);
        if (move_res.capture_) {
            strb.append('x');
        } else {
            strb.append('-');
        }
        strb.append(COORD_X[x1]);
        strb.append(COORD_Y[y1]);
        switch (move_res.check_) {
            case NONE:
                break;

            case CHECK:
                strb.append('+');
                break;

            case CHECKMATE:
                strb.append('#');
                break;
        }
    }

    private static final char[] COORD_X = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
    private static final char[] COORD_Y = {'1', '2', '3', '4', '5', '6', '7', '8'};
}
