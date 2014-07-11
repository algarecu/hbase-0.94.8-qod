package org.apache.hadoop.hbase.replication.regionserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.regionserver.wal.HLog.Entry;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;

//import com.yahoo.ycsb.Client;

/**
 * This Cache should allocate memory through Direct ByteByffers, available in the java.nio package. This avoids the garbage collector
 * overhead that impacts performance.
 * 
 */
/**
 * @author algarecu
 * 
 */
public class Cache {

    private int size = 1000;
    private int maxElementsToEvict = 500;

    // stores (table, row)->CacheEntry
    // private static Map<String, Map<String, CacheEntry>> tables = new ConcurrentHashMap<String, Map<String, CacheEntry>>();

    // SROE: maps data_container_id -> CacheEntry_List
    private Map<String, List<CacheEntry>> cache = new ConcurrentHashMap<String, List<CacheEntry>>();

    private Map<Long, String> lastAccessTime = Collections.synchronizedMap(new TreeMap<Long, String>());


    private void evictElems(Map<String, CacheEntry> table) {
        int countEvicts = maxElementsToEvict;
        List<Long> toRemove = new ArrayList<Long>();
        for (Long key : lastAccessTime.keySet()) {
            toRemove.add(key);
            if (--countEvicts == 0)
                break;

        }
        for (Long key : toRemove)
            table.remove(lastAccessTime.remove(key));
    }

    /**
     * @param key
     * @param entry
     */
    public void put(String key, CacheEntry entry) {
        if (cache.containsKey(key)) {
            cache.get(key).add(entry);
        } else {
            List<CacheEntry> lst = new ArrayList<CacheEntry>();
            lst.add(entry);
            cache.put(key, lst);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @author: alvaro, but see java.lang.Object#toString()
     * 
     * Overriden method to print cache entries
     */
    @Override
    public String toString() {
        String str = "CACHE CONTENTS: \n";

        Collection<List<CacheEntry>> values = cache.values();
        cache.values().toArray();
        
        for (List<CacheEntry> entriesList : values) {

            for (CacheEntry entry : entriesList) {
                KeyValue edit = entry.getEdit();
                if (edit == null)
                	System.out.println("Problem with cache edits");

                str += "(Row: " + Bytes.toString(edit.getRow()) + ", ColFamily: " + Bytes.toString(edit.getFamily()) + ", Value: "
                        + Bytes.toString(edit.getValue()) + ")\n";
                
            }

        }
        return str;
    }

    // A.G: what I return when I want to get an entry
    /**
     * This is a method to get the mapping to that container key.
     * 
     * @param key
     * @return returns the values or an empty collection.
     */
    public List<CacheEntry> get(String key) {

        try {
            if (cache.containsKey(key)) {
                System.out.println("The key does exist in the cache and has values associated");
                return (List<CacheEntry>) cache.remove(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
  
        return Collections.emptyList();
    }

    public Collection<CacheEntry> fetch(String tableName) {
        return cache.get(tableName);
    }

}
