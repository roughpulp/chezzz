package rpulp.chezzz.ai;

import rpulp.chezzz.board.*;

public final class AICore {
    enum Status {
        IDLE,
        RUNNING,
        ABORT
    }

    AICore(final Board board, final Object game_mutex) {
        game_mutex_ = game_mutex;
        real_board_ = board;
        board_ = new Board();
        evaluator_ = new BoardEvaluator(board_);
    }

    void reset() throws Exception {
        abort();

        // stats
        {
            stat_move_list_max_size_ = 0;
            stat_max_possible_moves_ = 0;
            stat_total_nodes_ = 0;
            stat_total_leaves_ = 0;
            stat_total_transtab_hits_ = 0;
            stat_total_time_ = 0;
        }
    }

    void abort() throws Exception {
        synchronized (this) {
            switch (status_) {

                case IDLE:
                    return;

                case ABORT:
                    throw new IllegalStateException("Not expecting mutliple ABORT commands");

                case RUNNING:
                    status_ = Status.ABORT;
                    while (status_ != Status.IDLE) {
                        wait();
                    }
                    return;

                default:
                    throw new IllegalStateException("Unexpected status [" + status_ + "]");
            }
        }
    }

    Move play(final boolean white_move, final int ply, final int think_time) {
        System.out.println("AI playing as [" + (white_move ? "white" : "black") + "] ...");

        final Move move = new Move();
        find_best_moves(white_move, ply, think_time, move);
        if (best_moves_.size_ == 0) {
            return null;
        }
        best_moves_.decode_move_at(0, move);

        {
            final StringBuilder strb = new StringBuilder("AI playing : ");
            MoveNotationCodec.encode(move, strb);
            System.out.println(strb);
        }

        return move;
    }

    void find_best_moves(final boolean white_move,
                         final int ply,
                         final int think_time,
                         final Move move) {
        try {
            status_check_countdown_ = 0;
            synchronized (this) {
                status_ = Status.RUNNING;
                notify();
            }

            find_best_moves_1(white_move, ply, think_time, move);

        } catch (AbortException ex) {
            System.out.println("AICore aborted");
            best_moves_.clear();

        } finally {
            synchronized (this) {
                status_ = Status.IDLE;
                notify();
            }
        }
    }

    void print_influence_map() {
        synchronized (game_mutex_) {
            board_.copy_from(real_board_);
        }
        evaluator_.score_influence();
        evaluator_.print_influence_map();
    }

    void print_board_eval() {
        synchronized (game_mutex_) {
            board_.copy_from(real_board_);
        }
        evaluator_.print_analysis();
    }

