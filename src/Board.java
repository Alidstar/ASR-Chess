import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Created by Earth on 12/4/2558.
 */
public class Board extends JFrame {
    private static final ImageIcon BACKGROUND_IMAGE[] = {
            new ImageIcon(Board.class.getResource("image/white_bg.png")),
            new ImageIcon(Board.class.getResource("image/black_bg.png"))
    };
    private static final int WIDTH = BACKGROUND_IMAGE[0].getIconWidth();
    private static final int HEIGHT = BACKGROUND_IMAGE[0].getIconHeight();

    public static boolean freeMove = false;
    public static int turn = 0;

    private static ArrayList<MoveLog> moveLog = new ArrayList<>();

    private static JLayeredPane layerPane = new JLayeredPane();
    private JPanel gridBoard = new JPanel(new GridLayout(8, 8));

    public static Team team[] = new Team[2];
    private static Tile tile[][] = new Tile[8][8];
    private static JLabel background = new JLabel();

    public static Thread inputListener;
    public static Commander commander;

    public static void main(String args[]) {
        new Board();
    }

    public Board() {
        initBoard();
        initPiece();
        setUI();
        commander = new Commander();
        inputListener = new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                while (true) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        System.out.println("Moved!");
                        addTurn();
                    }
                }
            }
        });
        commander.start();
        inputListener.start();
    }

    public static void unhighlightTile() {
        for (int i = 0; i < tile.length; i++) {
            for (int j = 0; j < tile[i].length; j++) {
                tile[i][j].unhighlight();
            }
        }
    }

    public static void showValidMove(ArrayList<String> list, int team) {
        for (String dest : list) {
            getTile(dest).highlight(team);
        }
    }

    public static ArrayList<Piece> getValidMoves(String tile, int color) {
        ArrayList<Piece> list = new ArrayList<>();
        for (Piece p : team[color].getPieces())
            if (p.isValidMove(tile) && p.getTeam() == color)
                list.add(p);
        return list;
    }

    public static String[][] getBoard() {
        String[][] board = new String[8][8];
        for (int i = 0; i < tile.length; i++) {
            for (int j = 0; j < tile[0].length; j++) {
                if (tile[i][j].getPiece() instanceof King)
                    board[i][j] = "K" + tile[i][j].getPiece().getTeam();
                else if (tile[i][j].getPiece() instanceof Queen)
                    board[i][j] = "Q" + tile[i][j].getPiece().getTeam();
                else if (tile[i][j].getPiece() instanceof Bishop)
                    board[i][j] = "B" + tile[i][j].getPiece().getTeam();
                else if (tile[i][j].getPiece() instanceof Knight)
                    board[i][j] = "N" + tile[i][j].getPiece().getTeam();
                else if (tile[i][j].getPiece() instanceof Rook)
                    board[i][j] = "R" + tile[i][j].getPiece().getTeam();
                else if (tile[i][j].getPiece() instanceof Pawn)
                    board[i][j] = "P" + tile[i][j].getPiece().getTeam();
                else board[i][j] = "**";
            }
        }
        return board;
    }

    public static void printBoard() {
        System.out.println("-----------------");
        String[][] board = getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println("  " + (8 - i));
        }
        System.out.println("a  b  c  d  e  f  g  h");
        System.out.println("-----------------");

    }

    public static void addTurn() {
        turn = (turn + 1) % 2;
        background.setIcon(BACKGROUND_IMAGE[turn]);
    }

    public static void addMoveLog(MoveLog ml) {
        moveLog.add(ml);
        inputListener.interrupt();
    }

    public static MoveLog undo() throws IndexOutOfBoundsException {
        if (!moveLog.isEmpty()) addTurn();
        return moveLog.remove(moveLog.size() - 1);
    }

    public static MoveLog lastMoveLog() {
        return moveLog.get(moveLog.size() - 1);
    }

    // -------------- GETTERS SETTERS --------------
    public static Tile getTile(String name) {
        int[] num = Tile.nameToNumber(name);
        return getTile(num[0], num[1]);
    }

    public static Tile getTile(int row, int col) {
        return tile[row][col];
    }

    public static Piece promotePiece(Piece p) {
        commander.setState(Commander.PROMOTE_STATE);
        Icon buttons[] = new Icon[4];
        int teamNumber = p.getTeam();
        String side = team[teamNumber].getColor().toLowerCase();
        buttons[0] = new ImageIcon(Board.class.getResource("image/" + side + "_queen.png"));
        buttons[1] = new ImageIcon(Board.class.getResource("image/" + side + "_bishop.png"));
        buttons[2] = new ImageIcon(Board.class.getResource("image/" + side + "_knight.png"));
        buttons[3] = new ImageIcon(Board.class.getResource("image/" + side + "_rook.png"));
        int answer = -1;
        Piece promoted = null;
        while (answer == -1 && commander.getSelectedChoice() == -1) {
            answer = JOptionPane.showOptionDialog(null, "Select Piece to Promote", "Promote",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, p.getIcon(), buttons, buttons[0]);
        }
        if(commander.getSelectedChoice() >= 0) answer = commander.getSelectedChoice();
        switch (answer) {
            case 0:
                promoted = new Queen(team[teamNumber], p.tile.getName());
                break;
            case 1:
                promoted = new Bishop(team[teamNumber], p.tile.getName());
                break;
            case 2:
                promoted = new Knight(team[teamNumber], p.tile.getName());
                break;
            case 3:
                promoted = new Rook(team[teamNumber], p.tile.getName());
                break;
            default:
                break;
        }
        commander.resetSelectedChoice();
        replacePiece(p, promoted);
        return promoted;
    }

    public static Piece demotePiece(Piece p) {
        System.out.println("demote");
        Piece demote = p.getDemoted();
        replacePiece(p, demote);
        demote.setDemoted(null);
        return demote;
    }

    public static void replacePiece(Piece from, Piece to) {
        int color = from.getTeam();
        Piece[] pieces = team[color].getPieces();
        int index = -1;
        for (int i = 0; i < pieces.length; i++) {
            if (pieces[i].equals(from))
                index = i;
        }

        to.setTile(pieces[index].getTile());
        pieces[index] = to;
        layerPane.remove(from);
        layerPane.add(to, new Integer(2));
    }

    // -------------- PRIVATE METHOD --------------
    private void initBoard() {
        gridBoard.setAlignmentX(Component.CENTER_ALIGNMENT);
        gridBoard.setAlignmentY(Component.CENTER_ALIGNMENT);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                tile[i][j] = new Tile(i, j);
                gridBoard.add(tile[i][j]);
            }
        }
        gridBoard.setBounds(Tile.SIZE, Tile.SIZE, Tile.SIZE * 8, Tile.SIZE * 8);
        layerPane.add(gridBoard, new Integer(1));
    }

    private void initPiece() {
        for (int i = 0; i < 2; i++) {
            team[i] = new Team(i);
            for (int j = 0; j < team[i].getPieces().length; j++) {
                layerPane.add(team[i].getPieces()[j], new Integer(2));
            }
        }
        team[0].setEnemyTeam(team[1]);
    }

    private void setUI() {
        background.setIcon(BACKGROUND_IMAGE[0]);
        background.setBounds(0, 0, WIDTH, HEIGHT);
        layerPane.add(background, new Integer(0));
        layerPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        add(layerPane);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setTitle("Speech Chess");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        requestFocus();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_Z:
                            try {
                                MoveLog ml = undo();

                                Piece p1 = ml.getPiece1();
                                if (p1.getDemoted() != null) p1 = Board.demotePiece(p1);
                                p1.setTile(ml.getTile1());
                                p1.move--;
                                p1.setIsAlive(true);

                                Piece p2 = ml.getPiece2();
                                p2.setTile(ml.getTile2());
                                p2.move--;
                                p2.setIsAlive(true);
                            } catch (Exception e1) {
                            } finally {
                                break;
                            }
                        case KeyEvent.VK_F:
                            freeMove = !freeMove;
                            break;

                        case KeyEvent.VK_P:
                            printBoard();
                            break;

                        case KeyEvent.VK_D:
                            commander.setEnable(!commander.isEnable());
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

}
