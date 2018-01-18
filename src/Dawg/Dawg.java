/**
 * Dawg is a Java library capable of constructing character-sequence-storing, directed acyclic
 * graphs of minimal size. This is a modified version
 *
 * Copyright (C) 2012 Kevin Lawson <Klawson88@gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package Dawg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import scrabble.Tupple;

/**
 * A minimalistic directed acyclical graph suitable for storing a set of Strings.
 *
 * @author Kevin
 * @author Oscardssmith
 */
public class Dawg {

    //DawgNode from which all others in the structure are reachable while Dawg is being built
    private DawgNode sourceNode = new DawgNode(false);

    //SimpleDawgNode from which all others in the structure are reachable (will be defined once this Dawg is simplified)
    private SimpleDawgNode simplifiedSourceNode;

    //HashMap which contains the DawgNodes collectively representing the all unique equivalence classes in the Dawg.
    //Uniqueness is defined by the types of transitions allowed from, and number and type of nodes reachable
    //from the node of interest. Since there are no duplicate nodes in an Dawg, # of equivalence classes == # of nodes.
    private HashMap<DawgNode, DawgNode> equivalenceClassDawgNodeHashMap = new HashMap<>();

    //Array that will contain a space-saving version of the Dawg after call to simplify().
    private SimpleDawgNode[] mdagDataArray;

    //An int denoting the total number of transitions between the nodes of the Dawg
    private int transitionCount;
    private int stringCount;

    /**
     * Creates an Dawg from a newline delimited file containing the data of interest.
     *
     * @param dataFile a {@link java.io.File} representation of a file containing the Strings that
     * the Dawg will contain
     * @throws IOException if {@code datafile} cannot be opened, or a read operation on it cannot be
     * carried out
     */
    public Dawg(File dataFile) throws IOException {
        Scanner s = new Scanner(dataFile);
        List<String> words = new ArrayList<>();
        while (s.hasNext()) {
            words.add(s.next());
        }
        this.addStrings(words);
    }

    /**
     * Creates an Dawg from a collection of Strings.
     *
     * @param strCollection a {@link java.util.Collection} containing Strings that the Dawg will
     * contain
     */
    public Dawg(Collection<String> strCollection) {
        addStrings(strCollection);
    }

    /**
     * Adds a Collection of Strings to the Dawg.
     *
     * @param strCollection a {@link java.util.Collection} containing Strings to be added to the
     * Dawg
    */
    private void addStrings(Collection<String> strCollection) {
        stringCount = strCollection.size();
        String previousString = "";

        //Add all the Strings in strCollection to the Dawg.
        for (String currentString : strCollection) {
            currentString = currentString.toUpperCase();
            int mpsIndex = calculateMinimizationProcessingStartIndex(previousString, currentString);

                    //If the transition path of the previousString needs to be examined for minimization or
            //equivalence class representation after a certain point, call replaceOrRegister to do so.
            if (mpsIndex != -1) {

                String transitionSubstring = previousString.substring(0, mpsIndex);
                String minimizationProcessingSubString = previousString.substring(mpsIndex);
                replaceOrRegister(sourceNode.transition(transitionSubstring), minimizationProcessingSubString);
            }
                   

            addStringInternal(currentString);
            previousString = currentString;
        }
               

        //Since we delay the minimization of the previously-added String
        //until after we read the next one, we need to have a seperate
        //statement to minimize the absolute last String.
        replaceOrRegister(sourceNode, previousString);
        this.simplify();
    }

    /**
     * Determines the start index of the substring in the String most recently added to the Dawg
     * that corresponds to the transition path that will be next up for minimization processing.
     *
     * The "minimization processing start index" is defined as the index in {@code prevStr} which
     * starts the substring corresponding to the transition path that doesn't have its right
     * language extended by {@code currStr}. The transition path of the substring before this point
     * is not considered for minimization in order to limit the amount of times the equivalence
     * classes of its nodes will need to be reassigned during the processing of Strings which share
     * prefixes.
     *
     * @param prevStr the String most recently added to the Dawg
     * @param currStr the String next to be added to the Dawg
     * @return an int of the index in {@code prevStr} that starts the substring corresponding to the
     * transition path next up for minimization processing
     */
    private int calculateMinimizationProcessingStartIndex(String prevStr, String currStr) {
        int mpsIndex;

        if (!currStr.startsWith(prevStr)) {
            //Loop through the corresponding indices of both Strings in search of the first index containing differing characters.
            //The transition path of the substring of prevStr from this point will need to be submitted for minimization processing.
            //The substring before this point, however, does not, since currStr will simply be extending the right languages of the
            //nodes on its transition path.
            int shortestStringLength = Math.min(prevStr.length(), currStr.length());
            for (mpsIndex = 0; mpsIndex < shortestStringLength && prevStr.charAt(mpsIndex) == currStr.charAt(mpsIndex); mpsIndex++) {
            };
            
        } else {
            mpsIndex = -1;    //If the prevStr is a prefix of currStr, then currStr simply extends the right language of the transition path of prevStr.
        }
        return mpsIndex;
    }

