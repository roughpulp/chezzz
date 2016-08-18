package rpulp.chezzz.board;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;

public final class Board {
    public static final int PIECE_NONE = 0;
    public static final int PIECE_PAWN = 1;
    public static final int PIECE_KNIGHT = 2;
    public static final int PIECE_BISHOP = 3;
    public static final int PIECE_ROOK = 4;
    public static final int PIECE_QUEEN = 5;
    public static final int PIECE_KING = 6;
    public static final int NB_PIECES = 6;

    public static final int WHITES = 0;
    public static final int BLACKS = 7;

    // castling flags
    private static final int FLAG_WHITE_CAN_O_O = 1;    // 0000 0001
    private static final int FLAG_WHITE_CAN_O_O_O = 2;    // 0000 0010
    private static final int FLAG_BLACK_CAN_O_O = 4;    // 0000 0100
    private static final int FLAG_BLACK_CAN_O_O_O = 8;    // 0000 1000

    public static String get_piece_name(int piece_type) {
        switch (piece_type) {
            case PIECE_PAWN:
                return "pawn";

            case PIECE_KNIGHT:
                return "knight";

            case PIECE_BISHOP:
                return "bishop";

            case PIECE_ROOK:
                return "rook";

            case PIECE_QUEEN:
                return "queen";

            case PIECE_KING:
                return "king";

            case PIECE_NONE:
                return "empty";
        }
        throw new IllegalStateException();
    }

    public static int coord_2_index(int x, int y) {
        return (y << 3) + x;
    }

    //-------------------

    public int flags_;
    public final int[] board_ = new int[8 * 8];
    public final short[] material_scores_ = new short[2 * 7];


    public Board() {
        reset();
    }

    public void copy_from(final Board master) {
        flags_ = master.flags_;
        System.arraycopy(master.board_, 0, board_, 0, board_.length);
        System.arraycopy(master.material_scores_, 0, material_scores_, 0, material_scores_.length);
    }

    public void reset() {
        flags_ = FLAG_WHITE_CAN_O_O | FLAG_WHITE_CAN_O_O_O | FLAG_BLACK_CAN_O_O | FLAG_BLACK_CAN_O_O_O;

        board_[coord_2_index(0, 0)] = PIECE_ROOK;
        board_[coord_2_index(1, 0)] = PIECE_KNIGHT;
        board_[coord_2_index(2, 0)] = PIECE_BISHOP;
        board_[coord_2_index(3, 0)] = PIECE_QUEEN;
        board_[coord_2_index(4, 0)] = PIECE_KING;
        board_[coord_2_index(5, 0)] = PIECE_BISHOP;
        board_[coord_2_index(6, 0)] = PIECE_KNIGHT;
        board_[coord_2_index(7, 0)] = PIECE_ROOK;
        {
            int idx = coord_2_index(0, 1);
            Arrays.fill(board_, idx, idx + 8, PIECE_PAWN);
        }

        {
            int idx = coord_2_index(0, 2);
            Arrays.fill(board_, idx, idx + (4 * 8), PIECE_NONE);
        }

        {
            int idx = coord_2_index(0, 6);
            Arrays.fill(board_, idx, idx + 8, -PIECE_PAWN);
        }

        board_[coord_2_index(0, 7)] = -PIECE_ROOK;
        board_[coord_2_index(1, 7)] = -PIECE_KNIGHT;
        board_[coord_2_index(2, 7)] = -PIECE_BISHOP;
        board_[coord_2_index(3, 7)] = -PIECE_QUEEN;
        board_[coord_2_index(4, 7)] = -PIECE_KING;
        board_[coord_2_index(5, 7)] = -PIECE_BISHOP;
        board_[coord_2_index(6, 7)] = -PIECE_KNIGHT;
        board_[coord_2_index(7, 7)] = -PIECE_ROOK;

        material_scores_[WHITES + PIECE_PAWN] = 8;
        material_scores_[WHITES + PIECE_KNIGHT] = 2;
        material_scores_[WHITES + PIECE_BISHOP] = 2;
        material_scores_[WHITES + PIECE_ROOK] = 2;
        material_scores_[WHITES + PIECE_QUEEN] = 1;
        material_scores_[WHITES + PIECE_KING] = 1;

        material_scores_[BLACKS + PIECE_PAWN] = 8;
        material_scores_[BLACKS + PIECE_KNIGHT] = 2;
        material_scores_[BLACKS + PIECE_BISHOP] = 2;
        material_scores_[BLACKS + PIECE_ROOK] = 2;
        material_scores_[BLACKS + PIECE_QUEEN] = 1;
        material_scores_[BLACKS + PIECE_KING] = 1;
    }

    public BoardBuilder get_builder() {
        return new BoardBuilder(this);
    }

    public int get_piece(int x, int y) {
        return board_[x + (y << 3)];
    }

    public int find_colored_piece_index(int colored_piece) {
        for (int i = 0; i != 64; ++i) {
            if (colored_piece == board_[i]) {
                return i;
            }
        }
        return -1;
    }

    public void do_move(final Move move) {
        switch (move.type_) {
            case Move.MOVE_TYPE_NORMAL:
                do_move_normal(move);
                break;

            case Move.MOVE_TYPE_O_O:
                do_move_castle(move, 1, -1);
                break;

            case Move.MOVE_TYPE_O_O_O:
                do_move_castle(move, -2, 1);
                break;

            case Move.MOVE_TYPE_ENPASSANT_X_MINUS_1:
                throw new UnsupportedOperationException();

            case Move.MOVE_TYPE_ENPASSANT_X_PLUS_1:
                throw new UnsupportedOperationException();

            case Move.MOVE_TYPE_PAWN_PROMOTION:
                do_move_pawn_promotion(move);
                break;

            default:
                throw new IllegalStateException();
        }
    }

