package rpulp.chezzz.board;

import java.util.Arrays;

public final class MoveList {
    // encoded move :  0000 0000 0000 0000 0000 0000 0000 0000
    //                 |_________________|  |_| |_____||_____|
    //                          |            |     |      |
    //                          |            |     |     src_idx [0 63]
    //                          |            |     dst_idx [0 63]
    //                          |          type_ : normal, O-O, O-O-O, enpassant x-1, enpassant x+1, pawn promotion [0 5]
    //                        score

    public static void decode_move(int encoded, final Move move) {
        move.src_idx_ = encoded & 0x3f;
        move.dst_idx_ = (encoded >>> 6) & 0x3f;
        move.type_ = (encoded >>> 12) & 7;
    }

    public static int encode_move_normal(int src, int dst) {
        int encoded = src;
        encoded |= dst << 6;
        return encoded;
    }

    public static int encode_move_promoted_pawn(int src, int dst) {
        int encoded = encode_move_normal(src, dst);
        encoded |= Move.MOVE_TYPE_PAWN_PROMOTION << 12;
        return encoded;
    }

    public static int encode_move_O_O(boolean white_move) {
        int encoded = white_move ? encode_move_normal(4 + (0 * 8), 6 + (0 * 8)) :
                encode_move_normal(4 + (7 * 8), 6 + (7 * 8));
        encoded |= Move.MOVE_TYPE_O_O << 12;
        return encoded;
    }

    public static int encode_move_O_O_O(boolean white_move) {
        int encoded = white_move ? encode_move_normal(4 + (0 * 8), 2 + (0 * 8)) :
                encode_move_normal(4 + (7 * 8), 2 + (7 * 8));
        encoded |= Move.MOVE_TYPE_O_O_O << 12;
        return encoded;
    }

    public MoveList(final int initial_capacity) {
        moves_ = new int[initial_capacity];
    }

    public void clear() {
        size_ = 0;
    }

    public void truncate(int size) {
        size_ = size;
    }

    public void add(final int encoded_move) {
        if (moves_.length == size_) {
            final int[] old_moves = moves_;
            moves_ = new int[((size_ * 3) / 2) + 1];
            System.arraycopy(old_moves, 0, moves_, 0, size_);
        }
        moves_[size_] = encoded_move;
        ++size_;
    }

    public int find_move(final int idx0, final int idx1) {
        final int pattern = idx0 | (idx1 << 6);
        for (int i = 0; i != size_; ++i) {
            final int move = moves_[i];
            if (pattern == (move & 0x0fff)) {
                return move;
            }
        }
        return 0;
    }

    public void decode_move_at(int idx, final Move move) {
        decode_move(moves_[idx], move);
    }

    public void set_score_at(int idx, int score) {
        moves_[idx] |= score << 16;
    }

    public void sort(int beg, int end) {
        Arrays.sort(moves_, beg, end);
    }

    public void print(final StringBuilder strb) {
        print(strb, 0, size_);
    }

    public void print(final StringBuilder strb, int beg, int end) {
        final MoveResult move_res = new MoveResult();
        final Move move = new Move();
        for (int i = beg; i != end; ++i) {
            decode_move(moves_[i], move);
            if (i != 0) {
                strb.append(", ");
            }
            MoveNotationCodec.encode(move, strb);
        }
    }

    public int[] moves_;
    public int size_ = 0;
}
