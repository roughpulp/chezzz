package rpulp.chezzz.ai;

import rpulp.chezzz.board.Board;
import rpulp.chezzz.board.Move;

public final class AIPlayer implements Runnable {
    public interface OnPlayed {
        void on_played(Move move);
    }

    public AIPlayer(final Board board, final Object game_mutex) {
        core_ = new AICore(board, game_mutex);
        thread_ = new Thread(this, "AIPlayer");
        thread_.start();
    }

    public void close() throws Exception {
        abort();
        synchronized (this) {
            alive_ = false;
            notify();
        }
    }

    public void reset() throws Exception {
        abort();
    }

    public void abort() throws Exception {
        core_.abort();
    }

    public void play(final boolean white_move,
                     final int ply,
                     final int think_time,
                     final OnPlayed callback) {
        synchronized (this) {    // relies on the property that java locks are reentrant, a task could call this method, for example when both white and black players are AI
            task_ = new PlayTask(white_move, ply, think_time, callback);
            notify();
        }
    }

    public void print_influence_map() {
        core_.print_influence_map();
    }

    public void print_board_eval() {
        core_.print_board_eval();
    }

    public void run() {
        try {
            System.out.println("AIPlayer thread started ...");

            synchronized (this) {
                while (alive_) {
                    wait();
                    if (!alive_) {
                        break;
                    }

                    Task task = burn_task();
                    while (task != null) {
                        task.run();
                        task = burn_task();    // a task may create a new task, for example when both white and black players are AI
                    }
                }
            }

            System.out.println("AIPlayer thread stopped");

        } catch (final Throwable th) {
            System.err.println("Caught the following in AIPlayer thread : [" + th.getMessage() + "]");
            th.printStackTrace(System.err);
        }
    }

    private Task burn_task() {
        final Task task = task_;
        task_ = null;
        return task;
    }

    private final AICore core_;
    private final Thread thread_;
    private boolean alive_ = true;
    private Task task_;

    private static interface Task {
        void run();
    }

    private class PlayTask implements Task {
        PlayTask(boolean white_move, int ply, final int think_time, final OnPlayed callback) {
            white_move_ = white_move;
            ply_ = ply;
            think_time_ = think_time;
            callback_ = callback;
        }

        public void run() {
            final Move move = core_.play(white_move_, ply_, think_time_);
            callback_.on_played(move);
        }

        final boolean white_move_;
        final int ply_;
        final int think_time_;
        final OnPlayed callback_;
    }
}
