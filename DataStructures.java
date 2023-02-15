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