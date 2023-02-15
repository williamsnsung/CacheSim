import java.util.LinkedHashSet;

class LinkedListNode {
    private int index;
    private LinkedListNode prev;
    private LinkedListNode next;

    public LinkedListNode() {
    }

    public LinkedListNode(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public LinkedListNode getPrev() {
        return this.prev;
    }

    public void setPrev(LinkedListNode prev) {
        this.prev = prev;
    }

    public LinkedListNode getNext() {
        return this.next;
    }

    public void setNext(LinkedListNode next) {
        this.next = next;
    }
}

class DoublyLinkedList {
    private LinkedListNode tail;
    private final LinkedHashSet<LinkedListNode> nodes;

    public DoublyLinkedList() {
        tail = new LinkedListNode();
        nodes = new LinkedHashSet<>();
    }

    public void remove(LinkedListNode node) {
        if (node.getPrev() != null) {
            node.getPrev().setNext(node.getNext());
        }
        if (node.getNext() != null) {
            node.getNext().setPrev(node.getPrev());
        }
        this.nodes.remove(node);
    }

    public void append(LinkedListNode node) {
        tail.setNext(node);
        node.setPrev(tail);
        tail = node;
        nodes.add(node);
    }

    public LinkedListNode getHead() {
        for (LinkedListNode node : this.nodes) {
            return node;
        }
        return new LinkedListNode();
    }
}

class LFUNode {
    private final int cacheLine;
    private int freq;
//TODO multilevel cache
    public LFUNode(int cacheLine, int freq) {
        this.cacheLine = cacheLine;
        this.freq = freq;
    }

    public int getCacheLine() {
        return this.cacheLine;
    }

    public int getFreq() {
        return this.freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }
}