    private void find_best_moves_1(final boolean white_move,
                                   final int ply,
                                   final int think_time,
                                   final Move move) {
        t0_ = System.currentTimeMillis();

        synchronized (game_mutex_) {
            board_.copy_from(real_board_);
        }

        System.out.println("\n====================================================");
        System.out.println("find best moves for [" + (white_move ? "white" : "black") + "] ply [" + ply + "] score [" + score(white_move) + "]\n");


        max_think_time_ = think_time;
        max_depth_ = 0;
        try {
            while (true) {
                final long one_run_t0 = System.currentTimeMillis();
                // reset stats
                {
                    stat_leaves_ = 0;
                    stat_nodes_ = 0;
                    stat_transtab_hits_ = 0;
                }

                index_lst_.clear();
                move_list_.clear();
                trans_tab_.clear();

                ++max_depth_;
                System.out.println("depth-search [" + max_depth_ + "] ...");
                best_score_ = minmax(white_move,
                        ply,
                        0,
                        MIN_SCORE,
                        MAX_SCORE,
                        board_.hash());
                best_moves_.clear();
                for (int i = 0; i != index_lst_.size_; ++i) {
                    best_moves_.add(move_list_.moves_[index_lst_.indices_[i]]);
                }

                final long one_run_t1 = System.currentTimeMillis();
                final long one_run_dt = one_run_t1 - one_run_t0;


                System.out.println("depth-search [" + max_depth_ + "] done. Score: " + best_score_ + ", time: " + (one_run_t1 - t0_) + " ms");

                // best moves
                {
                    strb_.delete(0, strb_.length());
                    strb_.append(best_moves_.size_);
                    strb_.append(" best move(s) : ");
                    for (int i = 0; i != best_moves_.size_; ++i) {
                        if (i != 0) {
                            strb_.append(", ");
                        }
                        best_moves_.decode_move_at(i, move);
                        MoveNotationCodec.encode(move, strb_);
                    }
                    System.out.println(strb_);
                }

                // stats
                {
                    stat_total_time_ += one_run_dt;
                    stat_total_nodes_ += stat_nodes_;
                    stat_total_leaves_ += stat_leaves_;
                    stat_total_transtab_hits_ += stat_transtab_hits_;

                    strb_.delete(0, strb_.length());
                    strb_.append("t "
                    ).append(one_run_dt
                    ).append("\tn "
                    ).append(stat_nodes_
                    ).append("\tl "
                    ).append(stat_leaves_
                    ).append("\tn/t "
                    ).append((float) stat_nodes_ / (float) one_run_dt
                    ).append("\ttts "
                    ).append(trans_tab_.size()
                    ).append("\ttth "
                    ).append(stat_transtab_hits_
                    ).append("\ttth/n "
                    ).append((float) stat_transtab_hits_ / (float) stat_nodes_);
                    System.out.println(strb_);
                }
                System.out.println("");
            }
        } catch (ThinkTimeOverException timeout) {
        }

        // stats
        {
            System.out.println("---- accum stats ----");
            System.out.println("leaves: " + stat_total_leaves_ + ",\t nodes: " + stat_total_nodes_ + ",\t time: " + stat_total_time_ + " ms,\t ratio: " + ((float) stat_total_nodes_ / (float) stat_total_time_) + " nodes/ms");
            System.out.println("trans-table. size: " + trans_tab_.size() + ",\t hits: " + stat_total_transtab_hits_ + ",\t ratio: " + ((float) stat_total_transtab_hits_ / (float) stat_total_nodes_) + " hits/nodes\n");
            System.out.println("move list max size : " + stat_move_list_max_size_);
            System.out.println("max possible moves : " + stat_max_possible_moves_);
            System.out.println("");
        }

        System.out.println("================================\n");
    }

    private short minmax(boolean white_move,
                         int ply,
                         int l_depth,
                         short alpha,
                         short beta,
                         long hash) {
        check_abort();

        if ((System.currentTimeMillis() - t0_) >= max_think_time_) {
            throw THINK_TIME_OVER;
        }

        ++stat_nodes_;

/**/
        final String signature = null; //board_.get_signature(strb_);
        if (trans_tab_.find(hash, trans_entry_)) {
/*
            if (! signature.equals(trans_entry_.signature_)) {
				System.err.println("------------------------------------");
				System.err.println("Signature error for hash [" + Long.toHexString(hash) + "]");
				System.err.println("depth: " + l_depth + "/" + max_depth_);
				System.err.println("expected :");
				System.err.println(signature);
				System.err.println("got :");
				System.err.println(trans_entry_.signature_);
				System.err.println("------------------------------------");
			}
*/

            ++stat_transtab_hits_;
//			return trans_entry_.score_;
        }
/**/

        final short score = minmax_1(white_move, ply, l_depth, alpha, beta, hash);
/**/
        trans_tab_.put(hash, score, l_depth, signature);
/**/

        return score;
    }

