package scrabble;

import Dawg.Dawg;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author oscardssmith
 */
public final class Scrabble implements Runnable {

    private final JFrame frame = new JFrame("Scrabble");
    private final JPanel gui = new JPanel(new BorderLayout(5, 5));
    private final JPanel rightPanel;
    private final Board board;
    private Player[] players;
    private Score score;
    int turn = 0;

    private Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    public Scrabble() {
        Scanner input = new Scanner(System.in);
        Dawg dawg = null;
        try {
            dawg = new Dawg(new File("src/scrabble/words.txt"));
        } catch (IOException e) {
            System.exit(0);
        }
        board = new Board(dawg);
        Bag bag = new Bag();

        int playerNum = -1;
        int humanNum = -1;
        while (!(playerNum <= 4 && playerNum >= humanNum && humanNum >= 0)) {
            System.out.println("How many players?");
            playerNum = input.nextInt();
            System.out.println("How many humans?");
            humanNum = input.nextInt();
        }

        this.players = new Player[playerNum];
        for (int i = 0; i < playerNum; i++) {
            if (i < humanNum) {
                players[i] = new Human("Human" + i, bag, board, dawg);
            } else {
                players[i] = new Computer("Computer" + (i - humanNum), bag, board, dawg);
            }
        }

        rightPanel = RightPanel();
        run();
    }

    public void nextPlayer() {
        players[turn].setVisible(false);
        turn = (turn + 1) % players.length;
        players[turn].setVisible(true);
        gui.add(players[turn], SOUTH);
    }

    @Override
    public void run() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gui.add(rightPanel, EAST);

        //board.setPreferredSize(Tile.SIZE);
        gui.add(board, CENTER);
        for (Player player : players) {
            gui.add(player, SOUTH);
        }

        players[turn].setVisible(true);
        gui.add(players[turn], SOUTH);

        frame.setContentPane(gui);

        frame.pack();

        frame.setLocationRelativeTo(null);
        frame.setLocationByPlatform(true);
        frame.setMinimumSize(frame.getSize());
        frame.setVisible(true);
        players[0].preMove();
        move();
    }

    private void move() {
        Point start = board.startCoord;
        String word = players[turn].nextWord;
        Point dir = players[turn].nextDir;
        if ((word == null || start == null)) {
            System.out.println("didn't work" + word + start);
        } else {
            System.out.println(word + start);
            if (!players[turn].move(start, word, dir)) {
                return;
            }
            score.update();

            if (players[turn].getTileNum() == 0) {
                try {
                    Thread.sleep(10000);
                    endGame();
                } catch (InterruptedException ex) {
                    System.exit(0);
                }
                System.exit(0);
            }

            //reset turn variables
            players[turn].nextWord = null;
            board.startCoord = null;
            nextPlayer();

            if (players[turn].preMove()) {
                move();
            }
        }
    }

    public JPanel RightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton newGame = new JButton("New Game");
        panel.add(newGame);
        newGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("New Game");
            }
        });

        JTextField wordField = new JTextField(7);
        wordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                players[turn].nextWord = wordField.getText().toUpperCase();
            }
        });
        wordField.setMaximumSize(new Dimension(300, 25));
        panel.add(new JLabel("Enter Your Word Here"));
        panel.add(wordField);

        JButton downButton = new JButton("Across");
        String[] downText = {"Across", "Down"};
        panel.add(downButton);
        downButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point nextDir = players[turn].nextDir;
                nextDir.setLocation(nextDir.y, nextDir.x);
                downButton.setText(downText[nextDir.y]);
            }
        });

        JButton move = new JButton("move");
        panel.add(move);
        move.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                move();
            }
        });

        JButton undo = new JButton("Undo");
        panel.add(undo);
        undo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.undo();
            }
        });

        JLabel blankLabel = new JLabel("Enter blank value");
        panel.add(blankLabel);
        JTextField blankSet = new JTextField();
        blankSet.setMaximumSize(new Dimension(25, 25));
        wordField.setMaximumSize(new Dimension(300, 25));
        panel.add(blankSet);
        blankSet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int col = players[turn].first;
                if (col < 0 || players[turn].rack[col] != '*') {
                    return;
                }
                players[turn].rack[col] = blankSet.getText().toLowerCase().charAt(0);
                players[turn].tiles[col].setText(blankSet.getText().toLowerCase());
            }
        });

        score = new Score(players);
        panel.add(score);
        return panel;
    }

    private void endGame() {
        int totalPoints = 0;
        for (Player player : players) {
            int points = 0;
            for (char letter : player.rack) {
                points -= Tile.getScore(letter);
            }
            player.updateScore(points);
            totalPoints -= points;
        }
        players[turn].updateScore(totalPoints);
        score.update();
    }

    public static void main(String[] args) {
        Scrabble x = new Scrabble();
    }
}
