import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by Earth on 13/4/2558.
 */
public class Pawn extends Piece {

    private final int forward;
    private boolean isDouble;

    public Pawn(Team team, String tileName) {
        super(team, tileName);
        forward = getTeam() * 2 - 1;
    }

    @Override
    public boolean move(String to) {
        Tile destTile = Board.getTile(to);
        isDouble = Math.abs(destTile.getRow() - tile.getRow()) == 2;
        boolean isKill = Math.abs(destTile.getCol() - tile.getCol()) == 1;
        boolean isPass = isKill && destTile.getPiece() == null;
        if(isPass) {
            if (Board.freeMove || (team.isTurn() && isValidMove(to))) {
                Tile victimTile = Board.getTile(tile.getRow(), destTile.getCol());
                Piece victimPiece = victimTile.getPiece();
                if(victimPiece instanceof Pawn && ((Pawn) victimPiece).isDouble()) {
                    MoveLog ml = new MoveLog(this, tile, victimPiece, victimTile);
                    victimPiece.setIsAlive(false);
                    victimPiece.move++;
                    setTile(destTile);
                    move++;
                    Board.addMoveLog(ml);
                }
                return true;
            }
        } else if(destTile.getRow() == team.getEnemyTeam().getStart()) {
            if (Board.freeMove || (team.isTurn() && isValidMove(to))) {
                Piece promoted = Board.promotePiece(this);
                promoted.setDemoted(this);
                Tile from = tile;
                Tile dest = Board.getTile(to);
                Piece killed = promoted.setTile(dest);
                Board.addMoveLog(new MoveLog(promoted, from, killed, dest));
                promoted.check();
                return true;
            }
        } else {
            return super.move(to);
        }
        return false;
    }

    @Override
    public ArrayList<String> getValidMoves() {
        ArrayList<String> validMove = new ArrayList<>();
        if(!isAlive) return validMove;
        int row = tile.getRow();
        int col = tile.getCol();

        // walk
        if (team.isTurn()) {
            try {
                for (int i = 1; i <= 2; i++) {
                    Tile destTile = Board.getTile(row + forward * i, col);
                    if (destTile.getPiece() == null) validMove.add(destTile.getName());
                    else break;
                    if (move > 0) break;
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }

        // kill
        for (int i = -1; i <= 1; i += 2) {
            try {
                Tile destTile = Board.getTile(row + forward, col + i);
                Piece destPiece = destTile.getPiece();
                if ((destPiece != null && isEnemy(destPiece)) || !team.isTurn()) {
                    validMove.add(destTile.getName());
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }

        // en passant
        for (int i = -1; i <= 1; i += 2) {
            try {

                Tile beside = Board.getTile(row, col + i);
                Piece besidePiece = beside.getPiece();
                if (besidePiece != null && besidePiece instanceof Pawn &&
                        isEnemy(besidePiece) && ((Pawn) besidePiece).isDouble()) {
                    Tile destTile = Board.getTile(row + forward, col + i);
                    if (destTile.getPiece() == null) validMove.add(destTile.getName());
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }

        team.removeDangerousMove(tile.getName(), validMove);
        return validMove;
    }

    private void promote() {
        Icon buttons[] = new Icon[4];
        String side = team.getColor().toLowerCase();
        buttons[0] = new ImageIcon(getClass().getResource("image/" + side + "_queen.png"));
        buttons[1] = new ImageIcon(getClass().getResource("image/" + side + "_bishop.png"));
        buttons[2] = new ImageIcon(getClass().getResource("image/" + side + "_knight.png"));
        buttons[3] = new ImageIcon(getClass().getResource("image/" + side + "_rook.png"));
        int answer = -1;
        Piece promoted = null;
        while (answer < 0) {
            answer = JOptionPane.showOptionDialog(null, "Select Piece to Promote", "Promote",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, getIcon(), buttons, buttons[0]);
            switch (answer) {
                case 0:
                    promoted = new Queen(team, tile.getName());
                    break;
                case 1:
                    promoted = new Bishop(team, tile.getName());
                    break;
                case 2:
                    promoted = new Knight(team, tile.getName());
                    break;
                case 3:
                    promoted = new Rook(team, tile.getName());
                    break;
                default:
                    break;
            }
        }
        Board.replacePiece(this, promoted);
    }

    public boolean isDouble() {
        return isDouble && Board.lastMoveLog().getPiece1().equals(this);
    }
}