    /**
     * Determines the longest prefix of a given String that is the prefix of another String
     * previously added to the Dawg.
     *
     * @param str the String to be processed
     * @return a String of the longest prefix of {@code str} that is also a prefix of a String
     * contained in the Dawg
     */
    private String determineLongestPrefixInDawg(String str) {
        DawgNode currentNode = sourceNode;
        int numberOfChars = str.length();
        int onePastPrefixEndIndex = 0;

        //Loop through the characters in str, using them in sequence to transition
        //through the Dawg until the currently processing node doesn't have a transition
        //labeled with the current processing char, or there are no more characters to process.
        for (int i = 0; i < numberOfChars; i++, onePastPrefixEndIndex++) {
            byte currentChar = (byte) str.charAt(i);
            if (currentNode.hasOutgoingTransition(currentChar)) {
                currentNode = currentNode.transition(currentChar);
            } else {
                break;
            }
        }
        

        return str.substring(0, onePastPrefixEndIndex);
    }

    /**
     * Determines and retrieves data related to the first confluence node (defined as a node with
     * two or more incoming transitions) of a transition path corresponding to a given String from a
     * given node.
     *
     * @param originNode the DawgNode from which the transition path corresponding to str starts
     * from
     * @param str a String corresponding to a transition path in the Dawg
     * @return a HashMap of Strings to Objects containing: - an int denoting the length of the path
     * to the first confluence node in the transition path of interest - the DawgNode which is the
     * first confluence node in the transition path of interest (or null if one does not exist)
     */
    private HashMap<String, Object> getTransitionPathFirstConfluenceNodeData(DawgNode originNode, String str) {
        int currentIndex = 0;
        int charCount = str.length();
        DawgNode currentNode = originNode;

        //Loop thorugh the characters in str, sequentially using them to transition through the Dawg in search of
        //(and breaking upon reaching) the first node that is the target of two or more transitions. The loop is
        //also broken from if the currently processing node doesn't have a transition labeled with the currently processing char.
        for (; currentIndex < charCount; currentIndex++) {
            byte currentChar = (byte) str.charAt(currentIndex);
            currentNode = (currentNode.hasOutgoingTransition(currentChar) ? currentNode.transition(currentChar) : null);

            if (currentNode == null || currentNode.isConfluenceNode()) {
                break;
            }
        }
        

        boolean noConfluenceNode = (currentNode == originNode || currentIndex == charCount);

        //Create a HashMap containing the index of the last char in the substring corresponding
        //to the transitoin path to the confluence node, as well as the actual confluence node
        HashMap<String, Object> confluenceNodeDataHashMap = new HashMap<>(2);
        confluenceNodeDataHashMap.put("toConfluenceNodeTransitionCharIndex", (noConfluenceNode ? null : currentIndex));
        confluenceNodeDataHashMap.put("confluenceNode", noConfluenceNode ? null : currentNode);
        

        return confluenceNodeDataHashMap;
    }

