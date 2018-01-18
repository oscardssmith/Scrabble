/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scrabble;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
/**
 *
 * @author oscardssmith
 */
public class Tile extends JButton {
    public static final Dimension SIZE = new Dimension(50, 50);
    public static final Font FONT = new Font(Font.MONOSPACED, Font.BOLD, 27);
    private int row;
    private int col;
    private char value;

    public Tile(int row, int col, char value) {
        this.row = row;
        this.col = col;
        this.value = value;
        setFont(FONT);
        setPreferredSize(SIZE);
        render();
    }

    public Point getCoord() {
        return new Point(col, row);
    }
    public char getValue() {
        return value;
    }

    public void setValue(char value) {
        this.value = value;
        render();
    }

    public final void render() {
        setText("");
        switch (value) {
            case 0:
                setBackground(Color.WHITE);
                break;
            case 1:
                setBackground(new Color(160, 220, 255));
                break;
            case 2:
                setBackground(Color.BLUE);
                break;
            case 4:
                setText("★");
            case 3:
                setBackground(Color.PINK);
                break;
            case 5:
                setBackground(Color.RED);
                break;
            default:
                setBackground(Color.LIGHT_GRAY);
                setText("" + value);
        }
    }
    private static final Map<Character, Integer> score;

    static {
        score = new HashMap<>(27);
        score.put('A', 1);
        score.put('B', 3);
        score.put('C', 3);
        score.put('D', 2);
        score.put('E', 1);
        score.put('F', 4);
        score.put('G', 2);
        score.put('H', 4);
        score.put('I', 1);
        score.put('J', 8);
        score.put('K', 5);
        score.put('L', 1);
        score.put('M', 3);
        score.put('N', 1);
        score.put('O', 1);
        score.put('P', 3);
        score.put('Q', 10);
        score.put('R', 1);
        score.put('S', 1);
        score.put('T', 1);
        score.put('U', 1);
        score.put('V', 4);
        score.put('W', 4);
        score.put('X', 8);
        score.put('Y', 4);
        score.put('Z', 10);
        score.put('*', 0);
    }

    public static int getScore(char letter) {
        if (score.containsKey(letter)) {
            return score.get(letter);
        }
        return 0;
    }
}
