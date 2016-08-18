package rpulp.chezzz.ai;

import rpulp.chezzz.board.Board;
import rpulp.chezzz.board.Constants;

import java.util.Arrays;

final class BoardEvaluator {
    private static final int MATERIAL_PAWN = 100;
    private static final int MATERIAL_KNIGHT = 300;
    private static final int MATERIAL_BISHOP = 300;
    private static final int MATERIAL_ROOK = 500;
    private static final int MATERIAL_QUEEN = 1000;

    private static final int DOUBLE_PAWNS_MALUS = 50;
    private static final int KNIGHT_IN_CENTER_BONUS = 50;

    private static final int INFLUENCE_PAWN = 1;
    private static final int INFLUENCE_KNIGHT = 1;
    private static final int INFLUENCE_BISHOP = 1;
    private static final int INFLUENCE_ROOK = 1;
    private static final int INFLUENCE_QUEEN = 1;
    private static final int INFLUENCE_KING = 1;
    private static final int INFLUENCE_KING_INIT_WEIGHT = 4;

    private static final int WHITE = 0;
    private static final int BLACK = 1;

    BoardEvaluator(final Board board) {
        board_ = board;
        bboard_ = board_.board_;

        influence_weights_ = new int[2][];
        influence_values_ = new int[2][];
        influence_weights_[WHITE] = new int[8 * 8];
        influence_weights_[BLACK] = new int[8 * 8];
        influence_values_[WHITE] = new int[8 * 8];
        influence_values_[BLACK] = new int[8 * 8];
    }

    short score() {
        short score = 0;
        score += score_material();
        score += score_pawn_structure();
        score += score_knight_position();
        score += score_influence();
        return score;
    }

    short score_material() {
        short score = 0;

        score += board_.material_scores_[Board.WHITES + Board.PIECE_PAWN] * MATERIAL_PAWN;
        score += board_.material_scores_[Board.WHITES + Board.PIECE_KNIGHT] * MATERIAL_KNIGHT;
        score += board_.material_scores_[Board.WHITES + Board.PIECE_BISHOP] * MATERIAL_BISHOP;
        score += board_.material_scores_[Board.WHITES + Board.PIECE_ROOK] * MATERIAL_ROOK;
        score += board_.material_scores_[Board.WHITES + Board.PIECE_QUEEN] * MATERIAL_QUEEN;

        score += board_.material_scores_[Board.BLACKS + Board.PIECE_PAWN] * -MATERIAL_PAWN;
        score += board_.material_scores_[Board.BLACKS + Board.PIECE_KNIGHT] * -MATERIAL_KNIGHT;
        score += board_.material_scores_[Board.BLACKS + Board.PIECE_BISHOP] * -MATERIAL_BISHOP;
        score += board_.material_scores_[Board.BLACKS + Board.PIECE_ROOK] * -MATERIAL_ROOK;
        score += board_.material_scores_[Board.BLACKS + Board.PIECE_QUEEN] * -MATERIAL_QUEEN;

        return score;
    }

    short score_pawn_structure() {
        short score = 0;

        int x = 0;
        do {
            int white_pawns = 0;
            int black_pawns = 0;
            int y = 1;    // pawns cannot be on 1th and 8th rank
            int idx = x;
            do {
                idx += 8;
                int piece = bboard_[idx];
                if (piece == Board.PIECE_PAWN) {
                    ++white_pawns;

                } else if (piece == -Board.PIECE_PAWN) {
                    ++black_pawns;
                }
            } while (++y != 7);    // pawns cannot be on 1th and 8th rank

            if (white_pawns > 1) {
                score -= (white_pawns - 1) * DOUBLE_PAWNS_MALUS;
            }
            if (black_pawns > 1) {
                score += (black_pawns - 1) * DOUBLE_PAWNS_MALUS;
            }
        } while (++x != 8);

        return score;
    }

    short score_knight_position() {
        int knights = board_.material_scores_[Board.WHITES + Board.PIECE_KNIGHT]
                + board_.material_scores_[Board.BLACKS + Board.PIECE_KNIGHT];

        if (knights == 0) {
            return 0;
        }
        short score = 0;
        for (int y = 2; y != 6; ++y) {
            for (int x = 2; x != 6; ++x) {
                int idx = (y << 3) + x;
                final int piece = bboard_[idx];
                if (piece == Board.PIECE_KNIGHT) {
                    score += KNIGHT_IN_CENTER_BONUS;
                    if (--knights == 0) {
                        return score;
                    }
                } else if (piece == -Board.PIECE_KNIGHT) {
                    score -= KNIGHT_IN_CENTER_BONUS;
                    if (--knights == 0) {
                        return score;
                    }
                }
            }
        }
        return score;
    }