    public void undo_move(final Move move) {
        switch (move.type_) {
            case Move.MOVE_TYPE_NORMAL:
                undo_move_normal(move);
                break;

            case Move.MOVE_TYPE_O_O:
                undo_move_castle(move, 1, -1);
                break;

            case Move.MOVE_TYPE_O_O_O:
                undo_move_castle(move, -2, 1);
                break;

            case Move.MOVE_TYPE_ENPASSANT_X_MINUS_1:
                throw new UnsupportedOperationException();

            case Move.MOVE_TYPE_ENPASSANT_X_PLUS_1:
                throw new UnsupportedOperationException();

            case Move.MOVE_TYPE_PAWN_PROMOTION:
                undo_move_pawn_promotion(move);
                break;

            default:
                throw new IllegalStateException();
        }
    }

    /*
     * Only updates move.hash_ and move.captured_piece_, doesn't update the board
     */
    public void do_move_hash(final Move move) {
        switch (move.type_) {
            case Move.MOVE_TYPE_NORMAL:
                do_move_hash_normal(move);
                break;

            case Move.MOVE_TYPE_O_O:
                throw new UnsupportedOperationException();

            case Move.MOVE_TYPE_O_O_O:
                throw new UnsupportedOperationException();

            case Move.MOVE_TYPE_ENPASSANT_X_MINUS_1:
                throw new UnsupportedOperationException();

            case Move.MOVE_TYPE_ENPASSANT_X_PLUS_1:
                throw new UnsupportedOperationException();

            case Move.MOVE_TYPE_PAWN_PROMOTION:
                do_move_hash_pawn_promotion(move);
                break;

            default:
                throw new IllegalStateException();
        }
    }

    public void undo_move_hash(final Move move) {
        switch (move.type_) {
            case Move.MOVE_TYPE_NORMAL:
                undo_move_hash_normal(move);
                break;

            case Move.MOVE_TYPE_O_O:
                throw new UnsupportedOperationException();

            case Move.MOVE_TYPE_O_O_O:
                throw new UnsupportedOperationException();

            case Move.MOVE_TYPE_ENPASSANT_X_MINUS_1:
                throw new UnsupportedOperationException();

            case Move.MOVE_TYPE_ENPASSANT_X_PLUS_1:
                throw new UnsupportedOperationException();

            case Move.MOVE_TYPE_PAWN_PROMOTION:
                undo_move_hash_pawn_promotion(move);
                break;

            default:
                throw new IllegalStateException();
        }
    }

    private void do_move_normal(final Move move) {
        final int moving_piece = board_[move.src_idx_];
        move.captured_piece_ = board_[move.dst_idx_];
        board_[move.dst_idx_] = moving_piece;
        board_[move.src_idx_] = PIECE_NONE;

        if (move.captured_piece_ != 0) {
            final int piece_idx;
            if (move.captured_piece_ > 0) {
                piece_idx = WHITES + move.captured_piece_;
            } else {
                piece_idx = BLACKS - move.captured_piece_;
            }
            --material_scores_[piece_idx];
        }

        // remove castle possibilities
        move.flags_before_ = flags_;
        if (moving_piece == PIECE_ROOK) {
            if (move.src_idx_ == (0 + (0 * 8))) {
                flags_ &= -1 ^ FLAG_WHITE_CAN_O_O_O;

            } else if (move.src_idx_ == (7 + (0 * 8))) {
                flags_ &= -1 ^ FLAG_WHITE_CAN_O_O;
            }

        } else if (moving_piece == -PIECE_ROOK) {
            if (move.src_idx_ == (0 + (7 * 8))) {
                flags_ &= -1 ^ FLAG_BLACK_CAN_O_O_O;

            } else if (move.src_idx_ == (7 + (7 * 8))) {
                flags_ &= -1 ^ FLAG_BLACK_CAN_O_O;
            }

        } else if (moving_piece == PIECE_KING) {
            flags_ &= -1 ^ (FLAG_WHITE_CAN_O_O_O | FLAG_WHITE_CAN_O_O);

        } else if (moving_piece == -PIECE_KING) {
            flags_ &= -1 ^ (FLAG_BLACK_CAN_O_O_O | FLAG_BLACK_CAN_O_O);
        }

        do_move_hash_normal_1(move, moving_piece);
    }

    private void undo_move_normal(final Move move) {
        final int moving_piece = board_[move.dst_idx_];
        board_[move.src_idx_] = moving_piece;
        board_[move.dst_idx_] = move.captured_piece_;

        if (move.captured_piece_ != 0) {
            final int piece_idx;
            if (move.captured_piece_ > 0) {
                piece_idx = WHITES + move.captured_piece_;
            } else {
                piece_idx = BLACKS - move.captured_piece_;
            }
            ++material_scores_[piece_idx];
        }

        flags_ = move.flags_before_;

        undo_move_hash_normal_1(move, moving_piece);
    }

