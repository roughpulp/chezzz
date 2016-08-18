package rpulp.chezzz.board;

import java.io.PrintStream;
import java.util.Arrays;

public final class BoardBuilder {
    public enum Piece {
        PAWN,
        KNIGHT,
        BISHOP,
        ROOK,
        QUEEN,
        KING
    }

    public enum Color {
        WHITE,
        BLACK
    }

    public void set_empty(final int row, final int col) {
        if ((row < 0) || (row > 7)) {
            throw new IndexOutOfBoundsException("invalid row [" + row + "]");
        }
        if ((col < 0) || (col > 7)) {
            throw new IndexOutOfBoundsException("invalid column [" + col + "]");
        }

        int idx = Board.coord_2_index(col, row);
        int old_piece = board_.board_[idx];
        if (old_piece != Board.PIECE_NONE) {
            final int mat_idx;
            if (old_piece > 0) {
                mat_idx = Board.WHITES + old_piece;
            } else {
                mat_idx = Board.BLACKS - old_piece;
            }
            --board_.material_scores_[mat_idx];
            board_.board_[idx] = Board.PIECE_NONE;
        }
    }

    public void set_piece(final int row, final int col, final Piece piece, final Color color) {
        if ((row < 0) || (row > 7)) {
            throw new IndexOutOfBoundsException("invalid row [" + row + "]");
        }
        if ((col < 0) || (col > 7)) {
            throw new IndexOutOfBoundsException("invalid column [" + col + "]");
        }

        int ipiece;

        switch (piece) {

            case PAWN:
                ipiece = Board.PIECE_PAWN;
                break;

            case KNIGHT:
                ipiece = Board.PIECE_KNIGHT;
                break;

            case BISHOP:
                ipiece = Board.PIECE_BISHOP;
                break;

            case ROOK:
                ipiece = Board.PIECE_ROOK;
                break;

            case QUEEN:
                ipiece = Board.PIECE_QUEEN;
                break;

            case KING:
                ipiece = Board.PIECE_KING;
                break;

            default:
                throw new IllegalStateException("unexpected piece: " + piece);
        }

        int mat_idx;

        switch (color) {
            case WHITE:
                mat_idx = Board.WHITES + ipiece;
                break;

            case BLACK:
                ipiece = -ipiece;
                mat_idx = Board.BLACKS - ipiece;
                break;

            default:
                throw new IllegalStateException("unexpected color: " + color);
        }

        ++board_.material_scores_[mat_idx];
        board_.board_[Board.coord_2_index(col, row)] = ipiece;
    }

    public void close() {
        if (board_.material_scores_[Board.WHITES + Board.PIECE_KING] != 1) {
            throw new IllegalStateException("Whites are missing their King.");
        }
        if (board_.material_scores_[Board.BLACKS + Board.PIECE_KING] != 1) {
            throw new IllegalStateException("Blacks are missing their King.");
        }
    }

    public void print(PrintStream out) {
        board_.print(out);
    }

    BoardBuilder(final Board board) {
        board_ = board;

        Arrays.fill(board_.board_, Board.PIECE_NONE);
        Arrays.fill(board_.material_scores_, (short) 0);
    }

    private final Board board_;
}
