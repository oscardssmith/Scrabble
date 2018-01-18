/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Dawg;

/**
 * The class capable of representing a Dawg node, its transition set, and one of its incoming
 * transitions; objects of this class are used to represent a Dawg after its been simplified in
 * order to save space.
 *
 * @author oscardssmith
 */
public class SimpleDawgNode {
    //The character labeling an incoming transition to this node
    private final byte letter;

    //The boolean denoting the accept state status of this node
    private final boolean endsWord;

    //The int denoting the size of this node's outgoing transition set
    private final int transitionSetSize;

    //The int denoting the index (in the array which contains this node) at which this node's transition set begins
    private int transitionSetBeginIndex;

    /**
     * Constructs a SimpleDawgNode.
     *
     * @param letter a char representing the transition label leading to this SimpleDawgNode
     * @param endsWord a boolean representing the accept state status of this SimpleDawgNode
     * @param transitionSetSize an int denoting the size of this transition set
     */
    public SimpleDawgNode(byte letter, boolean endsWord, int transitionSetSize) {
        this.letter = letter;
        this.endsWord = endsWord;
        this.transitionSetSize = transitionSetSize;
        //will be changed for all objects of this type, necessary for dummy root node creation
        this.transitionSetBeginIndex = 0;
    }

    /**
     * Retrieves the character representing the transition label leading up to this node.
     *
     * @return the char representing the transition label leading up to this node
     */
    public byte getLetter() {
        return letter;
    }

    /**
     * Retrieves the accept state status of this node.
     *
     * @return true if this node is an accept state, false otherwise
     */
    public boolean endsWord() {
        return endsWord;
    }

    /**
     * Retrieves the index in this node's containing array that its transition set begins at.
     *
     * @return an int of the index in this node's containing array at which its transition set
     * begins
     */
    public int getTransitionSetBeginIndex() {
        return transitionSetBeginIndex;
    }

    /**
     * Retrieves the size of this node's outgoing transition set.
     *
     * @return an int denoting the size of this node's outgoing transition set
     */
    public int getOutgoingTransitionSetSize() {
        return transitionSetSize;
    }

    /**
     * Records the index in this node's containing array that its transition set begins at.
     *
     * @param transitionSetBeginIndex an int denoting the index in this node's containing array that
     * is transition set beings at
     */
    public void setTransitionSetBeginIndex(int transitionSetBeginIndex) {
        this.transitionSetBeginIndex = transitionSetBeginIndex;
    }

    /**
     * Follows an outgoing transition from this node.
     *
     * @param mdagDataArray the array of SimpleDawgNodes containing this node
     * @param letter the char representation of the desired transition's label
     * @return the SimpleDawgNode that is the target of the transition labeled with {@code letter},
     * or null if there is no such labeled transition from this node
     */
    public SimpleDawgNode transition(SimpleDawgNode[] mdagDataArray, char letter) {
        int transitionSetEndIndex = transitionSetBeginIndex + transitionSetSize - 1;
        SimpleDawgNode targetNode = null;

        //Loop through the SimpleDawgNodes in this node's transition set, searching for
        //the one with a letter equal to that which labels the desired transition
        for (int i = transitionSetBeginIndex; i <= transitionSetEndIndex; i++) {
            if (mdagDataArray[i].getLetter() == Character.toUpperCase(letter)) {
                targetNode = mdagDataArray[i];
                break;
            }
        }
        return targetNode;
    }

    /**
     * Follows a transition path starting from this node.
     *
     * @param mdagDataArray the array of SimpleDawgNodes containing this node
     * @param str a String corresponding a transition path in the Dawg
     * @return the SimpleDawgNode at the end of the transition path corresponding to {@code str}, or
     * null if such a transition path is not present in the Dawg
     */
    public SimpleDawgNode transition(SimpleDawgNode[] mdagDataArray, String str) {
        SimpleDawgNode currentNode = this;
        int numberOfChars = str.length();

        //Iteratively transition through the Dawg using the chars in str
        for (int i = 0; i < numberOfChars; i++) {
            currentNode = currentNode.transition(mdagDataArray, str.charAt(i));
            if (currentNode == null) {
                break;
            }
        }
        return currentNode;
    }

    /**
     * Follows a transition path starting from the source node of a Dawg.
     *
     * @param mdagDataArray the array containing the data of the Dawg to be traversed
     * @param sourceNode the dummy SimpleDawgNode which functions as the source of the Dawg data in
     * {@code mdagDataArray}
     * @param str a String corresponding to a transition path in the to-be-traversed Dawg
     * @return the SimpleDawgNode at the end of the transition path corresponding to {@code str}, or
     * null if such a transition path is not present in the Dawg
     */
    public static SimpleDawgNode traverseDawg(SimpleDawgNode[] mdagDataArray, SimpleDawgNode sourceNode, String str) {
        char firstLetter = str.charAt(0);
        //Loop through the SimpleDawgNodes in the processing Dawg's source node's transition set,
        //searching for the the one with a letter (char) equal to the first char of str.
        //We can use that target node to transition through the Dawg with the rest of the string
        for (int i = 0; i < sourceNode.transitionSetSize; i++) {
            if (mdagDataArray[i].getLetter() == firstLetter) {
                return mdagDataArray[i].transition(mdagDataArray, str.substring(1));
            }
        }
        return null;
    }
}