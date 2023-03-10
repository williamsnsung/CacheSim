import java.util.*;

class LRU {
    private final LinkedHashSet<Integer>[] lru;                  // Linked HashSet of Linked List Nodes, the index being a set number

    /**
     * Constructor for an LRU Cache, creates one for each set in the cache
     * @param setCount The number of sets in the cache
     */
    public LRU(int setCount) {
        this.lru = new LinkedHashSet[setCount];
        for (int i = 0; i < setCount; i++) {
            this.lru[i] = new LinkedHashSet<>();
        }
    }

    /**
     * Updates the LRU Cache when told
     * @param index     The index of a given set
     * @param cacheLine The tag we would like to update
     */
    public void update(int index, int cacheLine) {
        this.lru[index].remove(cacheLine);
        this.lru[index].add(cacheLine);
    }

    /**
     * Removes the given cache line from the specified set
     * @param index     The set index
     * @param cacheLine The tag to remove
     */
    public void remove(int index, int cacheLine) {
        this.lru[index].remove(cacheLine);
    }

    /**
     * Returns the head of the linked list for the LRU Cache, otherwise known as the LRU cache line
     * @param index The set to get the head of
     * @return      The node of the head of the linked list
     */
    public int getHead(int index) {
        Iterator<Integer> iter = this.lru[index].iterator();
        return iter.next();
    }
}

class LFU {
    private final HashMap<Integer, LFUNode>[] nodeMap;              // An array of hashmaps, index representing a set, key being a cache line, value being a node
                                                                    // Custom node is needed so that we can find the node in a frequency in constant time by storing its frequency
    private final HashMap<Integer, TreeSet<LFUNode>>[] freqSetMap;  // An array of hashmaps, index representing a set, key being a frequency, value being an ordered set
                                                                    // A TreeSet was chosen for storing the nodes as we need to order by cache line for tie breaking
                                                                    // Using a TreeSet allows us to find, insert, and remove whilst maintaining an order in log(n) time
    private final int[] minFreqMap;                                 // An array, each index representing a set, value being the minimum frequency for that set

    /**
     * Constructor for an LFU Cache, creates one for each set in the cache
     * @param setCount The number of sets in the cache
     */
    public LFU(int setCount) {
        this.nodeMap = new HashMap[setCount];
        this.freqSetMap = new HashMap[setCount];
        this.minFreqMap = new int[setCount];
        for (int i = 0; i < setCount; i++) {
            this.nodeMap[i] = new HashMap<>();
            this.freqSetMap[i] = new HashMap<>();
        }
    }

    /**
     * Updates a cache line in a given set, either by inserting it into its appropriate frequency, or increasing its frequency
     * @param index         The index of the set to update
     * @param cacheLine     The cache line to be updated
     */
    protected void update(int index, int cacheLine) {
        // if already cached, move cache line into a higher frequency queue
        if (this.nodeMap[index].containsKey(cacheLine)) {
            LFUNode node = this.nodeMap[index].get(cacheLine);
            int freq = node.getFreq();
            this.freqSetMap[index].get(freq).remove(node);
            if (this.minFreqMap[index] == freq && this.freqSetMap[index].get(freq).size() == 0) {
                this.freqSetMap[index].remove(freq);
                this.minFreqMap[index]++;
            }
            node.setFreq(++freq);
            this.appendFreqNode(index, freq, node);
        }
        // otherwise create a new node to represent the cache line and insert it into the frequency 1 queue
        else {
            LFUNode node = new LFUNode(cacheLine, 1);
            this.nodeMap[index].put(cacheLine, node);
            this.appendFreqNode(index, 1, node);
            this.minFreqMap[index] = 1;
        }
    }

    /**
     * Removes a cache line from the LFU cache
     * @param index         The set to remove from
     * @param cacheLine     The cache line to remove
     */
    public void remove(int index, int cacheLine) {
        LFUNode node = this.nodeMap[index].get(cacheLine);
        this.freqSetMap[index].get(node.getFreq()).remove(node);
        this.nodeMap[index].remove(cacheLine);
    }

    /**
     * Creates a new TreeSet for a frequency if one does not exist, then inserts the given node into the TreeSet
     * @param index We want to update the LRU for the given set
     * @param freq  The frequency we want to add the node into
     * @param node  The node we want to add
     */
    protected void appendFreqNode(int index, int freq, LFUNode node) {
        if (!this.freqSetMap[index].containsKey(freq)) {
            this.freqSetMap[index].put(freq, new TreeSet<>(Comparator.comparing(LFUNode::getCacheLine)));   // tie breaking on cache line index
        }
        this.freqSetMap[index].get(freq).add(node);
    }

    /**
     * Returns the least frequently used cache line in the LFU cache
     * @param index The index we want to find the LFU cache line for
     * @return      The tag of the LFU cache line
     */
    public int getLFUCacheLine(int index) {
        int minFreq = this.minFreqMap[index];
        LFUNode lfuNode = this.freqSetMap[index].get(minFreq).first();
        return lfuNode.getCacheLine();
    }
}