    private short minmax_1(boolean white_move,
                           int ply,
                           int depth,
                           short alpha,    // assured min score of max player
                           short beta,    // assured max score of min player
                           long hash) {
        // generate possible moves
        final int move_list_beg = move_list_.size_;
        board_.add_possible_moves(white_move, move_list_);
        final int move_list_end = move_list_.size_;
        final int possible_moves_size = move_list_end - move_list_beg;
        // no possible move means either checkmated or stalemated
        if (possible_moves_size == 0) {
            ++stat_leaves_;
            return board_.king_is_checked(white_move) ? MIN_SCORE : STALEMATE_SCORE;
        }
        try {
            // stats
            {
                if (possible_moves_size > stat_max_possible_moves_) {
                    stat_max_possible_moves_ = possible_moves_size;
                }
                if (move_list_end > stat_move_list_max_size_) {
                    stat_move_list_max_size_ = move_list_end;
                }
            }

            if (depth == max_depth_) {
                ++stat_leaves_;
                return score(white_move);
            }

            final Move move = new Move(hash);
            short best_score = MIN_SCORE;

/*
			// reorder possible moves
			for (int i = move_list_beg; i != move_list_end; ++i) {
				move_list_.decode_move_at(i, move);
				board_.do_move_hash(move);
				final long move_hash = move.hash_;
				board_.undo_move_hash(move);

				final short score;
				if (trans_tab_.find(move_hash, trans_entry_)) {
					score = trans_entry_.score_;
				} else {
					score = MIN_SCORE;
				}
				move_list_.set_score_at(i, score);
			}
			move_list_.sort(move_list_beg, move_list_end);
*/

            for (int i = (move_list_end - 1); i != (move_list_beg - 1); --i) {
                move_list_.decode_move_at(i, move);
                move.hash_ = hash;
                board_.do_move(move);
                short score;
                try {
                    score = minmax(!white_move,
                            ply,
                            depth + 1,
                            (short) -beta,
                            (short) -alpha,
                            move.hash_);

                } finally {
                    board_.undo_move(move);
                }
                score = (short) -score;

                if (score > best_score) {
                    if (depth == 0) {
                        index_lst_.clear();
                        index_lst_.add(i);
                    }
                    best_score = score;

                } else if (score == best_score) {
                    if (depth == 0) {
                        index_lst_.add(i);
                    }
                }
                if (best_score > alpha) {
                    alpha = best_score;
                }
                if (alpha >= beta) {
                    return alpha;
                }
            }
            return best_score;

        } finally {
            if (depth != 0) {
                move_list_.truncate(move_list_beg);
            }
        }
    }

    private short score(boolean white_move) {
        return white_move ? evaluator_.score() : ((short) -evaluator_.score());
    }

    private void check_abort() {

        if (++status_check_countdown_ != 100000) {
            return;
        }
        status_check_countdown_ = 0;

        synchronized (this) {
            if (status_ == Status.ABORT) {
                throw new AbortException();
            }
        }
    }

    private Status status_ = Status.IDLE;
    private int status_check_countdown_;

    private final Object game_mutex_;
    private final Board real_board_;
    private final Board board_;
    private final BoardEvaluator evaluator_;
    private final IndexList index_lst_ = new IndexList();
    private final TranspositionTable trans_tab_ = new TranspositionTable();
    private final TranspositionTable.Entry trans_entry_ = new TranspositionTable.Entry();

    private short best_score_;
    private final MoveList best_moves_ = new MoveList(64);
    private final MoveList move_list_ = new MoveList(512);

    private long t0_;
    private long max_think_time_ = 5 * 1000;
    private int max_depth_;


    private int stat_nodes_;
    private int stat_leaves_;
    private int stat_transtab_hits_;
    private int stat_move_list_max_size_ = 0;
    private int stat_max_possible_moves_ = 0;
    private long stat_total_nodes_ = 0;
    private long stat_total_leaves_ = 0;
    private int stat_total_transtab_hits_ = 0;
    private long stat_total_time_ = 0;

    private final StringBuilder strb_ = new StringBuilder();

    private static final ThinkTimeOverException THINK_TIME_OVER = new ThinkTimeOverException();

    private static final short MAX_SCORE = Short.MAX_VALUE;
    private static final short MIN_SCORE = -MAX_SCORE;    // required because -Short.MIN_VALUE != Short.MAX_VALUE
    private static final short STALEMATE_SCORE = MIN_SCORE / 2;

    private static final class IndexList {
        void clear() {
            size_ = 0;
        }

        void add(final int idx) {
            if (indices_.length == size_) {
                final int[] old_indices = indices_;
                indices_ = new int[((size_ * 3) / 2) + 1];
                System.arraycopy(old_indices, 0, indices_, 0, size_);
            }
            indices_[size_] = idx;
            ++size_;
        }

        int[] indices_ = new int[64];
        int size_ = 0;
    }

    private static final class ThinkTimeOverException extends RuntimeException {
    }

    private static final class AbortException extends RuntimeException {
    }
}
