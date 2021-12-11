import java.util.ArrayList;

/**
 * Created by Earth on 17/4/2558.
 */
public class Team {
    private Team enemyTeam;

    private int team;
    private String color;
    private int start;

    private King king;
    private Piece[] pieces = new Piece[16];

    public Team(int team) {
        this.team = team;
        color = team == 0 ? "White" : "Black";
        this.start = 7 - team * 7;

        int backRow = team * 7;
        int forward = team * -2 + 1;

        pieces[0] = new Rook(this, "A" + (backRow + 1));
        pieces[1] = new Knight(this, "B" + (backRow + 1));
        pieces[2] = new Bishop(this, "C" + (backRow + 1));
        pieces[3] = new Queen(this, "D" + (backRow + 1));
        pieces[4] = new King(this, "E" + (backRow + 1));
        pieces[5] = new Bishop(this, "F" + (backRow + 1));
        pieces[6] = new Knight(this, "G" + (backRow + 1));
        pieces[7] = new Rook(this, "H" + (backRow + 1));

        for (int i = 0; i < 8; i++) {
            pieces[i + 8] = new Pawn(this, (((char) ('A' + i)) + "" + (backRow + forward + 1)));
        }

        king = (King) pieces[4];
    }

    public void removeDangerousMove(String from, ArrayList<String> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (isDangerousMove(from, list.get(i)))
                list.remove(i);
        }
    }

    public boolean isDangerousMove(String from, String to) {
        // simulate board
        String[][] board = Board.getBoard();
        int numFrom[] = Tile.nameToNumber(from);
        int numTo[] = Tile.nameToNumber(to);
        board[numTo[0]][numTo[1]] = board[numFrom[0]][numFrom[1]];
        board[numFrom[0]][numFrom[1]] = "**";

        int row, col;
        if (board[numTo[0]][numTo[1]].charAt(0) == 'K') {
            row = numTo[0];
            col = numTo[1];
        } else {
            row = king.getTile().getRow();
            col = king.getTile().getCol();
        }

        if (isDirectCheck(board, row, col)) return true;
        else if (isKnightCheck(board, row, col)) return true;
        else return isPawnCheck(board, row, col);
    }

    public boolean isCheck() {
        return isDangerousMove("A1", "A1");
    }

    public Piece getChecker() {
        ArrayList<Piece> list = Board.getValidMoves(king.getTile().getName(), enemyTeam.getTeam());
        if(list.isEmpty()) return null;
        return list.get(0);
    }

    public boolean cannotMove() {
        for(Piece p :pieces) {
            if(!p.getValidMoves().isEmpty())
                return false;
        }
        return true;
    }

    public boolean isCheckmate() {
        return isCheck() && cannotMove();
    }

    // -------------- GETTERS SETTERS --------------

    public Team getEnemyTeam() {
        return enemyTeam;
    }

    public void setEnemyTeam(Team enemyTeam) {
        if (this.enemyTeam == null || !this.enemyTeam.equals(enemyTeam)) {
            this.enemyTeam = enemyTeam;
            enemyTeam.setEnemyTeam(this);
        }
    }

    public boolean isEnemy(Piece piece) {
        return team != piece.getTeam();
    }

    public int getTeam() {
        return team;
    }

    public String getColor() {
        return color;
    }

    public int getStart() {
        return start;
    }

    public boolean isTurn() {
        return Board.turn == team;
    }

    public Piece[] getPieces() {
        return pieces;
    }

    public ArrayList<Piece> getPiece(Class type, String validMoveTo) {
        ArrayList<Piece> list = new ArrayList<>();
        for (Piece p : pieces) {
            if (p.getClass() == type) {
                if (validMoveTo == null || p.isValidMove(validMoveTo))
                    list.add(p);
            }
        }
        return list;
    }

    public King getKing() {
        return king;
    }

    @Override
    public String toString() {
        return color + " Team";
    }

    // ----------------- PRIVATE METHOD -------------------
    private boolean isDirectCheck(String[][] board, int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                try {
                    for (int power = 1; power < 8; power++) {
                        String destPiece = board[row + i * power][col + j * power];
                        if (destPiece.equals("**")) continue;
                        char destType = destPiece.charAt(0);
                        int destTeam = destPiece.charAt(1) - '0';
                        if (team != destTeam) { // isEnemy
                            if (Math.abs(i + j) == 1 && (destType == 'Q' || destType == 'R')) {
                                return true;
                            } else if (Math.abs(i + j) != 1 && (destType == 'Q' || destType == 'B')) {
                                return true;
                            } else if (power == 1 && destType == 'K') {
                                return true;
                            } else break;
                        } else break;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                }
            }
        }
        return false;
    }

    private boolean isKnightCheck(String[][] board, int row, int col) {
        for (int i = -2; i <= 2; i += 4) {
            for (int j = -1; j <= 1; j += 2) {
                for (int k = 0; k < 2; k++) {
                    try {
                        String destPiece = board[row + i][col + j];
                        if (!destPiece.equals("**")) {
                            char destType = destPiece.charAt(0);
                            int destTeam = destPiece.charAt(1) - '0';
                            if (team != destTeam && destType == 'N') {
                                return true;
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    int tmp = i;
                    i = j;
                    j = tmp;
                }
            }
        }
        return false;
    }

    private boolean isPawnCheck(String[][] board, int row, int col) {
        for (int i = -1; i <= 1; i += 2) {
            try {
                String destPiece = board[row + (team * 2 - 1)][col + i];
                if (destPiece.equals("**")) continue;
                char destType = destPiece.charAt(0);
                int destTeam = destPiece.charAt(1) - '0';
                if (team != destTeam && destType == 'P') {
                    return true;
                } else break;
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
        }
        return false;
    }
}
