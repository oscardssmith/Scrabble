        /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scrabble;

import Dawg.Dawg;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 *
 * @author oscardssmith
 */
public class Board extends JPanel implements ActionListener {
    private char[][] board;
    private final Tile[][] tiles;
    private Deque<char[][]> boardList;
    private static Dawg dawg;
    public Point startCoord = null;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BRIGHT_RED = "\u001B[31;1m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_BRIGHT_BLUE = "\u001B[36;1m";

    public Board(Dawg dawg) {
        super(new GridLayout(15, 15));
        Board.dawg = dawg;
        board = new char[][]{{5, 0, 0, 1, 0, 0, 0, 5, 0, 0, 0, 1, 0, 0, 5},
                             {0, 3, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3, 0},
                             {0, 0, 3, 0, 0, 0, 1, 0, 1, 0, 0, 0, 3, 0, 0},
                             {1, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 1},
                             {0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
                             {0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0},
                             {0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0},
                             {5, 0, 0, 1, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 5},
                             {0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0},
                             {0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2, 0},
                             {0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0},
                             {1, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 1},
                             {0, 0, 3, 0, 0, 0, 1, 0, 1, 0, 0, 0, 3, 0, 0},
                             {0, 3, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3, 0},
                             {5, 0, 0, 1, 0, 0, 0, 5, 0, 0, 0, 1, 0, 0, 5},};
        boardList = new ArrayDeque<>();
        boardList.addLast(board);

        tiles = new Tile[board.length][board.length];
        setBorder(new TitledBorder(null, "Board", TitledBorder.CENTER, TitledBorder.ABOVE_TOP));

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                Tile tile = new Tile(i, j, board[i][j]);
                tiles[i][j] = tile;
                add(tile);
                tile.addActionListener(this);
            }
        }
    }

