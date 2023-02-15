import java.util.LinkedHashSet;

// Class for a node in a linked list used for making the LRU Cache
class LinkedListNode {
    private int index;              // The index of a cache line in the current cache
    private LinkedListNode prev;    // The previous node in the linked list
    private LinkedListNode next;    // The next node in the linked list

    /**
     * Constructor which creates an empty linked node
     */
    public LinkedListNode() {
    }

    /**
     * Constructor which creates a linked node, storing the index of the given cache line in the current set
     * @param index The index of the cache line
     */
    public LinkedListNode(int index) {
        this.index = index;
    }

    /**
     * Returns the index of the cache line stored by the current node
     * @return  An integer
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Returns the previous node in the linked list
     * @return A LinkedListNode object
     */
    public LinkedListNode getPrev() {
        return this.prev;
    }

    /**
     * Sets the current node's previous node to the LinkedListNode given
     * @param prev  The linked list node to set the previous to
     */
    public void setPrev(LinkedListNode prev) {
        this.prev = prev;
    }

    /**
     * Returns the next LinkedListNode in the linked list
     * @return A LinkedListNode object
     */
    public LinkedListNode getNext() {
        return this.next;
    }

    /**
     * Sets the current node's next node to the node given
     * @param next  The next node you would like the current node to have
     */
    public void setNext(LinkedListNode next) {
        this.next = next;
    }
}

// Class for a doubly linked list used for making the LRU Cache
class DoublyLinkedList {
    private LinkedListNode tail;                        // The tail of the linked list
    private final LinkedHashSet<LinkedListNode> nodes;  // THe nodes in the linked list, in order of insertion

    /**
     * Constructor which creates an empty linked list
     */
    public DoublyLinkedList() {
        tail = new LinkedListNode();
        nodes = new LinkedHashSet<>();
    }

    /**
     * Removes the given node from the linked list
     * @param node
     */
    public void remove(LinkedListNode node) {
        // links the previous and next node of the given node together
        if (node.getPrev() != null) {
            node.getPrev().setNext(node.getNext());
        }
        if (node.getNext() != null) {
            node.getNext().setPrev(node.getPrev());
        }
        // removes the current node from the hash set of nodes
        this.nodes.remove(node);
    }

    /**
     * Adds the given node to the end of the linked list
     * @param node  The node to add to the end of the list
     */
    public void append(LinkedListNode node) {
        // sets the tail node to have the new node as next, and vice versa
        tail.setNext(node);
        node.setPrev(tail);
        tail = node;
        // add the node to the set of nodes
        nodes.add(node);
    }

    /**
     * Returns the head of the linked list
     * @return  The head of the linked list, empty node if list is empty
     */
    public LinkedListNode getHead() {
        for (LinkedListNode node : this.nodes) {
            return node;
        }
        return new LinkedListNode();
    }
}

// Class to represent a node in the TreeSet used to store the nodes for a given frequency
class LFUNode {
    private final int cacheLine;    // The cache line index for the current node
    private int freq;               // The frequency that the cache line has been used in the linked list

    /**
     * Constructor to create a new node in the TreeSet to store the cache line index and frequency that that cache line has been accessed
     * @param cacheLine The index of the cache line to store
     * @param freq      The frequency of how much this cache line has been used
     */
    public LFUNode(int cacheLine, int freq) {
        this.cacheLine = cacheLine;
        this.freq = freq;
    }

    /**
     * Returns the index of the cache line stored by the current node
     * @return  The cache line index stored by this node
     */
    public int getCacheLine() {
        return this.cacheLine;
    }

    /**
     * Returns the frequency that this current node has been accessed
     * @return The frequency that this node has been used
     */
    public int getFreq() {
        return this.freq;
    }

    /**
     * Sets the frequency of the current node to the frequency given
     * @param freq  The frequency to set the current node to, an integer
     */
    public void setFreq(int freq) {
        this.freq = freq;
    }
}