package Dawg;

import java.util.Map;
import java.util.TreeMap;
import java.util.Stack;

/**
 *
 * @author Kevin
 */
public class DawgNode {

    public int id;

    //The boolean denoting the accept state status of this node
    private boolean isAcceptNode;

    //The TreeMap to containing entries that represent a transition (label and target node)
    private final TreeMap<Byte, DawgNode> outgoingTransitionTreeMap;

    //The int representing this node's incoming transition node count
    private int incomingTransitionCount = 0;

    //The int denoting index in a simplified dawg data array that this node's transition set begins at
    private int transitionSetBeginIndex = -1;

    //The int which will store this node's hash  after its been calculated (necessary due to how expensive the hashing calculation is)
    private Integer storedHashCode = null;

    /**
     * Constructs an DawgNode.
     *
     * @param isAcceptNode a boolean denoting the accept state status of this node
     */
    public DawgNode(boolean isAcceptNode) {
        this.isAcceptNode = isAcceptNode;
        outgoingTransitionTreeMap = new TreeMap<>();
    }

    /**
     * Constructs an DawgNode possessing the same accept state status and outgoing transitions as
     * another.
     *
     * @param node the DawgNode possessing the accept state status and outgoing transitions that the
     * to-be-created DawgNode is to take on
     */
    private DawgNode(DawgNode node) {
        isAcceptNode = node.isAcceptNode;
        outgoingTransitionTreeMap = new TreeMap<>(node.outgoingTransitionTreeMap);

        //Loop through the nodes in this node's outgoing transition set, incrementing the number of
        //incoming transitions of each by 1 (to account for this newly created node's outgoing transitions)
        for (Map.Entry<Byte, DawgNode> transitionKeyValuePair : outgoingTransitionTreeMap.entrySet()) {
            transitionKeyValuePair.getValue().incomingTransitionCount++;

        }
    }

    public DawgNode(boolean isAcceptNode, int id) {
        this.id = id;
        this.isAcceptNode = isAcceptNode;
        outgoingTransitionTreeMap = new TreeMap<>();
    }

    private DawgNode(DawgNode node, int id) {
        this.id = id;
        isAcceptNode = node.isAcceptNode;
        outgoingTransitionTreeMap = new TreeMap<>(node.outgoingTransitionTreeMap);

        for (Map.Entry<Byte, DawgNode> transitionKeyValuePair : outgoingTransitionTreeMap.entrySet()) {
            transitionKeyValuePair.getValue().incomingTransitionCount++;
        }
    }

    /**
     * Creates an DawgNode possessing the same accept state status and outgoing transitions as this
     * node.
     *
     * @return an DawgNode possessing the same accept state status and outgoing transitions as this
     * node
     */
    @Override
    public DawgNode clone(){
        DawgNode clone = new DawgNode(this);
        return clone;
    }

    /**
     * Creates an DawgNode possessing the same accept state status ant transition set (incoming &
     * outgoing) as this node. outgoing transitions as this node.
     *
     * @param soleParentNode the DawgNode possessing the only transition that targets this node
     * @param parentToCloneTransitionLabelChar the char which labels the transition from
     * {@ soleParentNode} to this node
     * @return an DawgNode possessing the same accept state status and transition set as this node.
     */
    public DawgNode clone(DawgNode soleParentNode, Byte parentToCloneTransitionLabelChar) {
        DawgNode cloneNode = new DawgNode(this);
        soleParentNode.reassignOutgoingTransition(parentToCloneTransitionLabelChar, this, cloneNode);

        return cloneNode;
    }

    /**
     * Retrieves the index in a simplified dawg data array that the SimpleDawgNode representation of
     * this node's outgoing transition set begins at.
     *
     * @return the index in a simplified dawg data array that this node's transition set begins at,
     * or -1 if its transition set is not present in such an array
     */
    public int getTransitionSetBeginIndex() {
        return transitionSetBeginIndex;
    }

    public Map.Entry<Byte, DawgNode> getLastTransition() {
        return outgoingTransitionTreeMap.lastEntry();
    }

    /**
     * Retrieves this node's outgoing transition count.
     *
     * @return an int representing this node's number of outgoing transitions
     */
    public int getOutgoingTransitionCount() {
        return outgoingTransitionTreeMap.size();
    }

    /**
     * Retrieves this node's incoming transition count
     *
     * @return an int representing this node's number of incoming transitions
     */
    public int getIncomingTransitionCount() {
        return incomingTransitionCount;
    }

