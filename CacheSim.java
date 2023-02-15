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
    protected int offsetBitShift; // the number of bits to be right shifted to remove the offset
    protected int indexMask; // a mask which allows us to keep the index bits only
    protected int tagBitShift; // the number of bits to right shift to arrive at the tag
    protected long[][] entries;  // Key value pair for set indices and tag(s), n sets x m cache lines
    protected int[] cacheLinePtr; // Points to a possible next cache line to insert into for a given set
    protected int[] setCapacity; // How many free lines are available for a given set
    boolean last; // whether this is the last cache in the hierarchy

    /**
     * Returns the name of the cache
     * @return A string
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the size of the cache in bytes
     * @return An integer
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Returns the number of bytes in a cache line
     * @return An integer
     */
    public int getLineSize() {
        return this.lineSize;
    }

    /**
     * Returns the number of cache hits for the current cache
     * @return An integer
     */
    public int getHits() {
        return this.hits;
    }

    /**
     * Returns the number of misses for the current cache
     * @return An integer
     */
    public int getMisses() {
        return this.misses;
    }

    /**
     * Returns the number of bits to right shift to remove the offset
     * @return An integer
     */
    public int getOffsetBitShift() {
        return this.offsetBitShift;
    }

    /**
     * Returns a bit mask that can be used to find the index from an address
     * @return An integer
     */
    public int getIndexMask() {
        return this.indexMask;
    }

    /**
     * Returns the number of bits to shift to arrive at the tag for an address
     * @return An integer
     */
    public int getTagBitShift() {
        return this.tagBitShift;
    }

    /**
     * Returns the maximum number of cache lines in a set
     * @return An integer
     */
    public int getSetSize() {
        return this.setSize;
    }

    /**
     * Constructor for a cache, initialises the data structures used along with assigning the given parameters to its attributes
     * @param name                  Name of the cache
     * @param size                  Size of the cache
     * @param lineSize              Size of a line in the cache
     * @param setSize               Size of a set in the cache
     * @param replacementPolicy     The replacement policy for this cache
     * @param last                  Whether this set is the last member of the cache hierarchy
     */
    public Cache(String name, int size, int lineSize, int setSize, String replacementPolicy, boolean last) {
        this.name = name;
        this.size = size;
        this.lineSize = lineSize;
        this.setSize = setSize;
        this.replacementPolicy = replacementPolicy;
        this.hits = 0;
        this.misses = 0;
        this.setCount = size / (lineSize * setSize);
        this.entries = new long[setCount][this.setSize];
        this.cacheLinePtr = new int[setCount];
        this.setCapacity = new int[setCount];
        this.last = last;

        // Below calculated as per lectures
        int indexBits =  log2(this.setCount);
        this.offsetBitShift = log2(lineSize);
        this.tagBitShift = this.offsetBitShift + indexBits;
        // Want a sequence of n ones for n index bits, with the rest being 0
        for (int i = 0; i < indexBits; i++) {
            this.indexMask += Math.pow(2, i);
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

    /**
     * Checks if the current tag is cached for a given index, adding to the hit/miss count appropriately.
     * Loads the tag into the current cache if it's last in the hierarchy, as you would then be retrieving from memory
     * @param memAddr The memory address converted into a long
     * @return Boolean, if it was a hit or a miss
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

        if (this.last) {
            if (this.setCapacity[index] >= this.setSize) {
                this.evict(index);
            }
            this.insert(index, tag);
        }
        return false;
    }

    /**
     * Abstract method where the eviction policy for a cache is defined
     * @param index The index to evict from for a given cache
     */
    abstract void evict(int index);

    /**
     * Abstract method where the insertion policy for a given cache is defined
     * @param index The index to insert into
     * @param tag   The tag you want to cache
     */
    abstract void insert(int index, long tag);

    /**
     * Abstract method to find if the tag you are looking for is in the given set
     * @param index The index for your set you want to search in
     * @param tag   The tag you are wanting to search for
     * @return      Whether it was found or not
     */
    abstract boolean find(int index, long tag);

    /**
     * Converts a memory address into a CacheLine object, holding an index and a tag for its attributes
     * @param memAddr   The memory address to convert into a CacheLine object
     * @return          A CacheLine object created from the given memory address
     */
    public CacheLine getCacheLine(long memAddr) {
        int index = (int) (memAddr >> this.getOffsetBitShift() & this.getIndexMask());
        long tag = memAddr >> this.getTagBitShift();
        return new CacheLine(index, tag);
    }
}

class CacheLine {
    private final int index;
    private final long tag;

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
     * @param name      The name of the cache
     * @param size      The size of the cache
     * @param lineSize  The size of a line in the cache
     * @param last      If this cache is last in the hierarchy
     */
    public DirectMapped(String name, int size, int lineSize, boolean last) {
        super(name, size, lineSize, 1, "direct", last);
    }

    /**
     * Removes the current tag in the cache line at the given index
     * @param index The index to evict from for a given cache
     */
    protected void evict(int index) {
        this.entries[index][0] = -1;
        this.cacheLinePtr[index] = 0;
        this.setCapacity[index] = 0;
    }

    /**
     * Inserts a tag into the set specified by the given index
     * @param index The index to insert into
     * @param tag   The tag you want to cache
     */
    protected void insert(int index, long tag) {
        this.entries[index][0] = tag;
        this.cacheLinePtr[index]++;
        this.setCapacity[index] = 1;
    }

    /**
     * Finds if your tag is in the directly mapped cache
     * @param index The index for your set you want to search in
     * @param tag   The tag you are wanting to search for
     * @return      Whether it was in the cache or not
     */
    public boolean find(int index, long tag) {
        return this.entries[index][0] == tag;
    }
}

class NWayAssociative extends Cache {
    LRU lru;
    LFU lfu;

    /**
     * Creates an NWayAssociative cache, defaulting to the round-robin replacement policy if none is specified
     * @param name                  The name of the cache
     * @param size                  The size of the cache in bytes
     * @param lineSize              The size of a cache line in bytes
     * @param setSize               The size of a set in the cache
     * @param replacementPolicy     The replacement policy when the cache is full
     * @param last                  Whether this cache is last in the hierarchy or not
     */
    public NWayAssociative(String name, int size, int lineSize, int setSize, String replacementPolicy, boolean last) {
        super(name, size, lineSize, setSize, replacementPolicy, last);
        switch (this.replacementPolicy) {
            case "lru":
                lru = new LRU(this.setCount);
                break;
            case "lfu":
                lfu = new LFU(this.setCount);
                break;
        }
    }

    /**
     * Evicts from an index using either the round-robin, least frequently used, or least recently used eviction policies
     * @param index The index to evict from for a given cache
     */
    protected void evict(int index) {
        int cacheLine;
        switch (this.replacementPolicy) {
            case "lru":
                cacheLine = this.lru.getHead(index).getIndex();     // finding the head of the lru linked list
                this.entries[index][cacheLine] = -1;                // resetting the cache line entry
                this.lru.remove(index, cacheLine);                  // removing the cache line entry from the lru object
                this.cacheLinePtr[index] = cacheLine;               // setting the next free cache line to the current one
                break;
            case "lfu":
                cacheLine = this.lfu.getLFUCacheLine(index);        // finding the least frequently used cache line from a treemap
                this.entries[index][cacheLine] = -1;                // resetting the cache line entry
                this.lfu.remove(index, cacheLine);                  // removing the cache line entry from the lru object
                this.cacheLinePtr[index] = cacheLine;               // setting the next free cache line to the current one
                break;
            default:
                this.entries[index][this.cacheLinePtr[index]] = -1; // evicting the current cache line pointed to by the cache line pointer
        }
        this.setCapacity[index]--;                                  // decrementing the set capacity for the current set
    }

    /**
     * Inserts into a specified set a specified tag while adhering to a specified replacement policy, one of lru, lfu, and rr
     * @param index The index to insert into
     * @param tag   The tag you want to cache
     */
    protected void insert(int index, long tag) {
        this.entries[index][this.cacheLinePtr[index]] = tag;

        switch (this.replacementPolicy) {
            case "lru":
                this.lru.update(index, this.cacheLinePtr[index]);
                break;
            case "lfu":
                this.lfu.update(index, this.cacheLinePtr[index]);
                break;
        }

        this.cacheLinePtr[index] = ++this.cacheLinePtr[index] % this.setSize;   // increments the cache pointer, cycling back round to the start when at the end
        this.setCapacity[index]++;
    }

    /**
     * Looks for the tag you want in the specified index, updating the lru/lfu if one of the two policies is selected
     * @param index The index for your set you want to search in
     * @param tag   The tag you are wanting to search for
     * @return      Returns whether the tag was found or not
     */
    public boolean find(int index, long tag) {
        for (int i = 0; i < this.setSize; i++) {
            if (this.entries[index][i] == tag) {
                switch (this.replacementPolicy) {
                    case "lru":
                        this.lru.update(index, i);
                        break;
                    case "lfu":
                        this.lfu.update(index, i);
                        break;
                }
                return true;
            }
        }

        return false;
    }
}