    private void do_move_castle(final Move move,
                                int d_rook_src,
                                int d_rook_dst) {
        final int rook_src_idx = move.dst_idx_ + d_rook_src;
        final int rook_dst_idx = move.dst_idx_ + d_rook_dst;
        final int king_piece = board_[move.src_idx_];
        final int rook_piece = board_[rook_src_idx];
        board_[move.src_idx_] = PIECE_NONE;
        board_[rook_src_idx] = PIECE_NONE;
        board_[move.dst_idx_] = king_piece;
        board_[rook_dst_idx] = rook_piece;
        move.captured_piece_ = PIECE_NONE;

        // remove castle possibilities
        move.flags_before_ = flags_;
        if (king_piece > 0) {
            flags_ &= -1 ^ (FLAG_WHITE_CAN_O_O_O | FLAG_WHITE_CAN_O_O);

        } else {
            flags_ &= -1 ^ (FLAG_BLACK_CAN_O_O_O | FLAG_BLACK_CAN_O_O);
        }

//		do_move_hash_castle_1(move, moving_piece);
    }

    private void undo_move_castle(final Move move,
                                  int d_rook_src,
                                  int d_rook_dst) {
        final int rook_src_idx = move.dst_idx_ + d_rook_src;
        final int rook_dst_idx = move.dst_idx_ + d_rook_dst;
        final int king_piece = board_[move.dst_idx_];
        final int rook_piece = board_[rook_dst_idx];
        board_[move.dst_idx_] = PIECE_NONE;
        board_[rook_dst_idx] = PIECE_NONE;
        board_[move.src_idx_] = king_piece;
        board_[rook_src_idx] = rook_piece;

        flags_ = move.flags_before_;

//		undo_move_hash_castle_1(move, moving_piece);
    }

    private void do_move_hash_normal(final Move move) {
        final int moving_piece = board_[move.src_idx_];
        move.captured_piece_ = board_[move.dst_idx_];
        do_move_hash_normal_1(move, moving_piece);
    }

    private void undo_move_hash_normal(final Move move) {
        final int moving_piece = board_[move.src_idx_];    // same as do_move_hash, because board wasn't modified
        move.captured_piece_ = board_[move.dst_idx_];    // idem
        undo_move_hash_normal_1(move, moving_piece);
    }

    private void do_move_hash_normal_1(final Move move,
                                       final int moving_piece) {
        move.hash_ ^= ZOBRIST_TAB[move.captured_piece_ + 6][move.dst_idx_];    // remove captured piece
        move.hash_ ^= ZOBRIST_TAB[moving_piece + 6][move.src_idx_];        // remove moving piece from src
        move.hash_ ^= ZOBRIST_TAB[moving_piece + 6][move.dst_idx_];        // add moving piece to dst
    }

    private void undo_move_hash_normal_1(final Move move,
                                         final int moving_piece) {
        move.hash_ ^= ZOBRIST_TAB[moving_piece + 6][move.dst_idx_];        // remove moving piece from dst
        move.hash_ ^= ZOBRIST_TAB[moving_piece + 6][move.src_idx_];        // add moving piece to src
        move.hash_ ^= ZOBRIST_TAB[move.captured_piece_ + 6][move.dst_idx_];    // add captured piece to dst
    }

    private void do_move_pawn_promotion(final Move move) {
        final int pawn = board_[move.src_idx_];
        final int promoted;
        if (pawn > 0) {
            promoted = PIECE_QUEEN;
            --material_scores_[WHITES + PIECE_PAWN];
            ++material_scores_[WHITES + PIECE_QUEEN];

        } else {
            promoted = -PIECE_QUEEN;
            --material_scores_[BLACKS + PIECE_PAWN];
            ++material_scores_[BLACKS + PIECE_QUEEN];
        }

        move.captured_piece_ = board_[move.dst_idx_];
        board_[move.dst_idx_] = promoted;
        board_[move.src_idx_] = PIECE_NONE;

        if (move.captured_piece_ != 0) {
            final int piece_idx;
            if (move.captured_piece_ > 0) {
                piece_idx = WHITES + move.captured_piece_;
            } else {
                piece_idx = BLACKS - move.captured_piece_;
            }
            --material_scores_[piece_idx];
        }

        do_move_hash_pawn_promotion_1(move, pawn, promoted);
    }

    private void undo_move_pawn_promotion(final Move move) {
        final int promoted = board_[move.dst_idx_];
        final int pawn;
        if (promoted > 0) {
            pawn = PIECE_PAWN;
            ++material_scores_[WHITES + PIECE_PAWN];
            --material_scores_[WHITES + PIECE_QUEEN];

        } else {
            pawn = -PIECE_PAWN;
            --material_scores_[BLACKS + PIECE_PAWN];
            ++material_scores_[BLACKS + PIECE_QUEEN];
        }

        board_[move.src_idx_] = pawn;
        board_[move.dst_idx_] = move.captured_piece_;

        if (move.captured_piece_ != 0) {
            final int piece_idx;
            if (move.captured_piece_ > 0) {
                piece_idx = WHITES + move.captured_piece_;
            } else {
                piece_idx = BLACKS - move.captured_piece_;
            }
            ++material_scores_[piece_idx];
        }

        undo_move_hash_pawn_promotion_1(move, pawn, promoted);
    }

    private void do_move_hash_pawn_promotion(final Move move) {
        final int pawn = board_[move.src_idx_];
        final int promoted;
        if (pawn > 0) {
            promoted = PIECE_QUEEN;

        } else {
            promoted = -PIECE_QUEEN;
        }

        move.captured_piece_ = board_[move.dst_idx_];

        do_move_hash_pawn_promotion_1(move, pawn, promoted);
    }