    short score_influence() {
        Arrays.fill(influence_weights_[WHITE], 1);
        Arrays.fill(influence_weights_[BLACK], 1);
        Arrays.fill(influence_values_[WHITE], 0);
        Arrays.fill(influence_values_[BLACK], 0);

        int idx = -1;
        for (int y = 0; y != 8; ++y) {
            for (int x = 0; x != 8; ++x) {
                final int colored_piece = bboard_[++idx];
                if (colored_piece == 0) {
                    continue;
                }

                final int sign;
                final int[] values;
                final int piece_type;
                if (colored_piece > 0) {
                    sign = 1;
                    values = influence_values_[WHITE];
                    piece_type = colored_piece;

                } else {
                    sign = -1;
                    values = influence_values_[BLACK];
                    piece_type = -colored_piece;
                }
                switch (piece_type) {
                    case Board.PIECE_PAWN:
                        influence_pawn(idx, x, y, sign, values);
                        break;

                    case Board.PIECE_KNIGHT:
                        influence_knight(idx, x, y, sign, values);
                        break;

                    case Board.PIECE_BISHOP:
                        influence_bishop(idx, x, y, sign, values);
                        break;

                    case Board.PIECE_ROOK:
                        influence_rook(idx, x, y, sign, values);
                        break;

                    case Board.PIECE_QUEEN:
                        influence_queen(idx, x, y, sign, values);
                        break;

                    case Board.PIECE_KING:
                        influence_king(idx, x, y, sign, values);
                        break;
                }
            }
        }

        int score = 0;
        for (int c = 0; c != 2; ++c) {
            for (int i = 0; i != 64; ++i) {
                score += influence_values_[c][i] * influence_weights_[c][i];
            }
        }
        return (short) score;
    }

    void print_analysis() {
        System.out.println("==== board analysis ====");

        final short score_material = score_material();
        final short score_pawn = score_pawn_structure();
        final short score_knight = score_knight_position();
        final short score_influence = score_influence();
        short score_total = 0;
        score_total += score_material;
        score_total += score_pawn;
        score_total += score_knight;
        score_total += score_influence;

        System.out.println("material score        : " + score_material);
        System.out.println("pawn structure score  : " + score_pawn);
        System.out.println("knight position score : " + score_knight);
        System.out.println("influence map score   : " + score_influence);
        System.out.println("Total                 : " + score_total);

/*
        System.out.println("");
		print_influence_map();
*/
    }

    void print_influence_map() {
        final StringBuilder strb = new StringBuilder();
        print_influence_map("white", WHITE, strb);
        print_influence_map("black", BLACK, strb);
    }

    private void print_influence_map(final String color_name,
                                     final int color,
                                     final StringBuilder strb) {
        System.out.println(color_name);
        strb.delete(0, strb.length());
        strb.append("  |    ");
        for (int x = 0; x != 8; ++x) {
            strb.append(Constants.FILES[x]);
            strb.append(" |    ");
        }
        System.out.println(strb);
        System.out.println("-----------------------------------------------------------");
        int[] values = influence_values_[color];
        int[] weights = influence_weights_[color];
        for (int y = 0; y != 8; ++y) {
            strb.delete(0, strb.length());
            strb.append(Constants.RANKS[y]);
            strb.append(" | ");
            for (int x = 0; x != 8; ++x) {
                final int idx = (y * 8) + x;
                print_influence_map_value(values[idx] * weights[idx], strb);
                strb.append(" | ");
            }
            System.out.println(strb);
        }
        System.out.println("");
    }

    private void print_influence_map_value(int value, final StringBuilder strb) {
        if (value == 0) {
            strb.append("   0");
            return;
        }
        char sign;
        if (value > 0) {
            sign = ' ';
        } else {
            sign = '-';
            value = -value;
        }
        int pow = 100;
        do {
            int div = value / pow;
            value = value % pow;
            pow /= 10;
            if (div == 0) {
                strb.append(' ');
            } else {
                if (sign != 0) {
                    strb.append(sign);
                    sign = 0;
                }
                strb.append(div);
            }
        } while (pow != 0);
    }

    private void influence_pawn(int idx0, int x0, int y0, int sign, int[] values) {
        final int value = sign * INFLUENCE_PAWN;
        if (sign > 0) {
            if (x0 > 0) {
                values[idx0 + 8 - 1] += value;
            }
            if (x0 < 7) {
                values[idx0 + 8 + 1] += value;
            }
        } else {
            if (x0 > 0) {
                values[idx0 - 8 - 1] += value;
            }
            if (x0 < 7) {
                values[idx0 - 8 + 1] += value;
            }
        }
    }

