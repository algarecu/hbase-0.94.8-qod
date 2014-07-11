package org.apache.hadoop.hbase.replication.regionserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Date;

public class HBaseQoD {

    public static final String SEPARATOR = "::";

    private static final String ROW_SUFFIX = "ROW";
    private boolean stats = false;
    private int countCacheAccesses = 0, countCacheHits = 0;

    public enum Item {
        TABLE, ROW, COLUMN, CELL, NONE
    }

    // read-only map hardcoded bounds
    Map<String, K> maxBounds = new HashMap<String, K>();

    // shadow struct: table name concatenated with column id. Structure that you
    // update and maps K<> to expire with keys (string) of controlBounds
    private Map<String, K> controlBounds = Collections.synchronizedMap(new HashMap<String, K>());

    // time control: <expiration time, containerId>
    // private Map<Long, String> timeControlBounds = Collections.synchronizedMap(new TreeMap<Long, String>());

    public HBaseQoD() {
        // TODO Implement empty constructor
        // hardcoded bounds

        // Strong eventual consistency, this is what I want now.
        final String tableName = "usertable";
        final String columnFamily = "c0";

        maxBounds.put(tableName + SEPARATOR + columnFamily, new K(-1, 1, -1));

        // Weaker eventual consistency > QoD (seq) > 1
        // final String tableNamePriorities = "usertablePriorities";
        // final String columnFamily0 = "critical";//strong eventual consistency (immediate)
        // final String columnFamily1 = "noncritical";//non-strong eventual, just upper bounds.
        
        // -1 in the vector K means in this case
        // maxBounds.put(tableNamePriorities + SEPARATOR + columnFamily0, new K(-1, 50, -1));
        // maxBounds.put(tableNamePriorities + SEPARATOR + columnFamily1, new K(-1, 100, -1));
        // maxBounds.put(tableName2 + SEPARATOR + columnFamily2, new K(-1, 100, -1));
    }

    private Item getItem(String tableName, String columnId) {
        if (maxBounds.containsKey(tableName + SEPARATOR + columnId))
            return Item.COLUMN;
        if (maxBounds.containsKey(tableName + SEPARATOR + ROW_SUFFIX))
            return Item.ROW;
        if (maxBounds.containsKey(tableName))
            return Item.TABLE;
        return Item.CELL;
    }

    private K getMaxK(String containerId) {
        return maxBounds.get(containerId);
    }

    private K getActualK(String containerId) {
        K k;
        if (controlBounds.containsKey(containerId)) {
            k = controlBounds.get(containerId);
        } else {
            k = new K(-1, 0, -1);
            controlBounds.put(containerId, k);
        }
        return k;
    }

    
    /**Method to enforce QoD
     * @param containerId
     * @return true or false, replicated or not
     */
    public boolean enforceQoS(String containerId) {

     	K maxK = getMaxK(containerId);
        if(maxK == null)
          return true; 
      
    	K actualK = getActualK(containerId);
        actualK.incSequence();        
    	
        //System.out.println("SEQ: " + actualK.getSequence() + ", MAX SEQ: " + maxK.getSequence());
      	
        if (actualK.getSequence() >= maxK.getSequence()) {
            actualK.setSequence(0);
            return true; 
            // after this we return and propagate -> ReplicationSource.java
        }
      return false;
    }
    
//    private boolean isControlled (String containerId) {
//		// if control bounds key is different from containerId, then equal true (replicate anyway --META--)
//    	if(!controlBounds.containsKey(containerId))
//    		return true;
//		return false;	
//    }

    private void report(String msg) {
        if (stats)
            System.err.println(msg);
    }

    public double getCacheHitRate() {
        return (double) countCacheHits / (double) countCacheAccesses;
    }
    
    // from http://www.flex-compiler.lcs.mit.edu (credit)
    public void scheduler(String containerId, int num_threads){
      // there is only one thread in Replication sources, if not more (EDF init becomes trivial here)
      
      
      // INIT
      for (int x=0; x< num_threads; x++){
      // 1. get initial time of thread
      // 2. initialize absolute deadline ("d") to infinite, so never stops.
      // 3. amount of work to be completed, processor time received from "d".
      }
      
      // SCHEDULER
      // implement the earliest deadline first scheduler
      
      
    }

}