    private void do_move_hash_pawn_promotion_1(final Move move,
                                               final int pawn,
                                               final int promoted) {
        move.hash_ ^= ZOBRIST_TAB[move.captured_piece_ + 6][move.dst_idx_];    // remove captured piece
        move.hash_ ^= ZOBRIST_TAB[pawn + 6][move.src_idx_];            // remove pawn from src
        move.hash_ ^= ZOBRIST_TAB[promoted + 6][move.dst_idx_];        // add promoted to dst
    }

    private void undo_move_hash_pawn_promotion(final Move move) {
        final int pawn = board_[move.src_idx_];
        final int promoted;
        if (pawn > 0) {
            promoted = PIECE_QUEEN;

        } else {
            promoted = -PIECE_QUEEN;
        }

        move.captured_piece_ = board_[move.dst_idx_];

        undo_move_hash_pawn_promotion_1(move, pawn, promoted);
    }

    private void undo_move_hash_pawn_promotion_1(final Move move,
                                                 final int pawn,
                                                 final int promoted) {
        move.hash_ ^= ZOBRIST_TAB[promoted + 6][move.dst_idx_];        // remove promoted
        move.hash_ ^= ZOBRIST_TAB[move.captured_piece_ + 6][move.dst_idx_];    // add captured
        move.hash_ ^= ZOBRIST_TAB[pawn + 6][move.src_idx_];            // add pawn
    }

    public boolean king_is_checked(final boolean white_king) {
        final int king_idx = find_colored_piece_index(white_king ? PIECE_KING : -PIECE_KING);
        final int king_y = king_idx >>> 3;
        final int king_x = king_idx & 7;
        return king_is_checked(king_idx,
                king_x,
                king_y,
                white_king);
    }

    public void add_possible_moves(final boolean white_move, final MoveList move_list) {
        king_idx_ = find_colored_piece_index(white_move ? PIECE_KING : -PIECE_KING);
/*
if (king_idx_ == -1) {
	System.out.println("king is dead");
	throw new IllegalStateException("king is dead");
}
*/
        king_y_ = king_idx_ >>> 3;
        king_x_ = king_idx_ & 7;
        white_move_ = white_move;
        move_list_ = move_list;

        int idx0 = -1;
        for (int y0 = 0; y0 != 8; ++y0) {
            for (int x0 = 0; x0 != 8; ++x0) {
                final int colored_piece = board_[++idx0];
                if (colored_piece == 0) {
                    continue;
                }

                final int piece_type;
                if (colored_piece >= 0) {
                    if (!white_move) {
                        continue;
                    }
                    piece_type = colored_piece;

                } else {
                    if (white_move) {
                        continue;
                    }
                    piece_type = -colored_piece;
                }
                switch (piece_type) {
                    case PIECE_PAWN:
                        add_possible_moves_pawn(idx0, x0, y0);
                        break;

                    case PIECE_KNIGHT:
                        add_possible_moves_knight(idx0, x0, y0);
                        break;

                    case PIECE_BISHOP:
                        add_possible_moves_bishop(idx0, x0, y0);
                        break;

                    case PIECE_ROOK:
                        add_possible_moves_rook(idx0, x0, y0);
                        break;

                    case PIECE_QUEEN:
                        add_possible_moves_queen(idx0, x0, y0);
                        break;

                    case PIECE_KING:
                        add_possible_moves_king(idx0, x0, y0);
                        break;
                }
            }
        }
    }

    public long hash() {
        long key = 0;
        for (int i = 0; i != 64; ++i) {
            final int piece = board_[i];
            key ^= ZOBRIST_TAB[piece + 6][i];    // add 6 because min value is -PIECE_KING
        }
        return key;
    }

    public void print(final PrintStream out) {
        final StringBuilder strb = new StringBuilder();
        int idx = -1;

        out.println("--------");
        for (int y = 0; y != 8; ++y) {
            strb.delete(0, strb.length());
            for (int x = 0; x != 8; ++x) {
                final int colored_piece = board_[++idx];
                if (colored_piece == 0) {
                    strb.append('.');
                    continue;
                }
                final boolean white_piece;
                final int piece_type;

                if (colored_piece >= 0) {
                    white_piece = true;
                    piece_type = colored_piece;
                } else {
                    white_piece = false;
                    piece_type = -colored_piece;
                }

                switch (piece_type) {
                    case PIECE_PAWN:
                        strb.append(white_piece ? 'P' : 'p');
                        break;

                    case PIECE_KNIGHT:
                        strb.append(white_piece ? 'N' : 'n');
                        break;

                    case PIECE_BISHOP:
                        strb.append(white_piece ? 'B' : 'b');
                        break;

                    case PIECE_ROOK:
                        strb.append(white_piece ? 'R' : 'r');
                        break;

                    case PIECE_QUEEN:
                        strb.append(white_piece ? 'Q' : 'q');
                        break;

                    case PIECE_KING:
                        strb.append(white_piece ? 'K' : 'k');
                        break;
                }
            }
            out.println(strb);
        }
        out.println("--------");
    }

    //_____________________________

