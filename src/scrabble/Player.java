/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scrabble;

import Dawg.Dawg;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 *
 * @author oscardssmith
 */
public abstract class Player extends JPanel implements ActionListener {

    protected int score;
    protected char[] rack;
    public final Tile[] tiles;
    protected final String name;
    protected final Bag bag;
    protected int numTiles;
    protected final Dawg wordDawg;
    protected final Board board;

    public String nextWord = null;
    public Point nextDir = new Point(1, 0);

    public Player(String name, Bag bag, Board board, Dawg wordDawg) {
        super(new GridLayout(1, 7));
        numTiles = 7;
        rack = bag.draw(numTiles);
        tiles = new Tile[rack.length];
        setBorder(new TitledBorder(null, name + "'s rack", TitledBorder.CENTER, TitledBorder.ABOVE_TOP));

        for (int i = 0; i < rack.length; i++) {
            Tile tile = new Tile(1, i, rack[i]);
            tiles[i] = tile;
            add(tile);
            tile.addActionListener(this);
        }
        score = 0;
        this.name = name;
        this.bag = bag;
        this.wordDawg = wordDawg;
        this.board = board;
    }

    public int getScore() {
        return score;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getTileNum() {
        return numTiles;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void updateScore(int change) {
        score += change;
    }

    public char[] getRack() {
        return rack;
    }

    public boolean containsLetters(char[] letters) {
        // copy rack to prevent messing up order
        char[] tmpRack = Arrays.copyOf(rack, rack.length);

        for (char letter : letters) {
            boolean found = false;
            for (int i = 0; i < tmpRack.length; i++) {
                if (letter == tmpRack[i]) {
                    tmpRack[i] = '\0';
                    found = true;
                    break;
                } else if (Character.isLowerCase(letter) && tmpRack[i] == '*') {
                    tmpRack[i] = '\0';
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("letter not found: " + letter);
                return false;
            }
        }
        return true;
    }

    protected void swapLetters(char[] letters) {
        for (char letter : letters) {
            for (int i = 0; i < rack.length; i++) {
                if (rack[i] == letter
                    || Character.isLowerCase(letter) && rack[i] == '*') {
                    if (bag.isEmpty()) {
                        numTiles--;
                        rack[i] = rack[numTiles];
                        rack[numTiles] = '\0';
                        tiles[numTiles].setValue('\0');
                        tiles[i].setValue(rack[i]);
                    } else {
                        rack[i] = bag.draw();
                        tiles[i].setValue(rack[i]);
                        break;
                    }
                }
            }
        }
    }

    abstract public boolean preMove();

    public boolean move(Point coord, String word, Point dir) {
        Tupple<Integer, char[]> turnScore = board.move(coord, word.toUpperCase(), dir);
        if (turnScore == null) {
            return false;
        } else if (this.containsLetters(turnScore.seccond)) {
            this.updateScore(turnScore.first);
            this.swapLetters(turnScore.seccond);
            return true;
        } else {
            board.undo();
            return false;
        }
    }

    @Override
    public String toString() {
        String ans = Arrays.toString(rack);
        return ans;
    }

    public int first = -1;
    @Override
    public void actionPerformed(ActionEvent e) {
        int col = ((Tile) e.getSource()).getCoord().x;
        if (first == -1) {
            first = col;
            System.out.println("first=" + col);
            tiles[first].setBackground(Color.red);
        } else {
            System.out.println("second = " + col);
            char tmp = tiles[first].getValue();
            tiles[first].setValue(tiles[col].getValue());
            tiles[col].setValue(tmp);
            first = -1;
        }
    }
}