    /**
     * Determines if this node is a confluence node (defined as a node with two or more incoming
     * transitions
     *
     * @return true if this node has two or more incoming transitions, false otherwise
     */
    public boolean isConfluenceNode() {
        return (incomingTransitionCount > 1);
    }

    /**
     * Retrieves the accept state status of this node.
     *
     * @return true if this node is an accept state, false otherwise
     */
    public boolean isAcceptNode() {
        return isAcceptNode;
    }

    /**
     * Sets this node's accept state status.
     *
     * @param isAcceptNode a boolean representing the desired accept state status
     */
    public void setAcceptStateStatus(boolean isAcceptNode) {
        this.isAcceptNode = isAcceptNode;
    }

    /**
     * Records the index that this node's transition set starts at in an array containing this
     * node's containing Dawg data (simplified Dawg).
     *
     * @param transitionSetBeginIndex a transition set
     */
    public void setTransitionSetBeginIndex(int transitionSetBeginIndex) {
        this.transitionSetBeginIndex = transitionSetBeginIndex;
    }

    /**
     * Determines whether this node has an outgoing transition with a given label.
     *
     * @param letter the char labeling the desired transition
     * @return true if this node possesses a transition labeled with {@ letter}, and false
     * otherwise
     */
    public boolean hasOutgoingTransition(byte letter) {
        return outgoingTransitionTreeMap.containsKey(letter);
    }

    /**
     * Determines whether this node has any outgoing transitions.
     *
     * @return true if this node has at least one outgoing transition, false otherwise
     */
    public boolean hasOutgoingTransitions() {
        return !outgoingTransitionTreeMap.isEmpty();
    }

    /**
     * Follows an outgoing transition of this node labeled with a given char.
     *
     * @param letter the char representation of the desired transition's label
     * @return the DawgNode that is the target of the transition labeled with letter, or
     * null if there is no such labeled transition from this node
     */
    public DawgNode transition(byte letter) {
        return outgoingTransitionTreeMap.get(letter);
    }

    /**
     * Follows a transition path starting from this node.
     *
     * @param str a String corresponding a transition path in the Dawg
     * @return the DawgNode at the end of the transition path corresponding to {@ str}, or null
     * if such a transition path is not present in the Dawg
     */
    public DawgNode transition(String str) {
        int charCount = str.length();
        DawgNode currentNode = this;

        //Iteratively transition through the Dawg using the chars in str
        for (int i = 0; i < charCount; i++) {
            currentNode = currentNode.transition((byte) str.charAt(i));
            if (currentNode == null) {
                break;
            }
        }


        return currentNode;
    }

    /**
     * Retrieves the nodes in the transition path starting from this node corresponding to a given
     * String .
     *
     * @param str a String corresponding to a transition path starting from this node
     * @return a Stack of DawgNodes containing the nodes in the transition path denoted by
     * {@ str}, in the order they are encountered in during transitioning
     */
    public Stack<DawgNode> getTransitionPathNodes(String str) {
        Stack<DawgNode> nodeStack = new Stack<>();

        DawgNode currentNode = this;
        int numberOfChars = str.length();

        //Iteratively transition through the Dawg using the chars in str,
        //putting each encountered node in nodeStack
        for (int i = 0; i < numberOfChars && currentNode != null; i++) {
            currentNode = currentNode.transition((byte) str.charAt(i));
            nodeStack.add(currentNode);
        }


        return nodeStack;
    }

    /**
     * Retrieves this node's outgoing transitions.
     *
     * @return a TreeMap containing entries collectively representing all of this node's outgoing
     * transitions
     */
    public TreeMap<Byte, DawgNode> getOutgoingTransitions() {
        return outgoingTransitionTreeMap;
    }

    /**
     * Decrements the incoming transition counts of all of the nodes that are targets of
     * outgoing transitions from this node.
     */
    public void decrementTargetIncomingTransitionCounts() {
        for (Map.Entry<Byte, DawgNode> transitionKeyValuePair : outgoingTransitionTreeMap.entrySet()) {
            transitionKeyValuePair.getValue().incomingTransitionCount--;
        }
    }

    /**
     * Reassigns the target node of one of this node's outgoing transitions.
     *
     * @param letter the char which labels the outgoing transition of interest
     * @param oldTargetNode the DawgNode that is currently the target of the transition of interest
     * @param newTargetNode the DawgNode that is to be the target of the transition of interest
     */
    public void reassignOutgoingTransition(byte letter, DawgNode oldTargetNode, DawgNode newTargetNode) {
        oldTargetNode.incomingTransitionCount--;
        newTargetNode.incomingTransitionCount++;

        outgoingTransitionTreeMap.put(letter, newTargetNode);
    }

