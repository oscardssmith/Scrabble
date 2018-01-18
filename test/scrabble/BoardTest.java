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
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 *
 * @author oscardssmith
 */
public class BoardTest {

    public BoardTest() {
    }
    static Dawg dawg;
    Board board;
    static Bag bag;

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
    }

    @Test
    public void testIsValidCoord() {
        assertEquals(false, board.isValidCoord(7, 15));
        assertEquals(true, board.isValidCoord(7, 1));
        assertEquals(true, board.isValidCoord(0, 14));
        assertEquals(false, board.isValidCoord(-1, 1));
    }

    @Test
    @Ignore
    public void testMove() {
        int expResult = board.dryMove(new Point(7, 3), "ULTIMO", new Point(0, 1));
        int result = board.move(new Point(7, 3), "ULTIMO", new Point(0, 1)).first;
        System.out.println("expected " + expResult + " found " + result);
        assertEquals(expResult, result);
    }

    @Test
    public void testMove1() {
        Player human = new Human("Human", new Bag(), board, null);
        human.rack = "ROLE".toCharArray();
        human.move(new Point(4, 7), "ROLE", new Point(1, 0));
        human.rack = "LOSING".toCharArray();
        human.move(new Point(6, 7), "LOSING", new Point(0, 1));
        human.rack = "LOSING".toCharArray();
        human.move(new Point(6, 9), "SPAZ", new Point(1, 0));
        human.rack = "VAT".toCharArray();
        human.move(new Point(8, 8), "VAT", new Point(0, 1));
    }

    @Test
    @Ignore
    public void testGetLetterScore() {
        board.move(new Point(7, 7), "F", new Point(1, 0));
        int result = board.getLetterScore('\0', 'I', new Point(0, 1), new Point(6, 7), 2);
        assertEquals(2, result);
    }
}