    private void add_possible_moves_pawn(int idx0, int x0, int y0) {
        final int inc_idx;
        final int initial_y;
        final int promotion_y;
        final int enpassant_y;
        if (white_move_) {
            inc_idx = 8;
            initial_y = 1;
            promotion_y = 6;
            enpassant_y = 5;
        } else {
            inc_idx = -8;
            initial_y = 6;
            promotion_y = 1;
            enpassant_y = 2;
        }

        // ... 1 step forward
        {
            // idx0 + 8 will always be a valid index, as a pawn on the last row is always promoted,
            // so pawns never remain on the last row
            int idx1 = idx0 + inc_idx;
            int colored_piece = board_[idx1];
            if (colored_piece == 0) {
                if (y0 == promotion_y) {
                    add_possible_move_pawn_promotion(idx0, idx1);
                } else {
                    add_possible_move(idx0, idx1);

                    // ... 2 steps forward only possible if 1 step forward is free and on initial position
                    if (y0 == initial_y) {
                        idx1 = idx0 + (2 * inc_idx);
                        colored_piece = board_[idx1];
                        if (colored_piece == 0) {
                            add_possible_move(idx0, idx1);
                        }
                    }
                }
            }
        }

        // ... captures
        if (x0 != 0) {
            add_possible_moves_pawn_captures(idx0, x0, y0, inc_idx, -1, promotion_y, enpassant_y);
        }
        if (x0 != 7) {
            add_possible_moves_pawn_captures(idx0, x0, y0, inc_idx, 1, promotion_y, enpassant_y);
        }
    }

    private void add_possible_moves_pawn_captures(int idx0, int x0, int y0,
                                                  int inc_idx,
                                                  int dx,
                                                  int promotion_y,
                                                  int enpassant_y) {
        final int idx1 = idx0 + inc_idx + dx;
        final int colored_piece = board_[idx1];
        if (colored_piece != 0) {
            if (colored_piece > 0) {
                if (!white_move_) {
                    if (y0 == promotion_y) {
                        add_possible_move_pawn_promotion(idx0, idx1);
                    } else {
                        add_possible_move(idx0, idx1);
                    }
                }
            } else {
                if (white_move_) {
                    if (y0 == promotion_y) {
                        add_possible_move_pawn_promotion(idx0, idx1);
                    } else {
                        add_possible_move(idx0, idx1);
                    }
                }
            }

        } else if (y0 == enpassant_y) {
            final int x1 = x0 + dx;
            // en passant ?
            if (get_enpassant_column() == x1) {
                add_possible_move_enpassant(idx0, idx1, idx0 + dx);
            }
        }
    }

    private void add_possible_moves_knight(int idx0, int x0, int y0) {
        //
        //	..X.X..	(-1, +2) (+1, +2) |	+15 +17
        //	.X...X.	(-2, +1) (+2, +1) |	 +6 +10
        //	...N...
        //	.X...X.	(-2, -1) (+2, -1) |	-10  -6
        //	..X.X..	(-1, -2) (+1, -2) |	-17 -15
        //

        if (x0 > 1) {
            if (y0 < 6) {
                add_possible_moves_knight_1(idx0, -1 + (2 * 8));
                add_possible_moves_knight_1(idx0, -2 + (1 * 8));

            } else if (y0 < 7) {
                add_possible_moves_knight_1(idx0, -2 + (1 * 8));
            }

            if (y0 > 1) {
                add_possible_moves_knight_1(idx0, -1 + (-2 * 8));
                add_possible_moves_knight_1(idx0, -2 + (-1 * 8));

            } else if (y0 > 0) {
                add_possible_moves_knight_1(idx0, -2 + (-1 * 8));
            }

        } else if (x0 > 0) {
            if (y0 < 6) {
                add_possible_moves_knight_1(idx0, -1 + (2 * 8));
            }
            if (y0 > 1) {
                add_possible_moves_knight_1(idx0, -1 + (-2 * 8));
            }
        }

        if (x0 < 6) {
            if (y0 < 6) {
                add_possible_moves_knight_1(idx0, 1 + (2 * 8));
                add_possible_moves_knight_1(idx0, 2 + (1 * 8));

            } else if (y0 < 7) {
                add_possible_moves_knight_1(idx0, 2 + (1 * 8));
            }

            if (y0 > 1) {
                add_possible_moves_knight_1(idx0, 1 + (-2 * 8));
                add_possible_moves_knight_1(idx0, 2 + (-1 * 8));
            } else if (y0 > 0) {
                add_possible_moves_knight_1(idx0, 2 + (-1 * 8));
            }

        } else if (x0 < 7) {
            if (y0 < 6) {
                add_possible_moves_knight_1(idx0, 1 + (2 * 8));
            }
            if (y0 > 1) {
                add_possible_moves_knight_1(idx0, 1 + (-2 * 8));
            }
        }
    }

    private void add_possible_moves_knight_1(int idx0, int didx) {
        final int idx1 = idx0 + didx;
        final int colored_piece = board_[idx1];
        if (colored_piece == 0) {
            add_possible_move(idx0, idx1);
        } else {
            final boolean white_piece = colored_piece > 0;
            if ((white_move_ && !white_piece) || (!white_move_ && white_piece)) {
                add_possible_move(idx0, idx1);
            }
        }
    }

    private void add_possible_moves_bishop(int idx0, int x0, int y0) {
        add_possible_moves_along_path(idx0, x0, y0, 1, 1, 8, 8);
        add_possible_moves_along_path(idx0, x0, y0, 1, -1, 8, -1);
        add_possible_moves_along_path(idx0, x0, y0, -1, 1, -1, 8);
        add_possible_moves_along_path(idx0, x0, y0, -1, -1, -1, -1);
    }

