import javax.swing.*;
import java.awt.*;

/**
 * Created by Earth on 17/4/2558.
 */
public class Tile extends JLabel {
    public final static int SIZE = 60;
    public final static Color WHITE_TILE_N = new Color(232, 189, 111);
    public final static Color BLACK_TILE_N = new Color(207, 121, 15);
    public final static Color WHITE_TILE_M = Color.green;
    public final static Color BLACK_TILE_M = Color.green;
    public final static Color WHITE_TILE_E = Color.red;
    public final static Color BLACK_TILE_E = Color.red;

    private final int color;
    private final int row;
    private final int col;
    private Piece piece;

    public Tile(int row, int col) {
        this.row = row;
        this.col = col;
        color = (row + col) % 2;
        setName(numberToName(row, col));

        setPreferredSize(new Dimension(SIZE, SIZE));
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createStrokeBorder(new BasicStroke(1)));
        setOpaque(true);

        unhighlight();
    }

    public void unhighlight() {
        setBackground(color == 0 ? WHITE_TILE_N : BLACK_TILE_N);
    }

    public void highlight(int team) {
        if (team == Board.turn)
            setBackground(color == 0 ? WHITE_TILE_M : BLACK_TILE_M);
        else
            setBackground(color == 0 ? WHITE_TILE_E : BLACK_TILE_E);

    }

    @Override
    public String toString() {
        return getName();
    }

    // --------------- GETTERS SETTERS ----------------

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Piece getPiece() {
        return piece;
    }

    public Piece setPiece(Piece piece) {
        Piece killed = this.piece;
        this.piece = piece;
        return killed;
    }

    public Point getTileLocation() {
        int offset = (SIZE - Piece.SIZE) / 2;
        return new Point((col + 1) * SIZE + offset, (row + 1) * SIZE + offset);
    }

    // --------------- STATIC METHOD ----------------
    public static int[] nameToNumber(String name) {
        if (name.charAt(0) < 'A' || name.charAt(0) > 'H' || name.charAt(1) < '1' || name.charAt(1) > '8')
            throw new ArrayIndexOutOfBoundsException();
        return new int[]{'8' - name.charAt(1), name.charAt(0) - 'A'};
    }

    public static String numberToName(int row, int col) {
        if (row < 0 || row > 7 || col < 0 || col > 7)
            throw new ArrayIndexOutOfBoundsException();
        return (char) ('A' + col) + "" + (8 - row);
    }
}
