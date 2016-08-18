package rpulp.chezzz.game;

import rpulp.chezzz.ai.AIPlayer;
import rpulp.chezzz.board.Board;
import rpulp.chezzz.board.Move;
import rpulp.chezzz.board.MoveList;
import rpulp.chezzz.board.MoveResult;
import rpulp.chezzz.io.BoardConsoleReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Game {
    public static final int WHITE = 0;
    public static final int BLACK = 1;

    public interface OnAIPlayed

    {
        void on_ai_played(int x0, int y0, int x1, int y1);
    }

    public Game() {
        ai_ = new AIPlayer(board_, board_);
        player_is_AI_[WHITE] = false;
        player_is_AI_[BLACK] = false;

        think_time_ = 5000;
    }

    public void set_AI_played_callback(final OnAIPlayed on_ai_played) {
        on_played_out_ = on_ai_played;
    }


    public void close() throws Exception {
        ai_.close();
    }

    public Object get_mutex() {
        return board_;
    }

    public void new_game() throws Exception {
        reset();
        if (is_AIs_turn()) {
            ai_.play(is_white_move(), ply_, think_time_, on_played_in_);
        }
    }

    public int get_piece(int x, int y) {
        return board_.get_piece(x, y);
    }

    public MoveResult check_n_move(int x0, int y0, int x1, int y1) {
        // sanity checks
        if ((x0 < 0) || (x0 > 7) || (y0 < 0) || (y0 > 7) ||
                (x1 < 0) || (x1 > 7) || (y1 < 0) || (y1 > 7)) {
            return null;
        }

        move_list_.clear();
        board_.add_possible_moves(is_white_move(), move_list_);
        final int idx0 = x0 + (y0 * 8);
        final int idx1 = x1 + (y1 * 8);
        final int encoded_move = move_list_.find_move(idx0, idx1);
        if (encoded_move == 0) {
            return null;
        }
        move_list_.decode_move(encoded_move, move_);
        move(move_);

        return move_res_;
    }

    public int get_current_player() {
        return current_player_;
    }

    public boolean is_white_move() {
        return current_player_ == WHITE;
    }

    public boolean is_AIs_turn() {
        return player_is_AI_[current_player_];
    }

    public boolean is_player_AI(int player) {
        return player_is_AI_[player];
    }

    public void toggle_AI(final int player) throws Exception {
        set_AI(player, !player_is_AI_[player]);
    }

    public void set_AI(final int player, boolean value) throws Exception {
        if (player_is_AI_[player] == value) {
            return;
        }
        if (player_is_AI_[player]) {
            if (player == current_player_) {
                ai_.abort();
            }
            player_is_AI_[player] = false;

        } else {
            if (player == current_player_) {
                ai_.play(is_white_move(), ply_, think_time_, on_played_in_);
            }

            player_is_AI_[player] = true;
        }
    }

    public int get_think_time() {
        return think_time_;
    }

    public void set_think_time(int think_time) {
        think_time_ = think_time;
    }

    public int get_ply() {
        return ply_;
    }

    public MoveList get_possible_moves(boolean white_move) {
        move_list_.clear();
        board_.add_possible_moves(white_move, move_list_);
        return move_list_;
    }

    public void load_board(final File file) throws Exception {
        try {
            final BoardConsoleReader reader = new BoardConsoleReader();
            reader.read(file, board_.get_builder());
            current_player_ = WHITE;
            System.out.println("Board loaded.");

        } catch (final Exception ex) {
            System.err.println("Couldn't load board from [" + file + "] : ");
            board_.print(System.err);
            ex.printStackTrace(System.err);
            reset();
        }
    }

    public void print_board_eval() {
        ai_.print_board_eval();
    }

    public List<Move> get_played_moves() {
        return played_moves_;
    }

    public void undo_last_move() throws Exception {
        ai_.reset();
        if (played_moves_.size() == 0) {
            return;
        }
        final Move move = played_moves_.remove(played_moves_.size() - 1);
        board_.undo_move(move);
        --ply_;
        current_player_ = 1 - current_player_;
        if (is_AIs_turn()) {
            ai_.play(is_white_move(), ply_, think_time_, on_played_in_);
        }
    }

    private void reset() throws Exception {
        ai_.reset();        // it is important that this call is made 1st, this is because we want to clear any AI pending tasks 1st.
        board_.reset();
        current_player_ = WHITE;
        ply_ = 0;
        played_moves_.clear();
    }

    private void move(final Move move) {
        board_.do_move(move);
        played_moves_.add(new Move(move));
        ++ply_;
        current_player_ = 1 - current_player_;
        if (is_AIs_turn()) {
            ai_.play(is_white_move(), ply_, think_time_, on_played_in_);
        }
    }

    private final Board board_ = new Board();
    private final AIPlayer ai_;
    private final OnPlayed on_played_in_ = new OnPlayed();
    private OnAIPlayed on_played_out_;
    private final MoveList move_list_ = new MoveList(64);
    private final Move move_ = new Move();
    private final MoveResult move_res_ = new MoveResult();
    private int ply_;
    private int current_player_ = WHITE;
    private boolean[] player_is_AI_ = new boolean[2];
    private int think_time_;
    private final ArrayList<Move> played_moves_ = new ArrayList<Move>(128);

    private final class OnPlayed implements AIPlayer.OnPlayed {
        public void on_played(final Move move) {
            if (move == null) {
                return;
            }
            synchronized (get_mutex()) {
                move(move);
                final int x0 = move.src_idx_ % 8;
                final int y0 = move.src_idx_ / 8;
                final int x1 = move.dst_idx_ % 8;
                final int y1 = move.dst_idx_ / 8;
                on_played_out_.on_ai_played(x0, y0, x1, y1);
            }
        }
    }
}
