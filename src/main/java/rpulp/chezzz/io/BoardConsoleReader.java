package rpulp.chezzz.io;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import rpulp.chezzz.board.BoardBuilder;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public final class BoardConsoleReader extends DefaultHandler {

    public void read(final File file,
                     final BoardBuilder bbuilder) throws Exception {
        final FileInputStream fin = new FileInputStream(file);
        try {
            read(fin, bbuilder);
        } finally {
            fin.close();
        }
    }

    public void read(final InputStream in,
                     final BoardBuilder bbuilder) throws Exception {
        bbuilder_ = bbuilder;
        try {
            row_idx_ = -1;

            final SAXParserFactory factory = SAXParserFactory.newInstance();
            final SAXParser parser = factory.newSAXParser();
            final XMLReader xreader = parser.getXMLReader();

            final InputSource insrc = new InputSource(in);
            insrc.setEncoding("UTF-8");
            xreader.setContentHandler(this);
            xreader.setErrorHandler(this);
            xreader.parse(insrc);

            bbuilder_.close();

        } finally {
            bbuilder_ = null;
        }
    }

    //_________________________________________
    // SAX DocumentHandler methods
    //_________________________________________

/*
    public void		startDocument () throws SAXException {}
	public void		endDocument () throws SAXException {}
*/

    public void characters(final char buf[],
                           final int offset,
                           final int len) throws SAXException {
        strb_.append(buf, offset, len);
    }

    public void startElement(final String namespaceURI,
                             final String simple,
                             final String qualif,
                             final Attributes attrs) throws SAXException {
        final String name = ((simple == null) || (simple.length() == 0)) ? qualif : simple;
        if ("row".equals(name)) {
            strb_.delete(0, strb_.length());
        }
    }

    public void endElement(final String namespaceURI,
                           final String simple,
                           final String qualif) throws SAXException {
        final String name = ((simple == null) || (simple.length() == 0)) ? qualif : simple;

        if ("row".equals(name)) {
            ++row_idx_;
            final String row = strb_.toString();
            for (int col = 0; col != 8; ++col) {
                setPiece(row_idx_, col, row.charAt(col));
            }
        }
    }

    //_________________________________________
    // SAX ErrorHandler methods
    //_________________________________________

    public void error(final SAXParseException ex) {
        throw new RuntimeException(ex);
    }

    public void fatalError(final SAXParseException ex) {
        throw new RuntimeException(ex);
    }

    public void warning(final SAXParseException ex) {
        throw new RuntimeException(ex);
    }

    //_______________

    private void setPiece(int row, int col, char car) {
        switch (car) {
            case '.':
                bbuilder_.set_empty(row, col);
                break;

            case 'P':
                bbuilder_.set_piece(row, col, BoardBuilder.Piece.PAWN, BoardBuilder.Color.WHITE);
                break;

            case 'N':
                bbuilder_.set_piece(row, col, BoardBuilder.Piece.KNIGHT, BoardBuilder.Color.WHITE);
                break;

            case 'B':
                bbuilder_.set_piece(row, col, BoardBuilder.Piece.BISHOP, BoardBuilder.Color.WHITE);
                break;

            case 'R':
                bbuilder_.set_piece(row, col, BoardBuilder.Piece.ROOK, BoardBuilder.Color.WHITE);
                break;

            case 'Q':
                bbuilder_.set_piece(row, col, BoardBuilder.Piece.QUEEN, BoardBuilder.Color.WHITE);
                break;

            case 'K':
                bbuilder_.set_piece(row, col, BoardBuilder.Piece.KING, BoardBuilder.Color.WHITE);
                break;

            case 'p':
                bbuilder_.set_piece(row, col, BoardBuilder.Piece.PAWN, BoardBuilder.Color.BLACK);
                break;

            case 'n':
                bbuilder_.set_piece(row, col, BoardBuilder.Piece.KNIGHT, BoardBuilder.Color.BLACK);
                break;

            case 'b':
                bbuilder_.set_piece(row, col, BoardBuilder.Piece.BISHOP, BoardBuilder.Color.BLACK);
                break;

            case 'r':
                bbuilder_.set_piece(row, col, BoardBuilder.Piece.ROOK, BoardBuilder.Color.BLACK);
                break;

            case 'q':
                bbuilder_.set_piece(row, col, BoardBuilder.Piece.QUEEN, BoardBuilder.Color.BLACK);
                break;

            case 'k':
                bbuilder_.set_piece(row, col, BoardBuilder.Piece.KING, BoardBuilder.Color.BLACK);
                break;

            default:
                throw new IllegalStateException("Unexpected char [" + car + "] at [" + row + ", " + col + "]");
        }
    }

    private final StringBuilder strb_ = new StringBuilder();
    private int row_idx_;
    private BoardBuilder bbuilder_;
}
