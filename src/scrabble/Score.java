/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scrabble;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author oscardssmith
 */
public class Score extends JPanel {

    private Player[] players;
    private List<JLabel> scores;

    public Score(Player[] players) {
        super(new GridLayout(players.length, 2));
        this.players = players;

        scores = new ArrayList<>(players.length);
        for (Player player : players) {
            add(new JLabel(player.name));
            JLabel score = new JLabel(Integer.toString(player.score));
            score.setSize(new Dimension(75, 25));
            scores.add(score);
            add(score);
        }
        setMaximumSize(new Dimension(25 * 16, players.length * 50));
    }

    public void update() {
        for (int i = 0; i < scores.size(); i++) {
            scores.get(i).setText(Integer.toString(players[i].score));
        }
    }

}