    private void influence_knight(int idx0, int x0, int y0, int sign, int[] values) {
        //
        //	..X.X..	(-1, +2) (+1, +2) |	+15 +17
        //	.X...X.	(-2, +1) (+2, +1) |	 +6 +10
        //	...N...
        //	.X...X.	(-2, -1) (+2, -1) |	-10  -6
        //	..X.X..	(-1, -2) (+1, -2) |	-17 -15
        //

        if (x0 > 1) {
            if (y0 < 6) {
                influence_knight_1(idx0, -1 + (2 * 8), sign, values);
                influence_knight_1(idx0, -2 + (1 * 8), sign, values);

            } else if (y0 < 7) {
                influence_knight_1(idx0, -2 + (1 * 8), sign, values);
            }

            if (y0 > 1) {
                influence_knight_1(idx0, -1 + (-2 * 8), sign, values);
                influence_knight_1(idx0, -2 + (-1 * 8), sign, values);

            } else if (y0 > 0) {
                influence_knight_1(idx0, -2 + (-1 * 8), sign, values);
            }

        } else if (x0 > 0) {
            if (y0 < 6) {
                influence_knight_1(idx0, -1 + (2 * 8), sign, values);
            }
            if (y0 > 1) {
                influence_knight_1(idx0, -1 + (-2 * 8), sign, values);
            }
        }

        if (x0 < 6) {
            if (y0 < 6) {
                influence_knight_1(idx0, 1 + (2 * 8), sign, values);
                influence_knight_1(idx0, 2 + (1 * 8), sign, values);

            } else if (y0 < 7) {
                influence_knight_1(idx0, 2 + (1 * 8), sign, values);
            }

            if (y0 > 1) {
                influence_knight_1(idx0, 1 + (-2 * 8), sign, values);
                influence_knight_1(idx0, 2 + (-1 * 8), sign, values);
            } else if (y0 > 0) {
                influence_knight_1(idx0, 2 + (-1 * 8), sign, values);
            }

        } else if (x0 < 7) {
            if (y0 < 6) {
                influence_knight_1(idx0, 1 + (2 * 8), sign, values);
            }
            if (y0 > 1) {
                influence_knight_1(idx0, 1 + (-2 * 8), sign, values);
            }
        }
    }

    private void influence_knight_1(int idx0, int d_idx, int sign, int[] values) {
        values[idx0 + d_idx] += sign * INFLUENCE_KNIGHT;
    }

    private void influence_bishop(int idx0, int x0, int y0, int sign, int[] values) {
        final int value = sign * INFLUENCE_BISHOP;
        influence_along_path(idx0, x0, y0, 1, 1, 8, 8, value, values);
        influence_along_path(idx0, x0, y0, -1, 1, -1, 8, value, values);
        influence_along_path(idx0, x0, y0, -1, -1, -1, -1, value, values);
        influence_along_path(idx0, x0, y0, 1, -1, 8, -1, value, values);
    }

    private void influence_rook(int idx0, int x0, int y0, int sign, int[] values) {
        final int value = sign * INFLUENCE_ROOK;
        influence_along_path(idx0, x0, y0, 1, 0, 8, -1, value, values);
        influence_along_path(idx0, x0, y0, 0, 1, -1, 8, value, values);
        influence_along_path(idx0, x0, y0, -1, 0, -1, -1, value, values);
        influence_along_path(idx0, x0, y0, 0, -1, -1, -1, value, values);
    }

    private void influence_queen(int idx0, int x0, int y0, int sign, int[] values) {
        final int value = sign * INFLUENCE_QUEEN;
        influence_along_path(idx0, x0, y0, 1, 0, 8, -1, value, values);
        influence_along_path(idx0, x0, y0, 1, 1, 8, 8, value, values);
        influence_along_path(idx0, x0, y0, 0, 1, -1, 8, value, values);
        influence_along_path(idx0, x0, y0, -1, 1, -1, 8, value, values);
        influence_along_path(idx0, x0, y0, -1, 0, -1, -1, value, values);
        influence_along_path(idx0, x0, y0, -1, -1, -1, -1, value, values);
        influence_along_path(idx0, x0, y0, 0, -1, -1, -1, value, values);
        influence_along_path(idx0, x0, y0, 1, -1, 8, -1, value, values);
    }

