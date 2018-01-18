/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scrabble;

import Dawg.Dawg;

/**
 *
 * @author oscardssmith
 */
public class Human extends Player {

    public Human(String name, Bag bag, Board board, Dawg wordDawg) {
        super(name, bag, board, wordDawg);
    }

    @Override
    public boolean preMove() {
        return false;
    }
}
