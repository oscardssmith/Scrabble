/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scrabble;

import Dawg.Dawg;
import Dawg.SimpleDawgNode;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 *
 * @author oscardssmith
 */
public class Computer extends Player {
    public Computer(String name, Bag bag, Board board, Dawg wordDawg) {
        super(name, bag, board, wordDawg);
    }

    @Override
    public boolean preMove() {
        System.out.println("It is " + name + "'s turn. I have " + score + " points.");
        System.out.println("Here are my letters\n" + toString());

        //stores info of best move so far
        Point finalStart = null;
        int maxScore = Integer.MIN_VALUE;

        Point[] dirs = new Point[]{new Point(1, 0), new Point(0, 1)};
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                for (Point dir : dirs) {
                    Point start = new Point(col, row);
                    Tupple<String, Integer> wordScore = getWordsFrom(start, dir, maxScore);
                    if (wordScore.seccond > maxScore) {
                        maxScore = wordScore.seccond;
                        nextDir = dir;
                        finalStart = start;
                        nextWord = wordScore.first;
                        //System.out.println(max_score + " " + (char) ('A' + finalStart.x) + (finalStart.y + 1) + " " + nextWord + " " + nextDir);
                    }
                }
            }
        }
        if (nextWord == null) {
            numTiles = 0;
            System.out.println("No more moves");
            return false;
        }
        board.startCoord = finalStart;
        System.out.println(maxScore + " " + (char) ('A' + finalStart.x) + (finalStart.y + 1) + " " + nextWord + " " + nextDir);
        return true;
    }

    public Tupple<String, Integer> getWordsFrom(Point start, Point dir, int maxScore) {
        Arrays.sort(rack);
        Deque<Character> tileList = new ArrayDeque<>(rack.length);
        for (char tile : rack) {
            if (tile >= '*') {
                tileList.add(Character.toUpperCase(tile));
            }
        }
        Point beforeStart = new Point(start);
        beforeStart.translate(-dir.x, -dir.y);
        if (board.isValidCoord(beforeStart)
            && (board.getBoard()[beforeStart.y][beforeStart.x] >= '*')) {
            return new Tupple<>(null, Integer.MIN_VALUE);
        }
        return getWordsFrom(tileList, wordDawg.getSourceNode(), start, "", false, new ArrayDeque<>(), start, dir, new Tupple<>(null, maxScore));
    }

    private Tupple<String, Integer> getWordsFrom(Deque<Character> tileList, SimpleDawgNode currentNode, Point start, String substring, boolean hasHooked, Deque<Character> lettersUsed, Point coord, Point dir, Tupple<String, Integer> bestScore) {
        if (board.isValidCoord(coord)) {
            SimpleDawgNode[] dawgArray = wordDawg.getNodeArray();
            SimpleDawgNode newNode;
            char prevSquare = board.getBoard()[coord.y][coord.x];

            Point newCoord = new Point(coord);
            newCoord.translate(dir.x, dir.y);
            if (prevSquare < '*') {
                hasHooked |= board.hasHooked(coord, prevSquare, dir);
                for (int i = 0; i < tileList.size(); i++) {
                    char letter = tileList.removeFirst();
                    lettersUsed.add(letter);
                    if (letter == '*') {
                        for (letter = 'a'; letter <= 'z'; letter++) {
                            newNode = currentNode.transition(dawgArray, letter);
                            if (newNode != null) {
                                String newSubstring = substring + letter;
                                bestScore = getWordsHelper(newNode, start, newSubstring, hasHooked, lettersUsed, dir, bestScore);
                                Tupple<String, Integer> bestChild = getWordsFrom(tileList, newNode, start, newSubstring, hasHooked, lettersUsed, newCoord, dir, bestScore);
                                if (bestChild.seccond > bestScore.seccond) {
                                    bestScore = bestChild;
                                }
                            }
                        }
                        letter = '*';
                    } else {
                        newNode = currentNode.transition(dawgArray, letter);
                        if (newNode != null) {
                            String newSubstring = substring + letter;
                            bestScore = getWordsHelper(newNode, start, newSubstring, hasHooked, lettersUsed, dir, bestScore);
                            Tupple<String, Integer> bestChild = getWordsFrom(tileList, newNode, start, newSubstring, hasHooked, lettersUsed, newCoord, dir, bestScore);
                            if (bestChild.seccond > bestScore.seccond) {
                                bestScore = bestChild;
                            }
                        }
                    }
                    lettersUsed.remove(letter);
                    tileList.addLast(letter);
                }
            } else {
                newNode = currentNode.transition(dawgArray, prevSquare);
                if (newNode != null) {
                    substring += prevSquare;
                    getWordsHelper(newNode, start, substring, true, lettersUsed, dir, bestScore);
                    Tupple<String, Integer> bestChild = getWordsFrom(tileList, newNode, start, substring, true, lettersUsed, newCoord, dir, bestScore);
                    if (bestChild.seccond > bestScore.seccond) {
                        bestScore = bestChild;
                    }
                }
            }
        }
        return bestScore;
    }

    private Tupple<String, Integer> getWordsHelper(SimpleDawgNode newNode, Point start, String substring, boolean hasHooked, Deque<Character> lettersUsed, Point dir, Tupple<String, Integer> bestScore) {
        if (newNode != null) {
            if (hasHooked && !lettersUsed.isEmpty() && newNode.endsWord()) {
                Tupple<String, Integer> newScore = new Tupple<>(substring, wordScore(start, substring, dir, lettersUsed));
                if (newScore.seccond > bestScore.seccond) {
                    bestScore = newScore;
                }
            }
        }
        return bestScore;
    }

    private int wordScore(Point start, String word, Point dir, Deque<Character> lettersUsed) {
        int modifier = 0;
        if (lettersUsed.contains('s')) {
            modifier -= 8;
        }
        if (lettersUsed.contains('*')) {
            modifier -= 8;
        }
        return board.dryMove(start, word, dir) + modifier;
    }

    @Override
    public boolean move(Point start, String word, Point dir) {
        Tupple<Integer, char[]> turnScore = board.move(start, word, dir);
        if (turnScore != null && this.containsLetters(turnScore.seccond)) {
            this.updateScore(turnScore.first);
            this.swapLetters(turnScore.seccond);
            return true;
        } else {
            board.print();
            throw new RuntimeException("Computer did a big no now");
        }
    }
}
