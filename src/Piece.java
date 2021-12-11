import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Created by Earth on 12/4/2558.
 */
public abstract class Piece extends JLabel {
    public static final int SIZE = 50;
    private static Piece selectedPiece;
    private static Point selectedPoint;

    protected Tile tile;
    protected Team team;
    protected Piece demoted;
    protected boolean isAlive = true;
    protected int move = 0;

    public Piece(Team team, String tileName) {
        this.team = team;
        setIcon(new ImageIcon(getClass().getResource("image/" + (team.getColor().toLowerCase() + "_" + getClass().getName()).toLowerCase() + ".png")));
        setTile(Board.getTile(tileName));
        addListener();
    }

    public boolean isValidMove(String dest) {
        return getValidMoves().contains(dest);
    }

    public abstract ArrayList<String> getValidMoves();

    public boolean move(String to) {
        if (Board.freeMove || (team.isTurn() && isValidMove(to))) {
            Tile from = tile;
            Tile dest = Board.getTile(to);
            Piece killed = setTile(dest);
            Board.addMoveLog(new MoveLog(this, from, killed, dest));
            check();
            return true;
        }
        return false;
    }

    public void check() {
        Board.commander.setState(Commander.CHECK_STATE);
        Team enemy = team.getEnemyTeam();
        if(enemy.isCheckmate())
            JOptionPane.showMessageDialog(null, "Checkmate!  " + team.getColor() + " player win", "Checkmate", JOptionPane.ERROR_MESSAGE, enemy.getChecker().getIcon());
        else if (enemy.cannotMove())
            JOptionPane.showMessageDialog(null, "Draw!  " + enemy.getColor() + " has no valid moves", "Draw", JOptionPane.ERROR_MESSAGE, enemy.getKing().getIcon());
        else if (enemy.isCheck())
            JOptionPane.showMessageDialog(null, "Check!", "Check", JOptionPane.WARNING_MESSAGE, enemy.getChecker().getIcon());
        else
            Board.commander.setState(Commander.NORMAL_STATE);
        move++;
    }

    public int getTeam() {
        return team.getTeam();
    }

    public boolean isEnemy(Piece p) {
        return p.getTeam() != getTeam();
    }

    public void setIsAlive(boolean isAlive) {
        this.isAlive = isAlive;
        setVisible(isAlive);
    }

    public Tile getTile() {
        return tile;
    }

    public Piece setTile(Tile tile) {
        if (this.tile != null) this.tile.setPiece(null);
        this.tile = tile;
        Piece killed = tile.setPiece(this);
        if (killed != null)
            killed.setIsAlive(false);

        Point loc = tile.getTileLocation();
        setBounds(loc.x, loc.y, SIZE, SIZE);

        return killed;
    }

    public Piece getDemoted() {
        return demoted;
    }

    public void setDemoted(Piece demoted) {
        this.demoted = demoted;
    }

    public void undoMove() {
        move--;
    }

    @Override
    public String toString() {
        return team.getColor() + " " + getClass().getName() + " at " + tile;
    }

    private void addListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Board.showValidMove(getValidMoves(), getTeam());
                selectedPiece = (Piece) e.getSource();
                selectedPoint = e.getPoint();

                if (e.getButton() == MouseEvent.BUTTON3) {
                    System.out.println(selectedPiece);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Board.unhighlightTile();
                Point currentLocation = getLocation();
                Point mouseLocation = new Point(currentLocation.x + selectedPoint.x,
                        currentLocation.y + selectedPoint.y);
                int offset = (Tile.SIZE - Piece.SIZE) / 2;
                int index = Tile.SIZE - 1;
                int dropRow = (mouseLocation.y - offset) / index - 1;
                int dropCol = (mouseLocation.x - offset) / index - 1;
                try {
                    String to = Tile.numberToName(dropRow, dropCol);
                    if (!to.equals(selectedPiece.tile.getName()) && selectedPiece.move(to)) ;
                    else throw new ArrayIndexOutOfBoundsException();
                } catch (ArrayIndexOutOfBoundsException e1) {
                    selectedPiece.setLocation(selectedPiece.tile.getTileLocation());
                }
                selectedPiece = null;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point click = e.getPoint();
                Point currentLocation = getLocation();
                Point newLocation = new Point(currentLocation.x + click.x - selectedPoint.x,
                        currentLocation.y + click.y - selectedPoint.y);
                if (selectedPiece != null) {
                    selectedPiece.setLocation(newLocation);
                }
                selectedPiece.repaint();
            }
        });
    }
}
