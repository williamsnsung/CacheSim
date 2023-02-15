import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.gson.Gson;

// The below two classes are used for the parsing of the cache config json, attributes corresponding to the variables in the given json

class CacheList {
    public List<CacheConfig> getCaches() {
        return this.caches;
    }

    public void setCaches(List<CacheConfig> caches) {
        this.caches = caches;
    }

    List<CacheConfig> caches;

}

class CacheConfig {
    String name;
    int size;
    int line_size;
    String kind;
    String replacement_policy;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getLine_size() {
        return this.line_size;
    }

    public void setLine_size(int line_size) {
        this.line_size = line_size;
    }

    public String getKind() {
        return this.kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getReplacement_policy() {
        return this.replacement_policy;
    }

}

public class Main{
    public static void main(String[] args) {
//        final long startTime = System.currentTimeMillis();
        try {
            // How to parse a JSON file in Java
            // https://stackoverflow.com/questions/19169754/parsing-nested-json-data-using-gson [12/02/2023]
            Gson gson = new Gson();
            // Opens the file given by the first parameter relative to the current working directory
            Reader reader = Files.newBufferedReader(Paths.get(args[0]));
            // Loading the json into a java object before creating the relevant caches and inserting them into the below array
            CacheList cachesJson = gson.fromJson(reader, CacheList.class);
            Cache[] caches = new Cache[cachesJson.getCaches().size()];
            for (int i = 0; i < caches.length; i++) {
                CacheConfig cache = cachesJson.getCaches().get(i);
                switch (cache.getKind()) {
                    case "direct":
                        caches[i] = new DirectMapped(cache.getName(), cache.getSize(), cache.getLine_size());
                        break;
                    case "full":
                        caches[i] = new NWayAssociative(cache.getName(), cache.getSize(), cache.getLine_size(), cache.getSize() / cache.getLine_size(), cache.getReplacement_policy());
                        break;
                    default:
                        int setSize = Character.getNumericValue(cache.getKind().charAt(0));
                        caches[i] = new NWayAssociative(cache.getName(), cache.getSize(), cache.getLine_size(), setSize, cache.getReplacement_policy());
                }
                if (i != 0) {
                    caches[i - 1].setChild(caches[i]);
                }
            }
            reader.close();

            // Reading the trace file given by the second argument relative to the current working directory line by line
            FileReader trace = new FileReader(args[1]);
            BufferedReader br = new BufferedReader(trace);
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineData = line.split(" ");
                // Find the size of the memory block to find in the cache
                int blockSize = Integer.parseInt(lineData[3]);
                long memAddr = Long.parseLong(lineData[1], 16);
                long endAddr = memAddr + blockSize;
                // Check if the memory address is in cache, check the relevant cache lines to see if they contain the needed bytes for the cache
                memAddr = memAddr - memAddr % caches[0].getLineSize();
                // Goes through each cache line that this memory block would use in the cache and checks them to see if they hold the relevant bits for the current block
                while (memAddr < endAddr) {
                    caches[0].checkCache(memAddr); // Check cache will recursively seek the memory address in cache until it either gets it hit or reaches main memory
                    memAddr += caches[0].getLineSize();
                }
            }

            // Prints a json of the hits and misses for each cache along with the number of main memory accesses to stdout
            System.out.println("{");
            System.out.println("\t\"main_memory_accesses\": " + caches[caches.length - 1].getMisses() + ",");
            System.out.println("\t\"caches\": [");
            for (int i = 0; i < caches.length; i++) {
                System.out.println("\t\t{");
                System.out.println("\t\t\t\"name\": \"" + caches[i].getName() + "\",");
                System.out.println("\t\t\t\"hits\": \"" + caches[i].getHits() + "\",");
                System.out.println("\t\t\t\"misses\": \"" + caches[i].getMisses() + "\"");
                System.out.print("\t\t}");
                if (i != caches.length - 1){
                    System.out.println(",");
                }
                else{
                    System.out.println();
                }
            }
            System.out.println("\t]");
            System.out.println("}");
        }         
        catch (Exception e) {
            System.err.println(e);
        }
    }
}
