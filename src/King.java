import java.util.ArrayList;

/**
 * Created by Earth on 13/4/2558.
 */
public class King extends Piece {

    public King(Team team, String tileName) {
        super(team, tileName);
    }

    @Override
    public boolean move(String dest) {
        Tile destTile = Board.getTile(dest);
        int distance = destTile.getCol() - tile.getCol();
        if(Math.abs(distance) == 2) {
            if (Board.freeMove || (team.isTurn() && isValidMove(dest))) {
                int col = (distance + 2) / 4 * 7;
                Piece rook = Board.getTile(team.getStart(), col).getPiece();
                if (rook != null && rook instanceof Rook && rook.move == 0) {
                    MoveLog ml = new MoveLog(this, tile, rook, rook.getTile());
                    col = (distance + 2) / 2 + 3;
                    rook.setTile(Board.getTile(team.getStart(), col));
                    rook.move++;
                    col = distance + 4;
                    setTile(Board.getTile(team.getStart(), col));
                    move++;
                    Board.addMoveLog(ml);
                    return true;
                }
            }
            return false;
        }   else return super.move(dest);
    }

    @Override
    public ArrayList<String> getValidMoves() {
        ArrayList<String> validMove = new ArrayList<>();
        if(!isAlive) return validMove;
        int row = tile.getRow();
        int col = tile.getCol();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                try {
                    Tile destTile = Board.getTile(row + i, col + j);
                    Piece destPiece = destTile.getPiece();
                    if (!team.isTurn() || destPiece == null ||
                            (destPiece != null && isEnemy(destPiece))) {
                        validMove.add(destTile.getName());
                    }

                } catch (ArrayIndexOutOfBoundsException e) {
                }
            }
        }

        team.removeDangerousMove(tile.getName(), validMove);

        // castling
        if(move == 0 && !team.isCheck()) {
            for (int ccol = 0, dir = 1, rcol = 2; ccol < 8; ccol+= 7, dir-=2, rcol+=4) {
                Piece rook = Board.getTile(team.getStart(), ccol).getPiece();
                if(rook != null && rook.move == 0) {
                    boolean canCastle = true;
                    for(int j = ccol + dir; j != tile.getCol(); j+=dir) {
                        Tile obstacleTile = Board.getTile(team.getStart(), j);
                        Piece obstacle = obstacleTile.getPiece();
                        if(obstacle != null ||
                                team.isDangerousMove(tile.getName(), obstacleTile.getName())) {
                            canCastle = false;
                            break;
                        }
                    }
                    if(canCastle)
                        validMove.add(Board.getTile(team.getStart(), rcol).getName());
                }
            }
        }
        return validMove;
    }
}
