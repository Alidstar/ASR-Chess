/**
 * Created by Earth on 16/4/2558.
 */
public class MoveLog {
    private Piece piece1;
    private Tile tile1;
    private Piece piece2;
    private Tile tile2;

    public MoveLog(Piece piece1, Tile tile1, Piece piece2, Tile tile2) {
        this.piece1 = piece1;
        this.tile1 = tile1;
        this.piece2 = piece2;
        this.tile2 = tile2;
    }

    public Piece getPiece1() {
        return piece1;
    }

    public Tile getTile1() {
        return tile1;
    }

    public Piece getPiece2() {
        return piece2;
    }

    public Tile getTile2() {
        return tile2;
    }

    @Override
    public String toString() {
        return piece1 + " " + tile1 + " " + piece2 + " " + tile2;
    }
}