    /**
     * Performs minimization processing on a transition path starting from a given node.
     *
     * This entails either replacing a node in the path with one that has an equivalent right
     * language/equivalence class (defined as set of transition paths that can be traversed and
     * nodes able to be reached from it), or making it a representative of a right
     * language/equivalence class if a such a node does not already exist.
     *
     * @param originNode the DawgNode that the transition path corresponding to str starts from
     * @param str a String related to a transition path
     */
    private void replaceOrRegister(DawgNode originNode, String str) {
        byte transitionLabelChar = (byte) str.charAt(0);
        DawgNode relevantTargetNode = originNode.transition(transitionLabelChar);

        //If relevantTargetNode has transitions and there is at least one char left to process, recursively call
        //this on the next char in order to further processing down the transition path corresponding to str
        if (relevantTargetNode.hasOutgoingTransitions() && !str.substring(1).isEmpty()) {
            replaceOrRegister(relevantTargetNode, str.substring(1));
        }
        

        //Get the node representing the equivalence class that relevantTargetNode belongs to. DawgNodes hash on the
        //transitions paths that can be traversed from them and nodes able to be reached from them;
        //nodes with the same equivalence classes will hash to the same bucket.
        DawgNode equivalentNode = equivalenceClassDawgNodeHashMap.get(relevantTargetNode);

        if (equivalentNode == null) //if there is no node with the same right language as relevantTargetNode
        {
            equivalenceClassDawgNodeHashMap.put(relevantTargetNode, relevantTargetNode);
        } else if (equivalentNode != relevantTargetNode) //if there is another node with the same right language as relevantTargetNode, reassign the
        {                                               //transition between originNode and relevantTargetNode, to originNode and the node representing the equivalence class of interest
            relevantTargetNode.decrementTargetIncomingTransitionCounts();
            transitionCount -= relevantTargetNode.getOutgoingTransitionCount(); //Since this method is recursive, the outgoing transitions of all of relevantTargetNode's child nodes have already been reassigned,
            //so we only need to decrement the transition count by the relevantTargetNode's outgoing transition count
            originNode.reassignOutgoingTransition(transitionLabelChar, relevantTargetNode, equivalentNode);
        }
    }

    /**
     * Adds a transition path starting from a specific node in the Dawg.
     *
     * @param originNode the DawgNode which will serve as the start point of the to-be-created
     * transition path
     * @param str the String to be used to create a new transition path from {@code originNode}
     */
    private void addTransitionPath(DawgNode originNode, String str) {
        if (!str.isEmpty()) {
            DawgNode currentNode = originNode;
            int charCount = str.length();

            //Loop through the characters in str, iteratevely adding
            // a transition path corresponding to it from originNode
            for (int i = 0; i < charCount; i++, transitionCount++) {
                byte currentChar = (byte) str.charAt(i);
                boolean isLastChar = (i == charCount - 1);
                currentNode = currentNode.addOutgoingTransition(currentChar, isLastChar);
            }
            
        } else {
            originNode.setAcceptStateStatus(true);
        }
    }

    /**
     * Removes from equivalenceClassDawgNodeHashmap the entries of all the nodes in a transition
     * path.
     *
     * @param str a String corresponding to a transition path from sourceNode
     */
    private void removeTransitionPathRegisterEntries(String str) {
        DawgNode currentNode = sourceNode;

        int charCount = str.length();

        for (int i = 0; i < charCount; i++) {
            currentNode = currentNode.transition((byte) str.charAt(i));
            if (equivalenceClassDawgNodeHashMap.get(currentNode) == currentNode) {
                equivalenceClassDawgNodeHashMap.remove(currentNode);
            }

            //The hashCode of an DawgNode is cached the first time a hash is performed without a cache value present.
            //Since we just hashed currentNode, we must clear this regardless of its presence in equivalenceClassDawgNodeHashMap
            //since we're not actually declaring equivalence class representatives here.
            currentNode.clearStoredHashCode();
        }
    }

