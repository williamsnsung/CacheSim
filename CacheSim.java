import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * Abstract class for caches from which all other caches are based off of
 */
abstract class Cache {
    protected String name;
    protected int size;
    protected int lineSize;
    protected int setCount;
    protected String replacementPolicy;
    protected int hits;
    protected int misses;
    protected int offsetBitShift;
    protected int indexMask;
    protected int tagBitShift;
    protected HashMap<Integer, LinkedHashSet<Long>> entries;  // Key value pair of an index and tag(s) for a given cache line
    protected HashMap<Integer, Integer> freeEntries; // Key value pair showing how many entries are free for a given set in the cache
    protected boolean last;

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

    public HashMap<Integer, LinkedHashSet<Long>> getEntries() {
        return entries;
    }

    public HashMap<Integer, Integer> getFreeEntries() {
        return freeEntries;
    }

    public boolean isLast() {
        return last;
    }

    /**
     * Constructor for a cache, initialises the data structures used along with assigning the given parameters to its attributes
     * @param name                  name of the cache
     * @param size                  size of the cache
     * @param lineSize              size of a line in the cache
     * @param setCount               size of a set in the cache
     * @param replacementPolicy     the replacement policy for this cache
     * @param last                  whether this is the last cache in the hierarchy
     */
    public Cache(String name, int size, int lineSize, int setCount, String replacementPolicy, boolean last) {
        this.name = name;
        this.size = size;
        this.lineSize = lineSize;
        this.setCount = setCount;
        this.replacementPolicy = replacementPolicy;
        this.hits = 0;
        this.misses = 0;
        this.entries = new HashMap<>();
        this.freeEntries = new HashMap<>();
        this.last = last;

        for (int i = 0; i < setCount; i++) {
            this.entries.put(i, new LinkedHashSet<>());
            this.freeEntries.put(i, size / lineSize / setCount);
        }
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
        if (this.entries.get(index).contains(tag)) {
            this.hits++;
            return true;
        }
        this.misses++;
        if (this.freeEntries.get(index) > 0) {
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
        for (long tag : this.entries.get(index)) {
            this.entries.get(index).remove(tag);
            break;
        }
        this.freeEntries.put(index, this.freeEntries.get(index) + 1);
    }

    protected void insert(int index, long tag) {
        this.entries.get(index).add(tag);
        this.freeEntries.put(index, this.freeEntries.get(index) - 1);
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
                long evictim = this.lru.getFirst(index);
                this.entries.get(index).remove(evictim);
                this.lru.remove(index, evictim);
                break;
            case "lfu":
                break;
            default:
                for (long tag : this.entries.get(index)) {
                    this.entries.get(index).remove(tag);
                    break;
                }
        }
        this.freeEntries.put(index, this.freeEntries.get(index) + 1);
    }

    protected void insert(int index, long tag) {
        this.entries.get(index).add(tag);
        this.freeEntries.put(index, this.freeEntries.get(index) - 1);
        switch (this.replacementPolicy) {
            case "lru":
                this.lru.update(index, tag);
                break;
            case "lfu":
                break;
        }
    }

    @Override
    CacheLine getCacheLine(long memAddr) {
        return new CacheLine(0, memAddr);
    }
}

class LinkedListNode {
    private long val;
    private LinkedListNode prev;
    private LinkedListNode next;

    private int freq;

    public LinkedListNode() {
    }

    public LinkedListNode(long val, LinkedListNode prev, LinkedListNode next) {
        this.val = val;
        this.prev = prev;
        this.next = next;
    }

    public LinkedListNode(int freq, long val, LinkedListNode prev, LinkedListNode next) {
        this.freq = freq;
        this.val = val;
        this.prev = prev;
        this.next = next;
    }

    public long getVal() {
        return this.val;
    }

    public void setVal(long val) {
        this.val = val;
    }

    public LinkedListNode getPrev() {
        return this.prev;
    }

    public void setPrev(LinkedListNode prev) {
        this.prev = prev;
    }

    public LinkedListNode getNext() {
        return next;
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

class LRU {
    private HashMap<Integer, LinkedHashSet<LinkedListNode>> lru;
    private LinkedListNode tail;
    private HashMap<Integer, HashMap<Long, LinkedListNode>> tagToNode;

    public LRU(int setCount) {
        this.lru = new HashMap<>();
        this.tagToNode = new HashMap<>();
        for (int i = 0; i < setCount; i++) {
            lru.put(i, new LinkedHashSet<>());
            tagToNode.put(i, new HashMap<>());
        }
        tail = new LinkedListNode();
    }

    public void update(int index, long tag) {
        if (this.tagToNode.get(index).containsKey(tag)) {
            this.remove(index, tag);
        }
        LinkedListNode temp = tail;
        tail = new LinkedListNode(tag, tail, null);
        temp.setNext(tail);
        this.lru.get(index).add(tail);
        this.tagToNode.get(index).put(tag, tail);
    }

    public void remove(int index, long tag) {
        LinkedListNode node = this.tagToNode.get(index).get(tag);
        node.getPrev().setNext(node.getNext());
        node.getNext().setPrev(node.getPrev());
        this.lru.get(index).remove(node);
        this.tagToNode.get(index).remove(tag);
    }

    public long getFirst(int index) {
        for (LinkedListNode node : lru.get(index)) {
            return node.getVal();
        }
        return 0;
    }

}
//
//class LFU {
//    // break tie by index of lines in trace file, eject earlier line
//    HashMap<Integer, LinkedHashSet<LinkedListNode>> indexNodeList;
//    HashMap<Integer, HashMap<Integer, LinkedHashSet<LinkedListNode>>> indexFreqList;
//    HashMap<Integer, HashMap<Integer, Integer>> indexFreqTail;
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
//        node.setFreq(freq + 1);
//
//    }
//}