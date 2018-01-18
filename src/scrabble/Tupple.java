/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scrabble;

/**
 *
 * @author oscardssmith
 * @param <A> Type 1
 * @param <B> Type 2
 */
public class Tupple<A, B> {

    public A first;
    public B seccond;

    public Tupple(A a, B b) {
        this.first = a;
        this.seccond = b;
    }

    public Tupple() {
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Tupple) {
            Tupple<?, ?> otherTupple = (Tupple) other;
            return this.first.equals(otherTupple.first) && this.seccond.equals(otherTupple.seccond);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 17 * first.hashCode() + seccond.hashCode();
    }

    @Override
    public String toString() {
        return "(" + first + ',' + seccond + ')';
    }
}
