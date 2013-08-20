package org.apache.hadoop.hbase.replication.regionserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    public HBaseQoD() {
        // TODO Implement empty constructor
        // hardcoded bounds
        final String tableName = "usertable";
        final String columnFamily0 = "c0";
        final String columnFamily1 = "c1";

        maxBounds.put(tableName + SEPARATOR + columnFamily0, new K(-1, 50050, -1));
        maxBounds.put(tableName + SEPARATOR + columnFamily1, new K(-1, 6, -1));

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

    
    /**Method to determine what is replicated
     * @param containerId
     * @return true or false
     */
    public boolean enforceQoS(String containerId) {

     	K maxK = getMaxK(containerId);
        if(maxK == null)
          return true; 
      
    	K actualK = getActualK(containerId);
        actualK.incSequence();        
    	
        System.out.println("SEQ: " + actualK.getSequence() + ", MAX SEQ: " + maxK.getSequence());
      	
        if (actualK.getSequence() >= maxK.getSequence()) {
            actualK.setSequence(0);
            return true;
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

}
