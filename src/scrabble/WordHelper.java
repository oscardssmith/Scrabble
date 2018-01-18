/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scrabble;

import Dawg.Dawg;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author oscardssmith
 */
public final class WordHelper {

    public WordHelper() {
        Dawg dawg = null;
        try {
            dawg = new Dawg(new File("src/scrabble/words.txt"));
        } catch (IOException ex) {
        }
        Scanner scanner = new Scanner(System.in);
        if (dawg != null) {
            System.out.println("Enter your letters");
            char[] input = scanner.next().toCharArray();
            input = "etaoind".toCharArray();
            long start = System.nanoTime();
            Set<String> words = dawg.getStringsWith(input);
            System.out.println(words.size() + " words found");
            /*for (String word : words) {                System.out.println(word);
            }*/
            words = dawg.getStringsWith(input, 1);
            long end = System.nanoTime();
            System.out.println(words.size() + " words found with one blank");
            /* for (String word : words) {                System.out.println(word);
            }*/

            System.out.println((end - start) / 1000000000.0);
        }
    }

    public static void main(String[] args) {
        WordHelper x = new WordHelper();
    }
}