    /**
     * Creates an outgoing transition labeled with a given char that has a new node as its target.
     *
     * @param letter a char representing the desired label of the transition
     * @param targetAcceptStateStatus a boolean representing to-be-created transition target node's
     * accept status
     * @return the (newly created) DawgNode that is the target of the created transition
     */
    public DawgNode addOutgoingTransition(byte letter, boolean targetAcceptStateStatus) {
        DawgNode newTargetNode = new DawgNode(targetAcceptStateStatus);
        newTargetNode.incomingTransitionCount++;

        outgoingTransitionTreeMap.put(letter, newTargetNode);
        return newTargetNode;
    }

    public DawgNode addOutgoingTransition(byte letter, boolean isEndOfWord, int id) {
        DawgNode newTargetNode = new DawgNode(isEndOfWord, id);
        newTargetNode.incomingTransitionCount++;
        newTargetNode.id = id;

        outgoingTransitionTreeMap.put(letter, newTargetNode);
        return newTargetNode;
    }

    /**
     * Removes a transition labeled with a given char. This only removes the connection between this
     * node and the transition's target node; the target node is not deleted.
     *
     * @param letter the char labeling the transition of interest
     */
    public void removeOutgoingTransition(byte letter) {
        outgoingTransitionTreeMap.remove(letter);
    }

    /**
     * Determines whether the sets of transition paths from two DawgNodes are equivalent. This is an
     * expensive operation.
     *
     * @param node1
     * @param node2 a TreeMap containing entries collectively representing all of a node's outgoing
     * transitions a TreeMap containing entries collectively representing all of a node's outgoing
     * transitions
     * @return if the set of transition paths from node1 and node2 are equivalent
     */
    public static boolean haveSameTransitions(DawgNode node1, DawgNode node2) {
        TreeMap<Byte, DawgNode> outgoingTransitionTreeMap1 = node1.outgoingTransitionTreeMap;
        TreeMap<Byte, DawgNode> outgoingTransitionTreeMap2 = node2.outgoingTransitionTreeMap;

        if (outgoingTransitionTreeMap1.size() != outgoingTransitionTreeMap2.size()) {
            return false;
        }

        //For each transition in outgoingTransitionTreeMap1, get the identically lableed transition
        //in outgoingTransitionTreeMap2 (if present), and test the equality of the transitions' target nodes
        for (Map.Entry<Byte, DawgNode> transitionKeyValuePair : outgoingTransitionTreeMap1.entrySet()) {
            Byte currentCharKey = transitionKeyValuePair.getKey();
            DawgNode currentTargetNode = transitionKeyValuePair.getValue();

            if (!outgoingTransitionTreeMap2.containsKey(currentCharKey) || !outgoingTransitionTreeMap2.get(currentCharKey).equals(currentTargetNode)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clears this node's stored hash value
     */
    public void clearStoredHashCode() {
        storedHashCode = null;
    }

    /**
     * Evaluates the equality of this node with another object. This node is equal to obj if and
     * only if obj is also an DawgNode, and the set of transitions paths from this node and obj are
     * equivalent.
     *
     * @param obj an object
     * @return true of {@ obj} is an DawgNode and the set of transition paths from this node and
     * obj are equivalent
     */
    @Override
    public boolean equals(Object obj) {
        boolean areEqual = (this == obj);

        if (!areEqual && obj != null && obj.getClass().equals(DawgNode.class)) {
            DawgNode node = (DawgNode) obj;
            areEqual = (isAcceptNode == node.isAcceptNode && haveSameTransitions(this, node));
        }

        return areEqual;
    }

    /**
     * Hashes this node using its accept state status and set of outgoing transition paths. This is
     * an expensive operation, so the result is cached and only cleared when necessary.
     *
     * @return an int of this node's hash
     */
    @Override
    public int hashCode() {

        if (storedHashCode == null) {
            int hash = 7;
            hash = 53 * hash + (this.isAcceptNode ? 1 : 0);
            hash = 53 * hash + (this.outgoingTransitionTreeMap != null ? this.outgoingTransitionTreeMap.hashCode() : 0);    //recursively hashes the nodes in all the
            //transition paths stemming from this node
            storedHashCode = hash;
            return hash;
        } else {
            return storedHashCode;
        }
    }
}
