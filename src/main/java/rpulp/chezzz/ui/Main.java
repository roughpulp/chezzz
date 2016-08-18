package rpulp.chezzz.ui;

import rpulp.chezzz.board.Board;
import rpulp.chezzz.board.MoveResult;
import rpulp.chezzz.game.Game;
import rpulp.chezzz.board.Constants;
import rpulp.chezzz.pix.v0.PixLoader;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import static rpulp.chezzz.log.Log.error;
import static rpulp.chezzz.pix.v0.PixLoader.load_1_pix;

public final class Main {
    public static void main(final String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Main(args);

                } catch (Throwable th) {
                    System.err.println("Error : " + th.getMessage());
                    th.printStackTrace(System.err);
                }
            }
        });
    }

    public Main(final String[] args) throws Exception {
        game_.set_AI_played_callback(
                new Game.OnAIPlayed() {
                    public void on_ai_played(int x0, int y0, int x1, int y1) {
                        board_comp_.player_moved(x0, y0, x1, y1);
                    }
                });

        load_pix();

        frame_.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//		frame_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame_.getContentPane().setBackground(Color.WHITE);
        frame_.getContentPane().setLayout(new BorderLayout());

        {
            header_lbl_.setFont(new Font("verdana", Font.PLAIN, 18));
            header_lbl_.setForeground(Color.BLACK);
            update_header();

            final JPanel hdr_panel = new JPanel();
            hdr_panel.setBackground(Color.WHITE);
            hdr_panel.add(header_lbl_, BorderLayout.CENTER);
            frame_.getContentPane().add(hdr_panel, BorderLayout.NORTH);
        }

        new_game_btn_.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (new_game_btn_ == evt.getSource()) {
                    new_game();
                }
            }
        });

        switch_btn_.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (switch_btn_ == evt.getSource()) {
                    board_comp_.toggle_view_white();
                    board_comp_.repaint();
                }
            }
        });

        load_btn_.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (load_btn_ == evt.getSource()) {
                        final JFileChooser chooser = get_file_chooser();
                        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        chooser.setMultiSelectionEnabled(false);
                        //chooser.setCurrentDirectory(dir);

                        final int res = chooser.showDialog(frame_, "Load");
                        if (res == JFileChooser.APPROVE_OPTION) {
                            final File file = chooser.getSelectedFile();
                            load_board(file);

                        } else if (res == JFileChooser.CANCEL_OPTION) {

                        } else if (res == JFileChooser.ERROR_OPTION) {
                        } else {
                            throw new IllegalStateException("Unexpected JFileChooser option [" + res + "]");
                        }
                    }
                } catch (Exception ex) {
                    error("Caught the following exception : ", ex);
                }
            }
        });

        undo_btn_.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undo_btn_ == evt.getSource()) {
                        synchronized (game_.get_mutex()) {
                            game_.undo_last_move();
                            update_header();
                            board_comp_.clear_selections();
                            board_comp_.repaint();
                        }
                    }
                } catch (Exception ex) {
                    error("Caught the following exception : ", ex);
                }
            }
        });

        {
            final JPanel btn_panel = new JPanel();
            btn_panel.add(new_game_btn_);
            btn_panel.add(load_btn_);
            btn_panel.add(switch_btn_);
            btn_panel.add(undo_btn_);
            frame_.getContentPane().add(btn_panel, BorderLayout.SOUTH);
        }

        // EAST panel
        {
            // player-AI panel
            final JPanel player_ai_panel;
            {
                w_player_type_btn_.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if (w_player_type_btn_ == evt.getSource()) {
                            synchronized (game_.get_mutex()) {
                                try {
                                    game_.toggle_AI(Game.WHITE);
                                } catch (Exception ex) {
                                    error("Caught this", ex);
                                }
                                w_player_type_btn_.setText(get_player_type_label(Game.WHITE));
                            }
                        }
                    }
                });
                b_player_type_btn_.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if (b_player_type_btn_ == evt.getSource()) {
                            synchronized (game_.get_mutex()) {
                                try {
                                    game_.toggle_AI(Game.BLACK);
                                } catch (Exception ex) {
                                    error("Caught this", ex);
                                }
                                b_player_type_btn_.setText(get_player_type_label(Game.BLACK));
                            }
                        }
                    }
                });

                final JPanel w_panel;
                {
                    final JLabel lbl = new JLabel("White : ");
                    lbl.setAlignmentX(0.0f);
                    w_player_type_btn_.setText(get_player_type_label(Game.WHITE));
                    w_player_type_btn_.setAlignmentX(0.0f);

                    w_panel = new JPanel();
                    w_panel.setLayout(new BoxLayout(w_panel, BoxLayout.X_AXIS));
                    w_panel.setAlignmentX(0.0f);

                    w_panel.add(lbl);
                    w_panel.add(w_player_type_btn_);
                }

                final JPanel b_panel;
                {
                    final JLabel lbl = new JLabel("Black  : ");
                    lbl.setAlignmentX(0.0f);
                    b_player_type_btn_.setText(get_player_type_label(Game.BLACK));
                    w_player_type_btn_.setAlignmentX(0.0f);

                    b_panel = new JPanel();
                    b_panel.setLayout(new BoxLayout(b_panel, BoxLayout.X_AXIS));
                    b_panel.setAlignmentX(0.0f);

                    b_panel.add(lbl);
                    b_panel.add(b_player_type_btn_);
                }

                player_ai_panel = new JPanel();
                player_ai_panel.setLayout(new BoxLayout(player_ai_panel, BoxLayout.Y_AXIS));

                player_ai_panel.add(w_panel);
                player_ai_panel.add(b_panel);
            }

            // AI-ctrl panel
            final JPanel ai_ctrl_panel;
            {
                ai_ctrl_panel = new JPanel();
                ai_ctrl_panel.setLayout(new BoxLayout(ai_ctrl_panel, BoxLayout.X_AXIS));
                ai_ctrl_panel.setAlignmentX(0.0f);

                {
                    final JLabel lbl = new JLabel("Think time : ");
                    lbl.setAlignmentX(0.0f);
                    ai_ctrl_panel.add(lbl);
                }
                {
                    think_time_spinner_ = new JSpinner(new SpinnerNumberModel(game_.get_think_time(), 500, Integer.MAX_VALUE, 500));
                    ((JSpinner.NumberEditor) think_time_spinner_.getEditor()).getTextField().setColumns(6);
                    think_time_spinner_.setAlignmentX(0.0f);
                    think_time_spinner_.addChangeListener(new ChangeListener() {
                        public void stateChanged(final ChangeEvent evt) {
                            synchronized (game_.get_mutex()) {
                                game_.set_think_time(((Integer) think_time_spinner_.getValue()).intValue());
                            }
                        }
                    });
                    ai_ctrl_panel.add(think_time_spinner_);
                }
                {
                    final JLabel lbl = new JLabel(" ms");
                    lbl.setAlignmentX(0.0f);
                    ai_ctrl_panel.add(lbl);
                }
            }

            final JPanel east_panel;
            {
                final JPanel east_top_panel = new JPanel();
                east_top_panel.setLayout(new BoxLayout(east_top_panel, BoxLayout.Y_AXIS));

                player_ai_panel.setAlignmentX(0.5f);
                east_top_panel.add(player_ai_panel);

                ai_ctrl_panel.setAlignmentX(0.5f);
                east_top_panel.add(ai_ctrl_panel);

                east_panel = new JPanel(new BorderLayout());
                east_panel.setBackground(Color.WHITE);
                east_panel.add(east_top_panel, BorderLayout.NORTH);
            }

            frame_.getContentPane().add(east_panel, BorderLayout.EAST);
        }

        frame_.getContentPane().add(board_comp_, BorderLayout.CENTER);

        frame_.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent evt) {
/*
                int width = frame_.getWidth();
				int height = frame_.getHeight();

				if (width == height) {
					return;
				}

				final int size = width < height ? width : height;
				frame_.setSize(size, size);
*/
            }

            public void componentHidden(ComponentEvent evt) {
            }

            public void componentMoved(ComponentEvent evt) {
            }

            public void componentShown(ComponentEvent evt) {
            }
        });
        frame_.addWindowListener(new WindowListener() {
            public void windowClosing(WindowEvent evt) {
                try {
                    synchronized (game_.get_mutex()) {
                        game_.close();
                    }

                } catch (Exception ex) {
                    error("Caught this : ", ex);

                } finally {
                    System.exit(0);
                }
            }

            public void windowActivated(WindowEvent e) {
            }

            public void windowClosed(WindowEvent e) {
            }

            public void windowDeactivated(WindowEvent e) {
            }

            public void windowDeiconified(WindowEvent e) {
            }

            public void windowIconified(WindowEvent e) {
            }

            public void windowOpened(WindowEvent e) {
            }
        });

        frame_.setIconImage(load_1_pix("logo64.png"));
        frame_.pack();
        frame_.setVisible(true);

        for (int i = 0; i != 1; ++i) {
            System.gc();
        }

        if (args.length > 0) {
            load_board(new File(args[0]));

        } else {
            new_game();
        }
    }

    private String get_player_type_label(int player) {
        return game_.is_player_AI(player) ? "Machine" : " Human ";
    }

    private void load_pix() throws Exception {
        final BufferedImage[] piece_pixies = new BufferedImage[7 * 2];
        final BufferedImage[] bg_pixies = new BufferedImage[4];

        // white
        piece_pixies[Board.PIECE_PAWN] = load_1_pix("pawn_w.png");
        piece_pixies[Board.PIECE_KNIGHT] = load_1_pix("knight_w.png");
        piece_pixies[Board.PIECE_BISHOP] = load_1_pix("bishop_w.png");
        piece_pixies[Board.PIECE_ROOK] = load_1_pix("rook_w.png");
        piece_pixies[Board.PIECE_QUEEN] = load_1_pix("queen_w.png");
        piece_pixies[Board.PIECE_KING] = load_1_pix("king_w.png");
        piece_pixies[Board.PIECE_NONE] = null;

        // black
        piece_pixies[Board.PIECE_PAWN + 7] = load_1_pix("pawn_b.png");
        piece_pixies[Board.PIECE_KNIGHT + 7] = load_1_pix("knight_b.png");
        piece_pixies[Board.PIECE_BISHOP + 7] = load_1_pix("bishop_b.png");
        piece_pixies[Board.PIECE_ROOK + 7] = load_1_pix("rook_b.png");
        piece_pixies[Board.PIECE_QUEEN + 7] = load_1_pix("queen_b.png");
        piece_pixies[Board.PIECE_KING + 7] = load_1_pix("king_b.png");
        piece_pixies[Board.PIECE_NONE + 7] = null;

        // bgs
        bg_pixies[0] = load_1_pix("empty_w.png");
        bg_pixies[1] = load_1_pix("empty_b.png");
        bg_pixies[2] = load_1_pix("active.png");
        bg_pixies[3] = load_1_pix("played.png");

        // compose bgs with pieces, java2d seems to be done poorly with alpha transparency

        final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        for (int i = 0; i != bg_pixies.length; ++i) {
            final BufferedImage bg_pix = bg_pixies[i];
            for (int j = 0; j != piece_pixies.length; ++j) {
                final BufferedImage bimg = new BufferedImage(bg_pix.getWidth(),
                        bg_pix.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                final Graphics2D gfx2 = (Graphics2D) bimg.getGraphics();
                gfx2.drawImage(bg_pix, 0, 0, null);
                if (piece_pixies[j] != null) {
                    gfx2.drawImage(piece_pixies[j], 0, 0, null);
                }
                final BufferedImage bimg2 = new BufferedImage(bimg.getWidth(),
                        bimg.getHeight(),
                        BufferedImage.TYPE_INT_BGR);
                ((Graphics2D) bimg2.getGraphics()).drawImage(bimg, 0, 0, null);

                final Image img = gc.createCompatibleImage(bimg2.getWidth(),
                        bimg2.getHeight());
                img.getGraphics().drawImage(bimg2, 0, 0, null);
                pixies_[(i * (2 * 7)) + j] = img;
            }
        }
    }

    /*
     * bg == 0 -> background : empty_w
     * bg == 1 -> background : empty_r
     * bg == 2 -> background : active
     * bg == 3 -> background : played
     */
    private Image colored_piece_2_pix(int colored_piece, int bg) {
        int offset = bg * (2 * 7);
        if (colored_piece < 0) {
            offset += 7;
            colored_piece = -colored_piece;
        }
        return pixies_[offset + colored_piece];
    }

    private void update_header() {
        header_lbl_.setText(game_.is_white_move() ? "White" : "Black");
    }

    private void print_possible_moves() {
/*
		final MoveList moves = game_.get_possible_moves(game_.is_white_move());
		strb_.delete(0, strb_.length());
		strb_.append(moves.size_);
		strb_.append(" possible move(s) for ");
		strb_.append(game_.is_white_move() ? "White" : "Black" );
		strb_.append(" :\n");
		moves.print(strb_);
		System.out.println(strb_);

		game_.find_best_move();
*/
    }

    private void load_board(final File file) throws Exception {
        game_.load_board(file);
        board_comp_.repaint();
    }

    private JFileChooser get_file_chooser() {
        if (file_chooser_ == null) {
            file_chooser_ = new JFileChooser();
        }
        return file_chooser_;
    }

    private void new_game() {
        try {
            game_.new_game();
            board_comp_.new_game();
            game_.print_board_eval();

        } catch (Exception ex) {
            error("Caught this", ex);
        }
        frame_.repaint();
    }

    private final Image[] pixies_ = new Image[4 * 2 * 7];
    private final Game game_ = new Game();
    private final JFrame frame_ = new JFrame("Chezzz");
    private final JLabel header_lbl_ = new JLabel();
    private final JButton new_game_btn_ = new JButton("New Game");
    private final JButton switch_btn_ = new JButton("Switch");
    private final JButton load_btn_ = new JButton("Load");
    private final JButton w_player_type_btn_ = new JButton();
    private final JButton b_player_type_btn_ = new JButton();
    private final JButton undo_btn_ = new JButton("Undo");
    private final JSpinner think_time_spinner_;
    private final BoardComponent board_comp_ = new BoardComponent();

    private final StringBuilder strb_ = new StringBuilder();
    private JFileChooser file_chooser_;

    private final class BoardComponent extends Component implements MouseListener {
        public void new_game() {
            clicked_0_.set(-1, -1);
            played_[0].set(-1, -1);
            played_[1].set(-1, -1);
            view_white_ = true;
        }

        // Component extends

        public Dimension getPreferredSize() {
            return new Dimension(32 * 8, 32 * 8);
        }

        public void update(final Graphics gfx) {
            paint(gfx);
        }

        public void paint(final Graphics gfx) {
            super.paint(gfx);

            long tstamp0 = System.nanoTime();

            final Graphics2D gfx2 = (Graphics2D) gfx;
            final FontRenderContext font_ctx = gfx2.getFontRenderContext();
            gfx2.setRenderingHints(rhints_);

            final Dimension size = getSize();
            metrics_.update(size.width, size.height, font_ctx);

            {
                gfx2.setPaint(LABEL_COLOR);
                gfx2.setFont(metrics_.lbl_font_);

                int lbl_x = metrics_.rank_offset_x_;
                int lbl_y = metrics_.rank_offset_y_;
                for (int r = 0; r != 8; ++r) {
                    gfx2.drawString(view_white_ ? Constants.RANKS[7 - r] : Constants.RANKS[r], lbl_x, lbl_y);
                    lbl_y += metrics_.cell_size_;
                }

                lbl_x = metrics_.file_offset_x_;
                lbl_y = metrics_.file_offset_y_;
                for (int f = 0; f != 8; ++f) {
                    gfx2.drawString(view_white_ ? Constants.FILES[f] : Constants.FILES[7 - f], lbl_x, lbl_y);
                    lbl_x += metrics_.cell_size_;
                }
            }

            synchronized (game_.get_mutex()) {
                for (int screen_y = 0; screen_y != 8; ++screen_y) {
                    for (int screen_x = 0; screen_x != 8; ++screen_x) {

                        int board_x;
                        int board_y;
                        if (view_white_) {
                            board_x = screen_x;
                            board_y = 7 - screen_y;

                        } else {
                            board_x = 7 - screen_x;
                            board_y = screen_y;
                        }


                        CellStyle cell_style = CellStyle.NORMAL;
                        if ((board_x == clicked_0_.x_) && (board_y == clicked_0_.y_)) {
                            cell_style = CellStyle.SELECTED;
                        } else {
                            for (Coord played : played_) {
                                if ((board_x == played.x_) && (board_y == played.y_)) {
                                    cell_style = CellStyle.PLAYED;
                                    break;
                                }
                            }
                        }
                        paint_cell(gfx2,
                                screen_x, screen_y,
                                board_x, board_y,
                                cell_style);
                    }
                }
            }

            long tstamp1 = System.nanoTime();

//			System.out.println("paint in " + (tstamp1 - tstamp0) + " ns");
        }

        // MouseListener interface

        public void mouseClicked(MouseEvent evt) {
            if (metrics_.cell_size_ == 0) {
                return;
            }

            final int click_x = evt.getX() - metrics_.board_offset_x_;
            final int click_y = evt.getY() - metrics_.board_offset_y_;
            if ((click_x < 0) || (click_x > metrics_.board_size_)) {
                return;
            }
            if ((click_y < 0) || (click_y > metrics_.board_size_)) {
                return;
            }

            synchronized (game_.get_mutex())    // the scope is probably too wide, could end after is_AIs_turns
            {
                if (game_.is_AIs_turn()) {
                    return;
                }

                final int screen_x = click_x / metrics_.cell_size_;
                final int screen_y = click_y / metrics_.cell_size_;
                final int board_x;
                final int board_y;
                if (view_white_) {
                    board_x = screen_x;
                    board_y = 7 - screen_y;

                } else {
                    board_x = 7 - screen_x;
                    board_y = screen_y;
                }

                //			System.out.println("board clicked at [" + board_x + ", " + board_y + "] " + System.currentTimeMillis());

                if (clicked_0_.x_ == -1) {
                    //				System.out.println("src cell clicked : [" + board_x + ", " + board_y + "]");

                    final int colored_piece = game_.get_piece(board_x, board_y);
                    if (colored_piece == Board.PIECE_NONE) {
                        return;
                    }
                    final boolean piece_white = colored_piece >= 0;
                    final boolean white_move = game_.is_white_move();
                    if ((piece_white && !white_move) || (!piece_white && white_move)) {
                        return;
                    }

                    clicked_0_.set(board_x, board_y);
                    repaint();

                } else {
                    //				System.out.println("dst cell clicked : [" + board_x + ", " + board_y + "]");

                    final int colored_piece = game_.get_piece(board_x, board_y);
                    if (colored_piece != 0) {
                        final boolean piece_white = colored_piece >= 0;
                        final boolean white_move = game_.is_white_move();
                        if ((piece_white && white_move) || (!piece_white && !white_move)) {
                            // selecting another piece
                            clicked_0_.set(board_x, board_y);
                            repaint();
                            return;
                        }
                    }

                    final MoveResult move_res = game_.check_n_move(clicked_0_.x_, clicked_0_.y_,
                            board_x, board_y);
                    if (move_res != null) {
                        player_moved(clicked_0_.x_, clicked_0_.y_, board_x, board_y);

                    } else {
                        System.out.println("move not authorized");
                    }
                }
            }
        }

        public void mouseEntered(MouseEvent evt) {
        }

        public void mouseExited(MouseEvent evt) {
        }

        public void mousePressed(MouseEvent evt) {
        }

        public void mouseReleased(MouseEvent evt) {
        }

        // package

        BoardComponent() {
            played_[0] = new Coord(-1, -1);
            played_[1] = new Coord(-1, -1);
            rhints_ = new RenderingHints(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED);
            addMouseListener(this);
        }

        void toggle_view_white() {
            view_white_ = !view_white_;
        }

        void clear_selections() {
            played_[0].set(-1, -1);
            played_[1].set(-1, -1);
            clicked_0_.set(-1, -1);
        }

        private void paint_cell(final Graphics2D gfx2,
                                final int screen_x,
                                final int screen_y,
                                final int board_x,
                                final int board_y,
                                final CellStyle cell_style) {
            final int d0_x = (screen_x * metrics_.cell_size_) + metrics_.board_offset_x_;
            final int d0_y = (screen_y * metrics_.cell_size_) + metrics_.board_offset_y_;
            final int d1_x = d0_x + metrics_.cell_size_;
            final int d1_y = d0_y + metrics_.cell_size_;

            final int bg;
            switch (cell_style) {

                case NORMAL:
                    bg = (screen_x + (screen_y & 1)) & 1;
                    break;

                case SELECTED:
                    bg = 2;
                    break;

                case PLAYED:
                    bg = 3;
                    break;

                default:
                    throw new IllegalStateException("unexpected cell-style [" + cell_style + "]");
            }

            final int colored_piece = game_.get_piece(board_x, board_y);
            final Image pix = colored_piece_2_pix(colored_piece, bg);
            gfx2.drawImage(pix,
                    d0_x, d0_y, d1_x, d1_y,
                    0, 0, pix.getWidth(null), pix.getHeight(null),
                    null);
        }

        private void player_moved(int x0, int y0, int x1, int y1) {

/*
		{
			strb_.delete(0, strb_.length());
			strb_.append(game_.is_white_move() ? "black : " : "white : "); // the game has already turned
			MoveNotationCodec.encode(	clicked_0_.x_, clicked_0_.y_,
							board_x, board_y,
							move_res, strb_);
			System.out.println(strb_);
		}
*/
            played_[0].set(x0, y0);
            played_[1].set(x1, y1);
            clicked_0_.set(-1, -1);
            repaint();
            update_header();
            game_.print_board_eval();
        }

        private final Coord clicked_0_ = new Coord(-1, -1);
        private final Coord[] played_ = new Coord[2];
        private final RenderingHints rhints_;
        private final BoardMetrics metrics_ = new BoardMetrics();
        private boolean view_white_ = true;
    }

    private static final class BoardMetrics {
        void update(int width, int height, final FontRenderContext font_ctx) {
            if ((width_ == width) && (height_ == height)) {
                return;
            }

            final int screen_offset_x;
            final int screen_offset_y;
            final int size;
            if (width < height) {
                size = width;
                screen_offset_x = 0;
                screen_offset_y = (height - size) / 2;
            } else {
                size = height;
                screen_offset_x = (width - size) / 2;
                screen_offset_y = 0;
            }

            board_size_ = (int) Math.round((float) size * 0.95f);
            lbl_size_ = size - board_size_;
            cell_size_ = board_size_ / 8;
            lbl_font_size_ = (int) Math.round((float) cell_size_ * 0.3f);

            lbl_font_ = new Font("Arial", Font.BOLD, lbl_font_size_);
            {
                final TextLayout layout = new TextLayout("8", lbl_font_, font_ctx);
                final Rectangle2D bounds = layout.getBounds();
                lbl_w_ = (int) Math.round(bounds.getWidth());
                lbl_h_ = (int) Math.round(bounds.getHeight());
            }

            rank_offset_x_ = screen_offset_x + ((lbl_size_ - lbl_w_) / 2);
            board_offset_y_ = screen_offset_y;
            board_offset_x_ = rank_offset_x_ + lbl_size_;
            rank_offset_y_ = board_offset_y_ + (lbl_font_size_ * 2);
            file_offset_x_ = board_offset_x_ + ((cell_size_ - lbl_w_) / 2);
            file_offset_y_ = board_offset_y_ + board_size_ + lbl_font_size_;

            width_ = width;
            height_ = height;
        }

        int width_ = -1;
        int height_ = -1;
        int board_size_;
        int lbl_size_;
        int board_offset_x_;
        int board_offset_y_;
        int cell_size_;
        int rank_offset_x_;
        int rank_offset_y_;
        int file_offset_x_;
        int file_offset_y_;
        int lbl_font_size_;
        Font lbl_font_;
        int lbl_w_;
        int lbl_h_;
    }

    private static enum CellStyle {
        NORMAL,
        SELECTED,
        PLAYED
    }

    private static final class Coord {
        Coord(int x, int y) {
            set(x, y);
        }

        void set(final Coord coord) {
            x_ = coord.x_;
            y_ = coord.y_;
        }

        void set(int x, int y) {
            x_ = x;
            y_ = y;
        }

        int x_;
        int y_;
    }

    private static final Color LABEL_COLOR = new Color(0x88, 0x88, 0x88);
}
