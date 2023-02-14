import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * Abstract class for caches from which all other caches are based off of
 */
abstract class Cache {
    protected String name;
    protected int size;
    protected int lineSize;
    protected int setCount;
    protected int setSize;
    protected String replacementPolicy;
    protected int hits;
    protected int misses;
    protected int offsetBitShift;
    protected int indexMask;
    protected int tagBitShift;
    protected long[][] entries;  // Key value pair of an index and tag(s) for a given cache line
    protected int[] cacheLinePtr;
    protected int[] setCapacity;
    boolean last;

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getLineSize() {
        return lineSize;
    }

    public int getSetCount() {
        return setCount;
    }

    public String getReplacementPolicy() {
        return replacementPolicy;
    }

    public int getHits() {
        return hits;
    }

    public int getMisses() {
        return misses;
    }

    public int getOffsetBitShift() {
        return offsetBitShift;
    }

    public int getIndexMask() {
        return indexMask;
    }

    public int getTagBitShift() {
        return tagBitShift;
    }

    public long[][] getEntries() {
        return entries;
    }

    public int getSetSize() {
        return this.setSize;
    }

    public void setSetSize(int setSize) {
        this.setSize = setSize;
    }

    public int[] getSetCapacity() {
        return this.setCapacity;
    }

    public void setSetCapacity(int[] setCapacity) {
        this.setCapacity = setCapacity;
    }

    /**
     * Constructor for a cache, initialises the data structures used along with assigning the given parameters to its attributes
     * @param name                  name of the cache
     * @param size                  size of the cache
     * @param lineSize              size of a line in the cache
     * @param setCount               size of a set in the cache
     * @param replacementPolicy     the replacement policy for this cache
     */
    public Cache(String name, int size, int lineSize, int setCount, String replacementPolicy, boolean last) {
        this.name = name;
        this.size = size;
        this.lineSize = lineSize;
        this.setCount = setCount;
        this.replacementPolicy = replacementPolicy;
        this.hits = 0;
        this.misses = 0;
        this.setSize = size / lineSize / setCount;
        this.entries = new long[setCount][this.setSize];
        this.cacheLinePtr = new int[setCount];
        this.setCapacity = new int[setCount];
        this.last = last;
    }

    /**
     * Finds the base 2 log of a number
     * @param n the number to be logged
     * @return the result of the log operation
     */
    // https://www.geeksforgeeks.org/how-to-calculate-log-base-2-of-an-integer-in-java/ [12/02/2023]
    public int log2(int n) {
        return (int) (Math.log(n)/ Math.log(2));
    }

    /**
     * Prints the config of the current cache
     */
    public void printConfig() {
        System.out.println("name: " + this.name);
        System.out.println("size: " + this.size);
        System.out.println("lineSize: " + this.lineSize);
        System.out.println("setCount: " + this.setCount);
        System.out.println("replacementPolicy: " + this.replacementPolicy);
        System.out.println();
    }

    //TODO fix documentation
    /**
     * Checks if the current tag is cached for a given index, adding to the hit/miss count appropriately.
     * Loads the tag into the current cache if it's last in the hierarchy, as you would then be retrieving from memory
     * @param memAddr
     * @return
     */
    public boolean checkCache(long memAddr) {
        CacheLine cacheLine = this.getCacheLine(memAddr);
        int index = cacheLine.getIndex();
        long tag = cacheLine.getTag();
        if (this.find(index, tag)) {
            this.hits++;
            return true;
        }

        this.misses++;
        if (this.setCapacity[index] < this.setSize) {
            this.insert(index, tag);
            return true;
        }
        else if (this.last) {
            this.evict(index);
            this.insert(index, tag);
        }
        return false;
    }

    /**
     * Abstract method where the eviction policy for a cache is defined
     * @param index the index to evict from for a given cache
     */
    abstract void evict(int index);
    abstract void insert(int index, long tag);
    abstract boolean find(int index, long tag);

    abstract CacheLine getCacheLine(long memAddr);
}

class CacheLine {
    private int index;
    private long tag;

    public CacheLine(int index, long tag) {
        this.index = index;
        this.tag = tag;
    }

    public int getIndex() {
        return index;
    }

    public long getTag() {
        return tag;
    }
}
class DirectMapped extends Cache {

    /**
     * Constructor for a directly mapped cache.
     * In addition to the given parameters, it finds how many bits to shift from the offset to get to the index, and the tag after that,
     * along with the necessary bitmask for the index.
     *
     * @param name The name of the cache
     * @param size The size of the cache
     * @param lineSize The size of a line in the cache
     * @param last If this cache is last in the hierarchy
     */
    public DirectMapped(String name, int size, int lineSize, boolean last) {
        super(name, size, lineSize, size / lineSize, "direct", last);
        int indexBits =  log2(size / lineSize);
        this.offsetBitShift = log2(lineSize);
        this.tagBitShift = this.offsetBitShift + indexBits;
        for (int i = 0; i < indexBits; i++) {
            this.indexMask += Math.pow(2, i);
        }
    }

    public CacheLine getCacheLine(long memAddr) {
        int index = (int) (memAddr >> this.getOffsetBitShift() & this.getIndexMask());
        long tag = memAddr >> this.getTagBitShift();
        return new CacheLine(index, tag);
    }

