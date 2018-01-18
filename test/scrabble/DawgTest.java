/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scrabble;

import Dawg.Dawg;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author oscardssmith
 */
public class DawgTest {

    private Dawg dawg = null;

    @Before
    public void setUp() {
        try {
            dawg = new Dawg(new File("src/scrabble/words.txt"));
        } catch (IOException ex) {
            fail();
        }
        assertNotNull(dawg);
    }
    /**
     * Test of getWordsWithHook method, of class Dawg.
     */
    @Test
    public void testGetWordsWithHook() {
        char hook = 'b';
        char[] rack = new char[]{'a', 'a', 'i', 'i', 'm', 'l', 'a'};

        //Set<Tupple<String, Integer>> expResult = null;
        Set<Tupple<String, Integer>> result = dawg.getWordsWithHook(hook, rack);
        //assertEquals(expResult, result);
        for (Tupple<String, Integer> wordTupple : result) {
            System.out.println(wordTupple);
            assertTrue(dawg.contains(wordTupple.first));
            assertEquals(wordTupple.first.charAt(wordTupple.seccond), hook);
        }
    }
}
