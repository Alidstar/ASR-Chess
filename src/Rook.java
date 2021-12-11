import java.util.ArrayList;

/**
 * Created by Earth on 13/4/2558.
 */
public class Rook extends Piece {

    public Rook(Team team, String tileName) {
        super(team, tileName);
    }

    @Override
    public ArrayList<String> getValidMoves() {
        ArrayList<String> validMove = new ArrayList<>();
        if(!isAlive) return validMove;
        int row = tile.getRow();
        int col = tile.getCol();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (Math.abs(i + j) != 1) continue;
                try {
                    for (int power = 1; power < 8; power++) {
                        Tile destTile = Board.getTile(row + i * power, col + j * power);
                        Piece destPiece = destTile.getPiece();
                        if (destPiece == null)
                            validMove.add(destTile.getName());
                        else {
                            if (!team.isTurn() || isEnemy(destPiece))
                                validMove.add(destTile.getName());
                            break;
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                }
            }
        }
        team.removeDangerousMove(tile.getName(), validMove);
        return validMove;
    }
}