    public void update(char[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                tiles[i][j].setValue(board[i][j]);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        startCoord = ((Tile) e.getSource()).getCoord();
    }

    public char[][] getBoard() {
        return board;
    }

    public char get(int row, int col) {
        return  board[row][col];
    }

    public void undo() {
        boardList.removeLast();
        if (boardList.isEmpty()) {
            throw new java.util.EmptyStackException();
        }
        board = boardList.getLast();
        print();
        update(board);
    }

    public boolean isValidCoord(Point coord) {
        return isValidCoord(coord.y, coord.x);
    }
    public boolean isValidCoord(int row, int col) {
        return !(row < 0 || col < 0 || row >= board.length || col >= board.length);
    }

    /* retuns the number of points this move would make without making it.*/
    public int dryMove(Point start, String word, Point dir) {
        Point tmp = new Point(start);
        tmp.translate(dir.x * (word.length() - 1), dir.y * (word.length() - 1));
        if (!(isValidCoord(start) && isValidCoord(tmp))) {
            return -100000;
        }
        if (!dawg.contains(word)) {
            return -100000;
        }

        start = new Point(start);
        start.translate(-dir.x, -dir.y);
        if (isValidCoord(start) && board[start.y][start.x] >= 'A') {
            return -100000;
        }
        start.translate(dir.x, dir.y);

        int points = 0;
        int multiplier = getMultiplier(start, word, dir);
        boolean hasHooked = false;
        int lettersUsed = 0;
        for (char letter : word.toCharArray()) {
            char prevSquare = board[start.y][start.x];
            if (prevSquare < 'A') {
                lettersUsed += 1;
            } else if (prevSquare != letter) {
                return -100000;
            }

            int score = getLetterScore(prevSquare, letter, dir, start, multiplier);
            if (score < 0) {
                return -100000;
            }
            points += score;
            hasHooked = hasHooked || hasHooked(start, prevSquare, dir);

            start.translate(dir.x, dir.y);
        }

        if (!(hasHooked && lettersUsed > 0)) {
            return -100000;
        }
        if (isValidCoord(start) && board[start.y][start.x] >= 'A') {
            return -100000;
        }
        return points + (lettersUsed == 7 ? 50 : 0);
    }

    public Tupple<Integer, char[]> move(Point coord, String word, Point dir) {
        coord = new Point(coord);
        if (word == null) {
            return error("Word is null");
        }

        Point tmp = new Point(coord);
        tmp.translate(dir.x * (word.length() - 1), dir.y * (word.length() - 1));
        if (!(isValidCoord(coord) && isValidCoord(tmp))) {
            return error("Word does not stay in the board");
        }

        if (!dawg.contains(word)) {
            return error("Word is not a word");
        }

        //Copy board state to previous board so backup works.
        char[][] currentBoard = board.clone();
        for (int i = 0; i < board.length; i++) {
            currentBoard[i] = board[i].clone();
        }
        boardList.addLast(currentBoard);
        board = currentBoard;

        coord.translate(-dir.x, -dir.y);
        if (isValidCoord(coord) && board[coord.y][coord.x] >= 'A') {
            return error("Before start has a letter");
        }
        coord.translate(dir.x, dir.y);

        String letters = "";
        int points = 0;
        int multiplier = getMultiplier(coord, word, dir);
        boolean hasHooked = false;
        for (char letter : word.toCharArray()) {
            char prevSquare = board[coord.y][coord.x];
            if (prevSquare < 'A') {
                letters += letter;
                board[coord.y][coord.x] = letter;

            } else if (prevSquare != letter) {
                return error("expected " + letter + " found " + prevSquare);
            }

            int score = getLetterScore(prevSquare, letter, dir, coord, multiplier);
            if (score < 0) {
                return error("cross word " + crossWord + " is not a word");
            }
            points += score;
            hasHooked = hasHooked || hasHooked(coord, prevSquare, dir);

            coord.translate(dir.x, dir.y);
        }

        if (!hasHooked) {
            return error("Hasn't hooked");
        }
        if (letters.isEmpty()) {
            return error("No letters used");
        } else if (isValidCoord(coord) && board[coord.y][coord.x] >= 'A') {
            System.out.println("" + (char) ('A' + coord.x) + (coord.y + 1));
            return error("after end has a letter");
        }
        update(board);
        print();
        return new Tupple<>(points + (letters.length() == 7 ? 50 : 0), letters.toCharArray());
    }

    private Tupple<Integer, char[]> error(String msg) {
        System.out.println(msg);
        undo();
        return null;
    }

    private String crossWord;

    public int getLetterScore(char prevSquare, char letter, Point dir, Point coord, int word_multiplier) {
        coord = new Point(coord);
        int points = Tile.getScore(letter) * word_multiplier;
        int multiplier = 1;

        //Get points for square played
        switch (prevSquare) {
            case 0:
                break;
            case 1:
                points *= 2;
                break;
            case 2:
                points *= 3;
                break;
            case 3:
            case 4:
                multiplier = 2;
                break;
            case 5:
                multiplier = 3;
                break;
            default:
                return points;
        }

        Point start = new Point(coord);

        //If new tile was played, calculate cross score.
        //First down or right
        String suffix = "";
        coord.translate(dir.y, dir.x);
        while (isValidCoord(coord) && board[coord.y][coord.x] >= 'A') {
            points += multiplier * Tile.getScore(board[coord.y][coord.x]);
            suffix += board[coord.y][coord.x];
            coord.translate(dir.y, dir.x);
            }

        String prefix = "";
        coord = new Point(start);
        coord.translate(-dir.y, -dir.x);
        //Then up or left
        while (isValidCoord(coord) && board[coord.y][coord.x] >= 'A') {
            points += multiplier * Tile.getScore(board[coord.y][coord.x]);
            prefix += board[coord.y][coord.x];
            coord.translate(-dir.y, -dir.x);
        }

        crossWord = new StringBuilder(prefix).reverse().toString() + letter + suffix;

        //System.out.println(cross_word + " " + dir);
        if (crossWord.length() == 1 || dawg.contains(crossWord)) {
            return points;
        }
        return -100000;
    }

    public boolean hasHooked(Point coord, char prevSquare, Point dir) {
        coord = new Point(coord);
        if (prevSquare >= 'A' || prevSquare == 4) {
            return true;
        }
        coord.translate(dir.y, dir.x);
        if (isValidCoord(coord) && board[coord.y][coord.x] >= 'A') {
            return true;
        }
        coord.translate(-2 * dir.y, -2 * dir.x);
        return isValidCoord(coord) && board[coord.y][coord.x] >= 'A';
    }

    private int getMultiplier(Point coord, String word, Point dir) {
        coord = new Point(coord);
        int multiplier = 1;
        for (int i = 0; i < word.length(); i++) {
            switch (board[coord.y][coord.x]) {
                case 3:
                case 4:
                    multiplier = 2;
                    break;
                case 5:
                    multiplier *= 3;
            }
            coord.translate(dir.x, dir.y);
        }
        coord.translate(-dir.x * word.length(), -dir.y * word.length());
        return multiplier;
    }

    public void print() {
        System.out.println("┌───┰───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┐");
        for (int i = 0; i < board.length; i++) {
            System.out.print("│" + String.format("%02d", i + 1) + " ┃");
            for (char square : board[i]) {
                switch (square) {
                    case 0:
                        System.out.print("   ");
                        break;
                    case 1:
                        System.out.print(ANSI_BRIGHT_BLUE + "███" + ANSI_RESET);
                        break;
                    case 2:
                        System.out.print(ANSI_BLUE + "███" + ANSI_RESET);
                        break;
                    case 3:
                    case 4:
                        System.out.print(ANSI_BRIGHT_RED + "███" + ANSI_RESET);
                        break;
                    case 5:
                        System.out.print(ANSI_RED + "███" + ANSI_RESET);
                        break;
                    default:
                        System.out.print(" " + square + " ");
                }
                System.out.print("│");
            }
            System.out.println();
            if (i < board.length - 1) {
                System.out.println("├───╂───┼───┼───┼───┼───┼───┼───┼───┼───┼───┼───┼───┼───┼───┼───┤");
            }
        }
        System.out.println("└───╄━━━┿━━━┿━━━┿━━━┿━━━┿━━━┿━━━┿━━━┿━━━┿━━━┿━━━┿━━━┿━━━┿━━━┿━━━┥");
        System.out.print("   ");
        for (int i = 0; i < board.length; i++) {
            System.out.print(" │ " + (char) ('A' + i));
        }
        System.out.println(" │");
        System.out.println("    └───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┘");
    }

    public static void main(String[] args) {
        Board x = new Board(null);
        x.board[1][0] = 'A';
        x.print();
    }
}
