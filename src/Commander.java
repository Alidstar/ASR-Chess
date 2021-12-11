import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Created by Earth on 12/4/2558.
 */
public class Commander extends Thread {

    public static final int NORMAL_STATE = 0;
    public static final int PROMOTE_STATE = 1;
    public static final int POSSIBLE2_STATE = 2;
    public static final int CHECK_STATE = 3;

    private boolean enable = true;

    private int state = NORMAL_STATE;

    private ArrayList<Piece> possiblePiece;
    private int selectedChoice = -1;
    private String savedDest;

    @Override
    public void run() {
//        fakeCommand();
//        htkCommand("C:/Users/User/Desktop/ASRProject/");
        htkCommand("");
    }

    private void fakeCommand() {
        Scanner keyboard = new Scanner(System.in);
        while (true) {
            if(state == NORMAL_STATE)
                System.out.println("Enter command!");
            else if(state == PROMOTE_STATE)
                System.out.println("Promote to");
            else if(state == POSSIBLE2_STATE)
                System.out.println("Choose one");
            String line = keyboard.nextLine();
            line = "SENT-START " + line + " SENT-END";
            runCommand(line);
        }
    }

    private void htkCommand(String drive) {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", drive + "HVite -H " + drive + "am/hmm_5/newMacros -C " + drive + "config/liveRecog.config -w " + drive + "lm/words.wdnet " + drive + "config/words.dict " + drive + "config/monophn.list");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line != null && enable) {
                    System.out.println(line);
                    runCommand(line);
                    if(state == NORMAL_STATE) JOptionPane.getRootFrame().dispose();
                }
            }
        } catch (IOException e) {
        }
    }

    private void runCommand(String line) {
        if( state == CHECK_STATE) {
            state = NORMAL_STATE;
            JOptionPane.getRootFrame().dispose();
            return;
        }
        if (line.indexOf("SENT-START") == -1) return;
        line = line.substring(11, line.indexOf(" SENT-END"));
        String args[] = line.split(" ");
        try {
            if (state == NORMAL_STATE) {
                switch (args[0]) {
                    case "pawn":
                        movePiece(Pawn.class, decode(args[1]) + decode(args[2]));
                        return;
                    case "rook":
                        movePiece(Rook.class, decode(args[1]) + decode(args[2]));
                        return;
                    case "knight":
                        movePiece(Knight.class, decode(args[1]) + decode(args[2]));
                        return;
                    case "bishop":
                        movePiece(Bishop.class, decode(args[1]) + decode(args[2]));
                        return;
                    case "queen":
                        movePiece(Queen.class, decode(args[1]) + decode(args[2]));
                        return;
                    case "king":
                        movePiece(King.class, decode(args[1]) + decode(args[2]));
                        return;
                    case "castle":
                        int direction = 0;
                        if (args[1].equals("king")) {
                            direction = 2;
                        } else if (args[1].equals("queen")) {
                            direction = -2;
                        }
                        Tile tile = Board.team[Board.turn].getKing().getTile();
                        movePiece(King.class, Tile.numberToName(tile.getRow(), tile.getCol() + direction));
                        return;
                    default:
                        System.out.println("Invalid Command");
                        break;
                }
            } else if (state == PROMOTE_STATE) {
                if (args.length != 1) return;
                promote(args[0]);
                JOptionPane.getRootFrame().dispose();
                state = NORMAL_STATE;
            } else if (state == POSSIBLE2_STATE) {
                if (args.length != 2) return;
                selectPossible(decode(args[0]) + decode(args[1]));
                state = NORMAL_STATE;
            }
        } catch (NullPointerException en) {
        } catch (ArrayIndexOutOfBoundsException ea) {
            System.out.println("Invalid Command");
        } catch (InputMismatchException em) {
            System.out.println("Input Mismatch");
        }
    }

    private String decode(String word) {
        if (word.length() == 1) {
            return word.toUpperCase();
        }
        String number[] = {"one", "two", "three", "four", "five", "six", "seven", "eight"};
        for (int i = 0; i < number.length; i++) {
            if (word.equals(number[i])) return (i + 1) + "";
        }
        return null;
    }

    private void movePiece(Class type, String destTile) {
        ArrayList<Piece> list = Board.team[Board.turn].getPiece(type, destTile);
        if (list.isEmpty()) {
            System.out.println("Invalid Move");
        } else if (list.size() == 1) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    list.get(0).move(destTile);
                }
            }).start();
        } else {
            savedDest = destTile;
            possiblePiece = list;
            String[] choices = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                choices[i] = list.get(i).getTile().getName();
            }

            state = POSSIBLE2_STATE;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int answer = JOptionPane.showOptionDialog(null, "Select preferred piece to move", "Multiple available move",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, list.get(0).getIcon(), choices, choices[0]);
                    if(answer == -1) {
                        state = NORMAL_STATE;
                        return;
                    }
                    if(state == POSSIBLE2_STATE) {
                        if(answer >= 0) selectedChoice = answer;
                        else if(selectedChoice >= 0) answer = selectedChoice;
                        selectPossible(choices[answer]);
                        state = NORMAL_STATE;
                    }
                }
            }).start();
        }
    }

    private boolean promote(String type) throws InputMismatchException {
        switch (type) {
            case "rook":
                selectedChoice = 3;
                return true;
            case "knight":
                selectedChoice = 2;
                return true;
            case "bishop":
                selectedChoice = 1;
                return true;
            case "queen":
                selectedChoice = 0;
                return true;
            default:
                throw new InputMismatchException();
        }
    }

    private void selectPossible(String selectedTile) throws ArrayIndexOutOfBoundsException {
        Tile tile = Board.getTile(selectedTile);
        Piece selected = tile.getPiece();
        int index = possiblePiece.indexOf(selected);
        if (selected == null || index == -1)
            throw new ArrayIndexOutOfBoundsException();
        if(selectedChoice == -1) {
            JOptionPane.getRootFrame().dispose();
        }
        resetSelectedChoice();
        new Thread(new Runnable() {
            @Override
            public void run() {
                selected.move(savedDest);
            }
        }).start();
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setState(int state) {
        this.state = state;
    }

    public synchronized int getSelectedChoice() {
        return selectedChoice;
    }

    public void resetSelectedChoice() {
        selectedChoice = -1;
    }
}