    /**
     * Clones a transition path from a given node.
     *
     * @param pivotConfluenceNode the DawgNode that the cloning operation is to be based from
     * @param transitionStringToPivotNode a String which corresponds with a transition path from
     * souceNode to {@code pivotConfluenceNode}
     * @param str a String which corresponds to the transition path from {@code pivotConfluenceNode}
     * that is to be cloned
     */
    private void cloneTransitionPath(DawgNode pivotConfluenceNode, String transitionStringToPivotNode, String str) {
        DawgNode lastTargetNode = pivotConfluenceNode.transition(str);      //Will store the last node which was used as the base of a cloning operation
        DawgNode lastClonedNode = null;                                     //Will store the last cloned node
        char lastTransitionLabelChar = '\0';                                //Will store the char which labels the transition to lastTargetNode from its parent node in the prefixString's transition path

        //Loop backwards through the indices of str, using each as a boundary to create substrings of str of decreasing length
        //which will be used to transition to, and duplicate the nodes in the transition path of str from pivotConfluenceNode.
        for (int i = str.length(); i >= 0; i--) {
            String currentTransitionString = (i > 0 ? str.substring(0, i) : null);
            DawgNode currentTargetNode = (i > 0 ? pivotConfluenceNode.transition(currentTransitionString) : pivotConfluenceNode);
            DawgNode clonedNode;

            if (i == 0) //if we have reached pivotConfluenceNode
            {
                //Clone pivotConfluenceNode in a way that reassigns the transition of its parent node (in transitionStringToConfluenceNode's path) to the clone.
                String transitionStringToPivotNodeParent = transitionStringToPivotNode.substring(0, transitionStringToPivotNode.length() - 1);
                char parentTransitionLabelChar = transitionStringToPivotNode.charAt(transitionStringToPivotNode.length() - 1);
                clonedNode = pivotConfluenceNode.clone(sourceNode.transition(transitionStringToPivotNodeParent), (byte) parentTransitionLabelChar);
                
            } else {
                clonedNode = currentTargetNode.clone();     //simply clone curentTargetNode
            }
            transitionCount += clonedNode.getOutgoingTransitionCount();

            //If this isn't the first node we've cloned, reassign clonedNode's transition labeled
            //with the lastTransitionChar (which points to the last targetNode) to the last clone.
            if (lastClonedNode != null) {
                clonedNode.reassignOutgoingTransition((byte) lastTransitionLabelChar, lastTargetNode, lastClonedNode);
                lastTargetNode = currentTargetNode;
            }

            //Store clonedNode and the char which labels the transition between the node it was cloned from (currentTargetNode) and THAT node's parent.
            //These will be used to establish an equivalent transition to clonedNode from the next clone to be created (it's clone parent).
            lastClonedNode = clonedNode;
            lastTransitionLabelChar = (i > 0 ? str.charAt(i - 1) : '\0');
            
        }
        
    }

    /**
     * Adds a String to the Dawg (called by addString to do actual Dawg manipulation).
     *
     * @param str the String to be added to the Dawg
     */
    private void addStringInternal(String str) {
        String prefixString = determineLongestPrefixInDawg(str);
        String suffixString = str.substring(prefixString.length());

        //Retrive the data related to the first confluence node (a node with two or more incoming transitions)
        //in the transition path from sourceNode corresponding to prefixString.
        HashMap<String, Object> firstConfluenceNodeDataHashMap = getTransitionPathFirstConfluenceNodeData(sourceNode, prefixString);
        DawgNode firstConfluenceNodeInPrefix = (DawgNode) firstConfluenceNodeDataHashMap.get("confluenceNode");
        Integer toFirstConfluenceNodeTransitionCharIndex = (Integer) firstConfluenceNodeDataHashMap.get("toConfluenceNodeTransitionCharIndex");
        

        //Remove the register entries of all the nodes in the prefixString transition path up to the first confluence node
        //(those past the confluence node will not need to be removed since they will be cloned and unaffected by the
        //addition of suffixString). If there is no confluence node in prefixString, then remove the register entries in prefixString's entire transition path
        removeTransitionPathRegisterEntries((toFirstConfluenceNodeTransitionCharIndex == null ? prefixString : prefixString.substring(0, toFirstConfluenceNodeTransitionCharIndex)));

        //If there is a confluence node in the prefix, we must duplicate the transition path
        //of the prefix starting from that node, before we add suffixString (to the duplicate path).
        //This ensures that we do not disturb the other transition paths containing this node.
        if (firstConfluenceNodeInPrefix != null) {
            String transitionStringOfPathToFirstConfluenceNode = prefixString.substring(0, toFirstConfluenceNodeTransitionCharIndex + 1);
            String transitionStringOfToBeDuplicatedPath = prefixString.substring(toFirstConfluenceNodeTransitionCharIndex + 1);
            cloneTransitionPath(firstConfluenceNodeInPrefix, transitionStringOfPathToFirstConfluenceNode, transitionStringOfToBeDuplicatedPath);
        }
        

        //Add the transition based on suffixString to the end of the (possibly duplicated) transition path corresponding to prefixString
        addTransitionPath(sourceNode.transition(prefixString), suffixString);
    }

