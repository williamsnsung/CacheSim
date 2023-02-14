import java.util.LinkedHashSet;

class LinkedListNode {
    private int index;
    private LinkedListNode prev;
    private LinkedListNode next;

    private int freq;

    public LinkedListNode() {
    }

    public LinkedListNode(int index) {
        this.index = index;
    }

    public LinkedListNode(int index, int freq) {
        this.freq = freq;
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

    public int getFreq() {
        return this.freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }
}

class DoublyLinkedList {
    private LinkedListNode tail;
    private LinkedHashSet<LinkedListNode> nodes;

    public DoublyLinkedList() {
        tail = new LinkedListNode();
        nodes = new LinkedHashSet<>();
    }

    public boolean contains(LinkedListNode node) {
        if (nodes.contains(node)) {
            return true;
        }
        return false;
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
