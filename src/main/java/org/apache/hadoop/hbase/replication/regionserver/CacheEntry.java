package org.apache.hadoop.hbase.replication.regionserver;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;

// SROE:
import org.apache.hadoop.hbase.regionserver.wal.HLog.Entry;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;

public class CacheEntry {

    // SROE: remove next 2 lines
    private Put put;
    private Result result;

    // SROE: each CacheEntry contains a HLog entry
    private KeyValue edit;



    private boolean dirty;
    private long lastAccessTime;

//    public CacheEntry(Result result, long lastAccessTime) {
//        this.result = result;
//        this.lastAccessTime = lastAccessTime;
//        dirty = false;
//    }
//
//    public CacheEntry(Put put, long lastAccessTime) {
//        this.put = put;
//        this.lastAccessTime = lastAccessTime;
//        dirty = true;
//    }

    // SROE: decide later if lastAccessTime/dirty is required
    public CacheEntry(KeyValue entry) {
      this.edit = entry;
    }

    public Put getPut() {
        return put;
    }

    public void setPut(Put put) {
        this.put = put;
    }

    public Result getResult() {
        if (result != null)
            return result;
        return new Result(put.getFamilyMap().values().iterator().next());
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    public KeyValue getEdit(){
    	return this.edit;
    }
}