    /**
     * Creates a SimpleDawgNode version of an DawgNode's outgoing transition set in mdagDataArray.
     *
     * @param node the DawgNode containing the transition set to be inserted in to
     * {@code mdagDataArray}
     * @param mdagDataArray an array of SimpleDawgNodes containing a subset of the data of the Dawg
     * @param onePastLastCreatedConnectionSetIndex an int of the index in {@code mdagDataArray} that
     * the outgoing transition set of {@code node} is to start from
     * @return an int of one past the end of the transition set located farthest in
     * {@code mdagDataArray}
     */
    private int createSimpleDawgTransitionSet(DawgNode node, SimpleDawgNode[] mdagDataArray, int onePastLastCreatedTransitionSetIndex) {
        int pivotIndex = onePastLastCreatedTransitionSetIndex;
        node.setTransitionSetBeginIndex(pivotIndex);

        onePastLastCreatedTransitionSetIndex += node.getOutgoingTransitionCount();

        //Create a SimpleDawgNode representing each transition label/target combo in transitionTreeMap, recursively calling this method (if necessary)
        //to set indices in these SimpleDawgNodes that the set of transitions emitting from their respective transition targets starts from.
        TreeMap<Byte, DawgNode> transitionTreeMap = node.getOutgoingTransitions();
        for (Entry<Byte, DawgNode> transitionKeyValuePair : transitionTreeMap.entrySet()) {
            //Use the current transition's label and target node to create a SimpleDawgNode
            //(which is a space-saving representation of the transition), and insert it in to mdagDataArray
            byte transitionLabelChar = transitionKeyValuePair.getKey();
            DawgNode transitionTargetNode = transitionKeyValuePair.getValue();
            mdagDataArray[pivotIndex] = new SimpleDawgNode(transitionLabelChar, transitionTargetNode.isAcceptNode(), transitionTargetNode.getOutgoingTransitionCount());
            

            //If targetTransitionNode's outgoing transition set hasn't been inserted in to mdagDataArray yet, call this method on it to do so.
            //After this call returns, transitionTargetNode will contain the index in mdagDataArray that its transition set starts from
            if (transitionTargetNode.getTransitionSetBeginIndex() == -1) {
                onePastLastCreatedTransitionSetIndex = createSimpleDawgTransitionSet(transitionTargetNode, mdagDataArray, onePastLastCreatedTransitionSetIndex);
            }

            mdagDataArray[pivotIndex++].setTransitionSetBeginIndex(transitionTargetNode.getTransitionSetBeginIndex());
        }
        

        return onePastLastCreatedTransitionSetIndex;
    }

    /**
     * Creates a space-saving version of the Dawg in the form of an array. Once the Dawg is
     * simplified, Strings can no longer be added to or removed from it.
     */
    private void simplify() {
        if (sourceNode != null) {
            mdagDataArray = new SimpleDawgNode[transitionCount];
            createSimpleDawgTransitionSet(sourceNode, mdagDataArray, 0);
            simplifiedSourceNode = new SimpleDawgNode((byte) '\0', false, sourceNode.getOutgoingTransitionCount());

            //Mark the previous Dawg data structure and equivalenceClassDawgNodeHashMap
            //for garbage collection since they are no longer needed.
            sourceNode = null;
            equivalenceClassDawgNodeHashMap = null;
            
        }
    }

    /**
     * Determines whether a String is present in the Dawg.
     *
     * @param str the String to be searched for
     * @return true if {@code str} is present in the Dawg, and false otherwise
     */
    public boolean contains(String str) {
        SimpleDawgNode targetNode = SimpleDawgNode.traverseDawg(mdagDataArray, simplifiedSourceNode, str.toUpperCase());
        return (targetNode != null && targetNode.endsWord());
    }

    public Set<String> getAllStrings() {
        return getStringsWith("".toCharArray(), 15);
    }

    public Set<String> getStringsWith(char[] tiles) {
        return getStringsWith(tiles, 0);
    }

    public Set<String> getStringsWith(char[] tiles, int wildCardNum) {
        // Turn char array to deque with characters
        Deque<Character> tileList = new ArrayDeque<>(tiles.length + wildCardNum);
        if (wildCardNum < 0) {
            throw new IndexOutOfBoundsException("Less than 0 wildCards makes no sense");
        }
        for (; wildCardNum > 0; wildCardNum--) {
            tileList.add('*');
        }
        for (char tile : tiles) {
            if (tile >= 'A') {
                tileList.add(Character.toUpperCase(tile));
            }
        }
        return getStringsWith(tileList, simplifiedSourceNode, new HashSet<>(), "");
    }