    private void add_possible_moves_rook(int idx0, int x0, int y0) {
        add_possible_moves_along_path(idx0, x0, y0, 0, 1, -1, 8);
        add_possible_moves_along_path(idx0, x0, y0, 0, -1, -1, -1);
        add_possible_moves_along_path(idx0, x0, y0, 1, 0, 8, -1);
        add_possible_moves_along_path(idx0, x0, y0, -1, 0, -1, -1);
    }

    private void add_possible_moves_queen(int idx0, int x0, int y0) {
        add_possible_moves_along_path(idx0, x0, y0, 1, 0, 8, -1);
        add_possible_moves_along_path(idx0, x0, y0, 1, 1, 8, 8);
        add_possible_moves_along_path(idx0, x0, y0, 0, 1, -1, 8);
        add_possible_moves_along_path(idx0, x0, y0, -1, 1, -1, 8);
        add_possible_moves_along_path(idx0, x0, y0, -1, 0, -1, -1);
        add_possible_moves_along_path(idx0, x0, y0, -1, -1, -1, -1);
        add_possible_moves_along_path(idx0, x0, y0, 0, -1, -1, -1);
        add_possible_moves_along_path(idx0, x0, y0, 1, -1, 8, -1);
    }

    private void add_possible_moves_along_path(int idx0, int x0, int y0,
                                               int inc_x, int inc_y,
                                               int limit_x, int limit_y) {
        int x1 = x0 + inc_x;
        int y1 = y0 + inc_y;
        while ((x1 != limit_x) && (y1 != limit_y)) {
            final int idx1 = (y1 * 8) + x1;
            final int colored_piece = board_[idx1];
            if (colored_piece == 0) {
                add_possible_move(idx0, idx1);

            } else {
                final boolean white_piece = colored_piece > 0;
                if ((white_move_ && !white_piece) || (!white_move_ && white_piece)) {
                    add_possible_move(idx0, idx1);
                }
                return;
            }

            x1 += inc_x;
            y1 += inc_y;
        }
    }

    private void add_possible_moves_king(int idx0, int x0, int y0) {
        if (x0 != 0) {
            if (y0 != 7) {
                add_possible_moves_king_1(idx0, x0, y0, -1, 1);
            }
            if (y0 != 0) {
                add_possible_moves_king_1(idx0, x0, y0, -1, -1);
            }
            add_possible_moves_king_1(idx0, x0, y0, -1, 0);
        }

        if (x0 != 7) {
            if (y0 != 7) {
                add_possible_moves_king_1(idx0, x0, y0, 1, 1);
            }
            if (y0 != 0) {
                add_possible_moves_king_1(idx0, x0, y0, 1, -1);
            }
            add_possible_moves_king_1(idx0, x0, y0, 1, 0);
        }

        if (y0 != 7) {
            add_possible_moves_king_1(idx0, x0, y0, 0, 1);
        }
        if (y0 != 0) {
            add_possible_moves_king_1(idx0, x0, y0, 0, -1);
        }

        // castles
        {
            final int O_O_flag;
            final int O_O_O_flag;
            final int king_y;
            final int king_offset;
            final int king;
            if (white_move_) {
                O_O_flag = FLAG_WHITE_CAN_O_O;
                O_O_O_flag = FLAG_WHITE_CAN_O_O;
                king_y = 0;
                king_offset = 0 * 8;
                king = PIECE_KING;
            } else {
                O_O_flag = FLAG_BLACK_CAN_O_O;
                O_O_O_flag = FLAG_BLACK_CAN_O_O;
                king_y = 7;
                king_offset = 7 * 8;
                king = -PIECE_KING;
            }
            if ((flags_ & O_O_flag) == O_O_flag) {
                if ((board_[5 + king_offset] == 0) &&
                        (board_[6 + king_offset] == 0)) {
                    boolean checked;
                    board_[4 + king_offset] = 0;

                    board_[5 + king_offset] = king;
                    checked = king_is_checked(5 + king_offset, 5, king_y, white_move_);
                    board_[5 + king_offset] = 0;
                    if (!checked) {
                        board_[6 + king_offset] = king;
                        checked = king_is_checked(6 + king_offset, 6, king_y, white_move_);
                        board_[6 + king_offset] = 0;
                        if (!checked) {
                            move_list_.add(MoveList.encode_move_O_O(white_move_));
                        }
                    }

                    board_[4 + king_offset] = king;
                }
            }
            if ((flags_ & O_O_O_flag) == O_O_O_flag) {
                if ((board_[1 + king_offset] == 0) &&
                        (board_[2 + king_offset] == 0) &&
                        (board_[3 + king_offset] == 0)) {
                    boolean checked;
                    board_[4 + king_offset] = 0;

                    board_[3 + king_offset] = king;
                    checked = king_is_checked(3 + king_offset, 3, king_y, white_move_);
                    board_[3 + king_offset] = 0;
                    if (!checked) {
                        board_[2 + king_offset] = king;
                        checked = king_is_checked(2 + king_offset, 2, king_y, white_move_);
                        board_[2 + king_offset] = 0;
                        if (!checked) {
                            move_list_.add(MoveList.encode_move_O_O_O(white_move_));
                        }
                    }

                    board_[4 + king_offset] = king;
                }
            }
        }
    }

