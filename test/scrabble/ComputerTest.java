/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scrabble;

import Dawg.Dawg;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 *
 * @author oscardssmith
 */
public class ComputerTest {

    static Dawg dawg;
    static Computer computer;
    Board board;
    static Bag bag;

    public ComputerTest() {
    }

    @BeforeClass
    public static void x() {
        dawg = null;
        try {
            dawg = new Dawg(new File("src/scrabble/words.txt"));
        } catch (IOException ex) {
            fail();
        }
        assertNotNull(dawg);
        bag = new Bag();
    }

    @Before
    public void setUp() {
        board = new Board(dawg);
        computer = new Computer("tester", bag, board, dawg);
    }

    /**
     * Test of move method, of class Computer.
     */
    @Test
    @Ignore
    public void testMove() {
        board.move(new Point(7, 7), "baned".toUpperCase(), new Point(1, 0));
        board.move(new Point(8, 7), "ask".toUpperCase(), new Point(0, 1));
        computer.rack = "AAIIMLA".toCharArray();
        //assertEquals(new Tupple<>("IF", 5), computer.getWordsFrom(board, 4, 10, true));
        //computer.move(board, null);
    }

    @Test
    @Ignore
    public void testMove1() {
        board.move(new Point(4, 7), "VENT", new Point(1, 0));
        computer.rack = "EEWQROR".toCharArray();
        System.out.println(board.dryMove(new Point(4, 3), "REWOVE", new Point(0, 1)));
        System.out.println(computer.getWordsFrom(new Point(4, 3), new Point(0, 1), 0));
        computer.preMove();
        computer.move(board.startCoord, computer.nextWord, computer.nextDir);
        System.out.println(computer.score);
    }

    @Test
    public void testMoveWithWild() {
        board.move(new Point(4, 7), "VENT", new Point(1, 0));
        computer.rack = "*".toCharArray();
        System.out.println(computer.getWordsFrom(new Point(6, 6), new Point(0, 1), 0));
        computer.preMove();
        computer.move(board.startCoord, computer.nextWord, computer.nextDir);
        System.out.println(computer.score);
    }

    @Test
    public void testSwapLetters() {
        computer.rack = "QII".toCharArray();
        computer.numTiles = 3;
        bag.draw(100);
        computer.preMove();
        computer.move(board.startCoord, computer.nextWord, computer.nextDir);
        System.out.println(Arrays.toString(computer.rack));
        Assert.assertArrayEquals(computer.rack, "I\0\0".toCharArray());
    }
}
