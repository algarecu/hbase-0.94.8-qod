package org.apache.hadoop.hbase.replication.regionserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

public class HBaseQoD {
  
  public static final Log LOG = LogFactory.getLog(HBaseQoD.class);

  public static final String SEPARATOR = "::";

  public static final String ROW_SUFFIX = "ROW";
  public int countCacheAccesses = 0, countCacheHits = 0;

  public enum Item {
    TABLE, ROW, COLUMN, CELL, NONE
  }

  // Map to read cached qodBounds
  Map<String, K> qodBounds = new HashMap<String, K>();

  /* HBaseConfiguration() */
  public static Configuration config = HBaseConfiguration.create();
  public String _tableName = config.get("hbase.qod.tablename");
  public String _columnFamily = config.get("hbase.qod.columnFamily");    
  public int _sequence = config.getInt("hbase.qod.sequence", 0);

  /* Control bounds holding expiry value:
   * <_tableName:_columnFamily, K>
   * */

  public Map<String, K> controlBounds = Collections.synchronizedMap(new HashMap<String, K>());

  /* time control: <expiration time, containerId> private Map<Long, String> 
   * timeControlBounds = Collections.synchronizedMap(new TreeMap<Long, String>());*/
    
  /**
   * Constructor
   */
  public HBaseQoD() {
    if (qodBounds.containsKey(_tableName) && qodBounds.containsKey(_columnFamily)){
      qodBounds.put(_tableName + SEPARATOR + _columnFamily, new K(-1, _sequence, -1));

    }
    else{
      if (_tableName == null)
      {
        System.err.println("Error, must specify a table_name for HBase table");
        try {
          throw new Exception("No columnfamily specified");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      else if (_columnFamily == null)
      {
        System.err.println("Error, must specify a columnfamily for HBase table");
        try {
          throw new Exception("No columnfamily specified");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } 
  }

  @SuppressWarnings("unused")
  private Item getItem(String tableName, String columnId) {
    if (qodBounds.containsKey(tableName + SEPARATOR + columnId))
      return Item.COLUMN;
    if (qodBounds.containsKey(tableName + SEPARATOR + ROW_SUFFIX))
      return Item.ROW;
    if (qodBounds.containsKey(tableName))
      return Item.TABLE;
    return Item.CELL;
  }

  private K getMaxK(String containerId) {
    return qodBounds.get(containerId);
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

    LOG.debug(
      "Replicating SEQ: " + 
      actualK.getSequence() + ", MAX SEQ: " + maxK.getSequence()
      );

    if (actualK.getSequence() >= maxK.getSequence()) {
      actualK.setSequence(0);
      return true; // propagate
    }
    return false;
  }

  public double getCacheHitRate() {
    return (double) countCacheHits / (double) countCacheAccesses;
  }

  // Implement scheduler from http://www.flex-compiler.lcs.mit.edu
  public void scheduler(String containerId, int num_threads){
    /**
     *  One thread in Replication sources: 
     *  (EDF init becomes trivial here) 
     */
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