    private void influence_king(int idx0, int x0, int y0, int sign, int[] values) {
        final int[] weights;
        if (sign > 0) {
            weights = influence_weights_[BLACK]; // blacks aim at white king
        } else {
            weights = influence_weights_[WHITE]; // whites aim at black king
        }
        influence_fill_weights(idx0, x0, y0, INFLUENCE_KING_INIT_WEIGHT, weights);

        final int value = sign * INFLUENCE_KING;
        influence_along_path_king(idx0, x0, y0, 1, 0, 8, -1, value, values);
        influence_along_path_king(idx0, x0, y0, 1, 1, 8, 8, value, values);
        influence_along_path_king(idx0, x0, y0, 0, 1, -1, 8, value, values);
        influence_along_path_king(idx0, x0, y0, -1, 1, -1, 8, value, values);
        influence_along_path_king(idx0, x0, y0, -1, 0, -1, -1, value, values);
        influence_along_path_king(idx0, x0, y0, -1, -1, -1, -1, value, values);
        influence_along_path_king(idx0, x0, y0, 0, -1, -1, -1, value, values);
        influence_along_path_king(idx0, x0, y0, 1, -1, 8, -1, value, values);
    }

    private void influence_along_path(int idx0, int x0, int y0,
                                      int inc_x, int inc_y,
                                      int limit_x, int limit_y,
                                      int value, int[] values) {
        int x1 = x0 + inc_x;
        int y1 = y0 + inc_y;
        while ((x1 != limit_x) && (y1 != limit_y)) {
            final int idx1 = (y1 * 8) + x1;
            final int colored_piece = bboard_[idx1];
            values[idx1] += value;
            if (colored_piece != 0) {
                return;
            }

            x1 += inc_x;
            y1 += inc_y;
        }
    }

    private void influence_along_path_king(int idx0, int x0, int y0,
                                           int inc_x, int inc_y,
                                           int limit_x, int limit_y,
                                           int value, int[] values) {
        int x1 = x0 + inc_x;
        int y1 = y0 + inc_y;
        if ((x1 != limit_x) && (y1 != limit_y)) {
            final int idx1 = (y1 * 8) + x1;
            final int colored_piece = bboard_[idx1];
            values[idx1] += value;
        }
    }

    private void influence_fill_weights(int orig_idx, int orig_x, int orig_y,
                                        int d_wgt, int[] weights) {
        // 44444444  .22222..  ........  ..111...
        // 33333334  .21112..  11......  ..1o1...
        // 32222234  .21o12..  o1......  ..111...
        // 32111234  .21112..  11......  .22222..
        // 321o1234  .22222..  ........  ........
        // 32111234  ........  ........  ........
        // 32222234  ........  ........  ...111..
        // 33333334  ........  ........  ...1o1..

        weights[orig_idx] += d_wgt;

        int d_pos = 0;
        while (--d_wgt != 0) {
            ++d_pos;
            int x_l = orig_x - d_pos;
            int x_r = orig_x + d_pos;
            int y_t = orig_y - d_pos;
            int y_b = orig_y + d_pos;

            // draw 4 segments

            { // horizontal segments
                final int x0 = (x_l > -1) ? x_l : 0;
                final int x1 = ((x_r < 8) ? x_r : 7) + 1;    // +1 : x1 is the limit

                // #1 (x_l, y_t) (x_r, y_t)
                if (y_t > -1) {
                    int idx = (y_t * 8) + x0;
                    for (int x = x0; x != x1; ++x) {
                        weights[idx] += d_wgt;
                        ++idx;
                    }
                }
                // #2 (x_l, y_b) (x_r, y_b)
                if (y_b < 8) {
                    int idx = (y_b * 8) + x0;
                    for (int x = x0; x != x1; ++x) {
                        weights[idx] += d_wgt;
                        ++idx;
                    }
                }
            }
            { // vertical segments
                int y0 = y_t + 1;            // +1 : y0 was taken care of by the H segments
                int y1 = y_b - 1;            // -1 : for the same reason as above
                y0 = (y0 > -1) ? y0 : 0;
                y1 = ((y1 < 8) ? y1 : 7) + 1;        // +1 : y1 is the limit

                // #3 (x_l, y_t + 1) (x_l, y_b - 1)
                if (x_l > -1) {
                    int idx = (y0 * 8) + x_l;
                    for (int y = y0; y != y1; ++y) {
                        weights[idx] += d_wgt;
                        idx += 8;
                    }
                }
                // #4 (x_r, y_t + 1) (x_r, y_b - 1)
                if (x_r < 8) {
                    int idx = (y0 * 8) + x_r;
                    for (int y = y0; y != y1; ++y) {
                        weights[idx] += d_wgt;
                        idx += 8;
                    }
                }
            }
        }
    }

    private final Board board_;
    private final int[] bboard_;
    private final int[][] influence_weights_;
    private final int[][] influence_values_;
}