    /**
     * Removes the current tag in the cache line at the given index
     * @param index the index to evict from for a given cache
     */
    protected void evict(int index) {
        this.entries[index][0] = -1;
        this.cacheLinePtr[index] = 0;
        this.setCapacity[index] = 0;
    }

    protected void insert(int index, long tag) {
        this.entries[index][0] = tag;
        this.cacheLinePtr[index]++;
        this.setCapacity[index] = 1;
    }

    public boolean find(int index, long tag) {
        return this.entries[index][0] == tag;
    }
}

class NWayAssociative extends Cache {
    LRU lru;

    public NWayAssociative(String name, int size, int lineSize, int setCount, String replacementPolicy, boolean last) {
        super(name, size, lineSize, setCount, replacementPolicy, last);
        switch (this.replacementPolicy) {
            case "lru":
                lru = new LRU(this.setCount);
                break;
            case "lfu":
                break;
        }
    }

    protected void evict(int index) {
        switch (this.replacementPolicy) {
            case "lru":
                int lineIndex = this.lru.getHead(index).getIndex();
                this.entries[index][lineIndex] = -1;
                this.lru.remove(index, lineIndex);
                this.cacheLinePtr[index] = lineIndex;
                break;
            case "lfu":
                break;
            default:
                this.entries[index][this.cacheLinePtr[index]] = -1;
        }
        this.setCapacity[index]--;
    }

    protected void insert(int index, long tag) {
        this.entries[index][this.cacheLinePtr[index]] = tag;

        switch (this.replacementPolicy) {
            case "lru":
                this.lru.update(index, this.cacheLinePtr[index]);
                break;
            case "lfu":
                break;
        }

        this.cacheLinePtr[index] = ++this.cacheLinePtr[index] % this.setSize;
        this.setCapacity[index]++;
    }

    @Override
    CacheLine getCacheLine(long memAddr) {
        return new CacheLine(0, memAddr);
    }

    public boolean find(int index, long tag) {
        switch (this.replacementPolicy) {
            case "lru":
                for (int i = 0; i < this.setSize; i++) {
                    if (this.entries[index][i] == tag) {
                        this.lru.update(index, i);
                        return true;
                    }
                }
                break;
            default:
                for (int i = 0; i < this.setSize; i++) {
                    if (this.entries[index][i] == tag) {
                        return true;
                    }
                }
        }
        return false;
    }
}

class LRU {
    private HashMap<Integer, DoublyLinkedList> lru;
    private HashMap<Integer, HashMap<Integer, LinkedListNode>> indexToNode;

    public LRU(int setCount) {
        this.lru = new HashMap<>();
        this.indexToNode = new HashMap<>();
        for (int i = 0; i < setCount; i++) {
            lru.put(i, new DoublyLinkedList());
            indexToNode.put(i, new HashMap<>());
        }
    }

    public void update(int setIndex, int lineIndex) {
        if (this.indexToNode.get(setIndex).containsKey(lineIndex)) {
            LinkedListNode node = this.indexToNode.get(setIndex).get(lineIndex);
            this.lru.get(setIndex).remove(node);
        }
        LinkedListNode node = new LinkedListNode(lineIndex);
        this.lru.get(setIndex).append(node);
        this.indexToNode.get(setIndex).put(lineIndex, node);
    }

    public void remove(int setIndex, int lineIndex) {
        LinkedListNode node = this.indexToNode.get(setIndex).get(lineIndex);
        this.lru.get(setIndex).remove(node);
        this.indexToNode.get(setIndex).remove(lineIndex);
    }

    public LinkedListNode getHead(int index) {
        return this.lru.get(index).getHead();
    }
}

//class LFU {
//    // break tie by index of lines in trace file, eject earlier line
//    HashMap<Integer, LinkedHashSet<LinkedListNode>> indexNodeList;
//    HashMap<Integer, HashMap<Integer, LinkedHashSet<LinkedListNode>>> indexFreqList;
//    HashMap<Integer, HashMap<Integer, LinkedListNode>> indexFreqTail;
//    HashMap<Integer, Integer> indexMinFreq;
//
//    public LFU(int setCount) {
//        this.indexNodeList = new HashMap<>();
//        this.indexFreqList = new HashMap<>();
//        this.indexFreqTail = new HashMap<>();
//        this.indexMinFreq = new HashMap<>();
//        for (int i = 0; i < setCount; i++) {
//            this.indexNodeList.put(i, new LinkedHashSet<>());
//            this.indexFreqList.put(i, new HashMap<>());
//            this.indexMinFreq.put(i, 0);
//        }
//    }
//
//    protected void update(int index, LinkedListNode node) {
//        int freq = node.getFreq();
//        this.indexFreqList.get(index).remove(node);
//        if (this.indexMinFreq.get(index) == freq && this.indexFreqList.get(index).size() == 0) {
//            this.indexMinFreq.put(index, freq + 1);
//        }
//
//        node.setFreq(freq + 1);
//        freq = node.getFreq();
//        if (!this.indexFreqTail.get(index).containsKey(freq)) {
//            this.indexFreqTail.get(index).put(freq, new LinkedListNode());
//        }
//
//    }