    private void add_possible_moves_king_1(int idx0, int x0, int y0,
                                           int dx, int dy) {
        final int x1 = x0 + dx;
        final int y1 = y0 + dy;
        final int idx1 = (y1 * 8) + x1;
        final int colored_piece = board_[idx1];
        if (colored_piece == 0) {
            add_possible_move_king(idx0, idx1, x1, y1);

        } else {
            final boolean white_piece = colored_piece > 0;
            if ((white_move_ && !white_piece) || (!white_move_ && white_piece)) {
                add_possible_move_king(idx0, idx1, x1, y1);
            }
            return;
        }
    }

    private void add_possible_move(final int idx0, final int idx1) {
        final int moving_piece = board_[idx0];
        final int captured_piece = board_[idx1];
        board_[idx1] = moving_piece;
        board_[idx0] = PIECE_NONE;

        // is the player's king checked ?
        if (!king_is_checked(king_idx_, king_x_, king_y_, white_move_)) {
            move_list_.add(MoveList.encode_move_normal(idx0, idx1));
        }

        board_[idx0] = moving_piece;
        board_[idx1] = captured_piece;
    }

    private void add_possible_move_pawn_promotion(final int idx0, final int idx1) {
        final int pawn = board_[idx0];
        final int captured_piece = board_[idx1];
        board_[idx1] = white_move_ ? PIECE_QUEEN : -PIECE_QUEEN;
        board_[idx0] = PIECE_NONE;

        // is the player's king checked ?
        if (!king_is_checked(king_idx_, king_x_, king_y_, white_move_)) {
            move_list_.add(MoveList.encode_move_promoted_pawn(idx0, idx1));
        }

        board_[idx0] = pawn;
        board_[idx1] = captured_piece;
    }

    private void add_possible_move_king(final int idx0,
                                        final int idx1,
                                        final int king_x1,
                                        final int king_y1) {
        final int moving_piece = board_[idx0];
        final int captured_piece = board_[idx1];
        board_[idx1] = moving_piece;
        board_[idx0] = PIECE_NONE;

        // is the player's king checked ?
        if (!king_is_checked(idx1, king_x1, king_y1, white_move_)) {
            move_list_.add(MoveList.encode_move_normal(idx0, idx1));
        }
        board_[idx0] = moving_piece;
        board_[idx1] = captured_piece;
    }

    private void add_possible_move_enpassant(final int idx0, final int idx1, final int idx2) {
/*
        final int captured_piece = board_[idx2];
		board_[idx1] = board_[idx0];
		board_[idx0] = PIECE_NONE;

		// is the player's king checked ?
		if (! king_is_checked(king_idx_, king_x_, king_y_, white_move_)) {
			possible_moves_.add( MoveList.encode_move_enpassant(idx0, idx1, idx2) );
		}
		board_[idx0] = board_[idx1];
		board_[idx1] = PIECE_NONE;
		board_[idx2] = captured_piece;
*/
    }


    private boolean king_is_checked(final int idx,
                                    final int x,
                                    final int y,
                                    final boolean white_king) {
        // ________ check for knights and pawns

        //
        //	..X.X..	(-1, +2) (+1, +2) |	+15 +17
        //	.X...X.	(-2, +1) (+2, +1) |	 +6 +10
        //	...K...
        //	.X...X.	(-2, -1) (+2, -1) |	-10  -6
        //	..X.X..	(-1, -2) (+1, -2) |	-17 -15
        //

        if (y > 1) {

            if (!white_king) {
                //.P.P.
                //..K..
                // on ranks 0 and 1 black king cannot be attacked by pawns, hence the y > 1 test
                if (x < 7) {
                    if (piece_checks_king(x, y, 1, -1, white_king, PIECE_PAWN)) {
                        return true;
                    }
                }
                if (x > 0) {
                    if (piece_checks_king(x, y, -1, -1, white_king, PIECE_PAWN)) {
                        return true;
                    }
                }
            }

            if (x > 1) {
                if (piece_checks_king(x, y, -2, -1, white_king, PIECE_KNIGHT)) {
                    return true;
                }
                if (piece_checks_king(x, y, -1, -2, white_king, PIECE_KNIGHT)) {
                    return true;
                }
            } else if (x > 0) {
                if (piece_checks_king(x, y, -1, -2, white_king, PIECE_KNIGHT)) {
                    return true;
                }
            }
            if (x < 6) {
                if (piece_checks_king(x, y, 2, -1, white_king, PIECE_KNIGHT)) {
                    return true;
                }
                if (piece_checks_king(x, y, 1, -2, white_king, PIECE_KNIGHT)) {
                    return true;
                }
            } else if (x < 7) {
                if (piece_checks_king(x, y, 1, -2, white_king, PIECE_KNIGHT)) {
                    return true;
                }
            }

        } else if (y > 0) {
            if (x > 1) {
                if (piece_checks_king(x, y, -2, -1, white_king, PIECE_KNIGHT)) {
                    return true;
                }
            }
            if (x < 6) {
                if (piece_checks_king(x, y, 2, -1, white_king, PIECE_KNIGHT)) {
                    return true;
                }
            }
        }

        if (y < 6) {

            if (white_king) {
                //..K..
                //.P.P.
                // on ranks 6 and 7 white king cannot be attacked by pawns, hence the y < 6 test
                if (x < 7) {
                    if (piece_checks_king(x, y, 1, 1, white_king, PIECE_PAWN)) {
                        return true;
                    }
                }
                if (x > 0) {
                    if (piece_checks_king(x, y, -1, 1, white_king, PIECE_PAWN)) {
                        return true;
                    }
                }
            }


            if (x > 1) {
                if (piece_checks_king(x, y, -2, 1, white_king, PIECE_KNIGHT)) {
                    return true;
                }
                if (piece_checks_king(x, y, -1, 2, white_king, PIECE_KNIGHT)) {
                    return true;
                }
            } else if (x > 0) {
                if (piece_checks_king(x, y, -1, 2, white_king, PIECE_KNIGHT)) {
                    return true;
                }
            }
            if (x < 6) {
                if (piece_checks_king(x, y, 2, 1, white_king, PIECE_KNIGHT)) {
                    return true;
                }
                if (piece_checks_king(x, y, 1, 2, white_king, PIECE_KNIGHT)) {
                    return true;
                }
            } else if (x < 7) {
                if (piece_checks_king(x, y, 1, 2, white_king, PIECE_KNIGHT)) {
                    return true;
                }
            }

        } else if (y < 7) {
            if (x > 1) {
                if (piece_checks_king(x, y, -2, 1, white_king, PIECE_KNIGHT)) {
                    return true;
                }
            }
            if (x < 6) {
                if (piece_checks_king(x, y, 2, 1, white_king, PIECE_KNIGHT)) {
                    return true;
                }
            }
        }

        // ________ check for queens, rooks, and bishops

        if (king_is_checked(idx, x, y, white_king, 1, 0)) {
            return true;
        }
        if (king_is_checked(idx, x, y, white_king, 1, 1)) {
            return true;
        }
        if (king_is_checked(idx, x, y, white_king, 0, 1)) {
            return true;
        }
        if (king_is_checked(idx, x, y, white_king, -1, 1)) {
            return true;
        }
        if (king_is_checked(idx, x, y, white_king, -1, 0)) {
            return true;
        }
        if (king_is_checked(idx, x, y, white_king, -1, -1)) {
            return true;
        }
        if (king_is_checked(idx, x, y, white_king, 0, -1)) {
            return true;
        }
        if (king_is_checked(idx, x, y, white_king, 1, -1)) {
            return true;
        }

        return false;
    }

