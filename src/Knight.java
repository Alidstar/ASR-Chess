import java.util.ArrayList;

/**
 * Created by Earth on 13/4/2558.
 */
public class Knight extends Piece {

    public Knight(Team team, String tileName) {
        super(team, tileName);
    }

    @Override
    public ArrayList<String> getValidMoves() {
        ArrayList<String> validMove = new ArrayList<>();
        if(!isAlive) return validMove;
        int row = tile.getRow();
        int col = tile.getCol();
        for (int i = -2; i <= 2; i += 4) {
            for (int j = -1; j <= 1; j += 2) {
                for (int k = 0; k < 2; k++) {
                    try {
                        Tile destTile = Board.getTile(row + i, col + j);
                        Piece destPiece = destTile.getPiece();
                        if (!team.isTurn() || destPiece == null ||
                                (destPiece != null && isEnemy(destPiece)))
                            validMove.add(destTile.getName());
                    } catch (ArrayIndexOutOfBoundsException e) {

                    }
                    int tmp = i;
                    i = j;
                    j = tmp;
                }
            }
        }
        team.removeDangerousMove(tile.getName(), validMove);
        return validMove;
    }
}
