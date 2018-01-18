/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scrabble;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author oscardssmith
 */
public class Bag {
    private List<Character> letters;
    Random generator;

    public Bag() {
        generator = new Random();
        letters = new LinkedList<>();
        int[] freq = new int[]{9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1};
        for (int i = 0; i < freq.length; i++) {
            while (freq[i] > 0) {
                letters.add(0, (char) ('A' + i));
                freq[i]--;
            }
        }
        letters.add(0, '*');
        letters.add(0, '*');
        Collections.shuffle(letters);
    }

    public char draw() {
        if (!letters.isEmpty()) {
            return letters.remove(0);
        }
        throw new IndexOutOfBoundsException("Bag is empty. Please don't do this;");
    }

    public char[] draw(int numChar) {
        numChar = Math.min(letters.size(), numChar);
        char[] charList = new char[numChar];
        while (numChar > 0) {
            charList[numChar - 1] = letters.remove(0);
            numChar--;
        }
        return charList;
    }

    public char[] swapTiles(char[] tiles) {
        for (char tile : tiles) {
            letters.add(generator.nextInt(tiles.length), tile);
        }
        return tiles;
    }

    public boolean isEmpty() {
        return letters.isEmpty();
    }
}