    private boolean piece_checks_king(final int king_x, final int king_y,
                                      final int dx, final int dy,
                                      final boolean white_king,
                                      final int piece_type) {
        final int x = king_x + dx;
        final int y = king_y + dy;
        final int idx = x + (y << 3);
        int colored_piece = board_[idx];
        if (colored_piece == 0) {
            return false;
        }
        if (colored_piece > 0) {
            if (white_king) {
                return false;
            }
        } else {
            if (!white_king) {
                return false;
            }
            colored_piece = -colored_piece;
        }
        return colored_piece == piece_type;
    }

    private boolean king_is_checked(int idx,
                                    int x,
                                    int y,
                                    final boolean white_king,
                                    final int inc_x,
                                    final int inc_y) {
        int step = 0;

        final int limit_x = inc_x > 0 ? 8 : -1;
        final int limit_y = inc_y > 0 ? 8 : -1;
        final int inc = inc_x + (inc_y * 8);

//System.out.println("inc_x : " + inc_x + ", inc_y : " + inc_y);

        x += inc_x;
        y += inc_y;
        idx += inc;
        while ((x != limit_x) && (y != limit_y)) {
//System.out.println("idx : " + idx + " (" + x + ", " + y + ")");
            ++step;

            final int colored_piece = board_[idx];
            if (colored_piece != 0) {
                final int piece_type;
                if (colored_piece > 0) {
                    if (white_king) {
                        return false;
                    }
                    piece_type = colored_piece;

                } else {
                    if (!white_king) {
                        return false;
                    }
                    piece_type = -colored_piece;
                }
                switch (piece_type) {

                    case PIECE_PAWN:
                        return false;

                    case PIECE_KNIGHT:
                        return false;

                    case PIECE_BISHOP:
                        return ((inc_x != 0) && (inc_y != 0));    // checked if moving diag

                    case PIECE_ROOK:
                        return ((inc_x == 0) || (inc_y == 0));    // checked if moving strait

                    case PIECE_QUEEN:
                        return true;

                    case PIECE_KING:
                        return step == 1;

                    default:
                        throw new IllegalStateException("Unexpected piece [" + piece_type + "]");
                }
            }

            x += inc_x;
            y += inc_y;
            idx += inc;
        }

        return false;
    }

    private int get_enpassant_column() {
/*
		// either I don't understand >>> operator or it is not working as expected
		byte col = flags_;
		col &= 0x7f;
		col = (byte)(col >> 4);
		if (flags_ < 0) {
			col |= 8;
		}
		return col - 1;
*/
        return -1;
    }

    private void set_enpassant_column(int col) {
/*
		flags_ &= 15;
		flags_ |= (col + 1) << 4;
*/
    }

    // not state members. These members replace method parameters, the only purpose is to reduce the numbers of parameters in methods
    private boolean white_move_;
    private int king_idx_;
    private int king_x_;
    private int king_y_;
    private MoveList move_list_;

    private static final long[][] ZOBRIST_TAB = new long[(2 * 6) + 1][64]; //[-PIECE_KING PIECE_NONE PIECE_KING ] [0 6 12]

    static {
        final Random rand = new Random(1014);

        // -PIECE_KING     PIECE_NONE      PIECE_KING
        //      0              6              12
        for (int piece = 0; piece != 13; ++piece) {
            for (int i = 0; i != 64; ++i) {
                ZOBRIST_TAB[piece][i] = (piece == 6) ? 0 : rand.nextLong(); // set empty cells to 0
            }
        }
    }
}