    private Set<String> getStringsWith(Deque<Character> tileList, SimpleDawgNode currentNode, Set<String> strSet, String subString) {
        SimpleDawgNode newNode;
        char letter;
        for (int i = 0; i < tileList.size(); i++) {
            letter = tileList.removeFirst();
            if (letter == '*') {
                int transitionSetEndIndex = currentNode.getTransitionSetBeginIndex() + currentNode.getOutgoingTransitionSetSize() - 1;
                for (int j = currentNode.getTransitionSetBeginIndex(); j <= transitionSetEndIndex; j++) {
                    newNode = mdagDataArray[j];
                    String newSubstring = subString + letter;
                    if (newNode.endsWord()) {
                        strSet.add(newSubstring);
                    }
                    getStringsWith(tileList, newNode, strSet, newSubstring);
                }
            }
            newNode = currentNode.transition(mdagDataArray, letter);
            if (newNode != null) {
                String newSubstring = subString + letter;
                if (newNode.endsWord()) {
                    strSet.add(newSubstring);
                }
                getStringsWith(tileList, newNode, strSet, newSubstring);
            }
            tileList.addLast(letter);
        }
        return strSet;
    }

    public Set<Tupple<String, Integer>> getWordsWithHook(char hook, char[] tiles) {
        Set<Tupple<String, Integer>> wordsSet = new HashSet<>();
        if (hook < 'A') {
            for (String word : getStringsWith(tiles)) {
                wordsSet.add(new Tupple<>(word, 0));
            }
        } else {
            Deque<Character> tileList = new ArrayDeque<>(tiles.length + 1);
            for (char tile : tiles) {
                if (tile >= 'A') {
                    tileList.add(Character.toUpperCase(tile));
                }
            }
            tileList.add(hook);
            getWordsWithHook(simplifiedSourceNode, wordsSet, hook, tileList, new StringBuilder(8), new ArrayList<>(128));
        }
        return wordsSet;
    }

    private void getWordsWithHook(SimpleDawgNode currentNode, Set<Tupple<String, Integer>> wordSet, char hook, Deque<Character> tileList, StringBuilder subString, List<Integer> indices) {
        SimpleDawgNode newNode;
        char letter;
        boolean newHook = false;
        for (int i = 0; i < tileList.size(); i++) {
            letter = tileList.removeFirst();
            if (letter == '*') {
                int transitionSetEndIndex = currentNode.getTransitionSetBeginIndex() + currentNode.getOutgoingTransitionSetSize() - 1;
                for (int j = currentNode.getTransitionSetBeginIndex(); j <= transitionSetEndIndex; j++) {
                    newNode = mdagDataArray[j];
                    helper(wordSet, newNode, tileList, subString, hook, indices);
                }
            } else {
                newNode = currentNode.transition(mdagDataArray, letter);
                if (newNode != null) {
                    helper(wordSet, newNode, tileList, subString, hook, indices);
                }
            }

            if (newHook) {
                indices.remove(indices.size() - 1);
            }
            tileList.addLast(letter);
        }
    }

    private void helper(Set<Tupple<String, Integer>> wordSet, SimpleDawgNode newNode, Deque<Character> tileList, StringBuilder subString, char hook, List<Integer> indices) {
        boolean newHook = false;
        subString.append((char) newNode.getLetter());
        if (newNode.endsWord()) {
            for (Integer index : indices) {
                wordSet.add(new Tupple<>(subString.toString(), index));
            }
        }
        if (subString.charAt(subString.length() - 1) == hook) {
            indices.add(subString.length() - 1);
            newHook = true;
        }
        getWordsWithHook(newNode, wordSet, hook, tileList, subString, indices);
        subString.deleteCharAt(subString.length() - 1);
        if (newHook) {
            indices.remove(indices.size() - 1);
        }
    }

    public SimpleDawgNode[] getNodeArray() {
        return mdagDataArray;
    }

    public SimpleDawgNode getSourceNode() {
        return simplifiedSourceNode;
    }

    public int size() {
        return stringCount;
    }

    public int getTransitionCount() {
        return transitionCount;
    }
}
