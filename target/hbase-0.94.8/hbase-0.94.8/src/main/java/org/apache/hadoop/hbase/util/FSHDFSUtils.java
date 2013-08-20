/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.RemoteExceptionHandler;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.AlreadyBeingCreatedException;
import org.apache.hadoop.hdfs.server.namenode.LeaseExpiredException;


/**
 * Implementation for hdfs
 */
public class FSHDFSUtils extends FSUtils{
  private static final Log LOG = LogFactory.getLog(FSHDFSUtils.class);

  /**
   * Lease timeout constant, sourced from HDFS upstream.
   * The upstream constant is defined in a private interface, so we
   * can't reuse for compatibility reasons.
   * NOTE: On versions earlier than Hadoop 0.23, the constant is in
   * o.a.h.hdfs.protocol.FSConstants, while for 0.23 and above it is
   * in o.a.h.hdfs.protocol.HdfsConstants cause of HDFS-1620.
   */
  public static final long LEASE_SOFTLIMIT_PERIOD = 60 * 1000;

  public static final String TEST_TRIGGER_DFS_APPEND = "hbase.test.trigger.dfs.append";

  @Override
  public void recoverFileLease(final FileSystem fs, final Path p, Configuration conf)
  throws IOException{
    if (!isAppendSupported(conf)) {
      LOG.warn("Running on HDFS without append enabled may result in data loss");
      return;
    }
    // lease recovery not needed for local file system case.
    // currently, local file system doesn't implement append either.
    if (!(fs instanceof DistributedFileSystem)) {
      return;
    }
    LOG.info("Recovering file " + p);
    long startWaiting = System.currentTimeMillis();

    // Trying recovery
    boolean recovered = false;
    long recoveryTimeout = conf.getInt("hbase.lease.recovery.timeout", 300000);
    // conf parameter passed from unit test, indicating whether fs.append() should be triggered
    boolean triggerAppend = conf.getBoolean(TEST_TRIGGER_DFS_APPEND, false);
    // retrying lease recovery may preempt pending lease recovery; default to waiting for 4 seconds
    // after calling recoverLease
    int waitingPeriod = conf.getInt("hbase.lease.recovery.waiting.period", 4000);
    Exception ex = null;
    while (!recovered) {
      try {
        try {
          DistributedFileSystem dfs = (DistributedFileSystem) fs;
          if (triggerAppend) throw new IOException();
          try {
            recovered = (Boolean) DistributedFileSystem.class.getMethod(
              "recoverLease", new Class[] { Path.class }).invoke(dfs, p);
            if (!recovered) LOG.debug("recoverLease returned false");
          } catch (InvocationTargetException ite) {
            // function was properly called, but threw it's own exception
            throw (IOException) ite.getCause();
          }
        } catch (Exception e) {
          LOG.debug("Failed fs.recoverLease invocation, " + e.toString() +
            ", trying fs.append instead");
          ex = e;
        }
        if (ex != null || System.currentTimeMillis() - startWaiting > recoveryTimeout) {
          LOG.debug("trying fs.append for " + p + " with " + ex);
          ex = null; // assume the following append() call would succeed
          FSDataOutputStream out = fs.append(p);
          out.close();
          recovered = true;
          LOG.debug("fs.append passed");
        }
        if (recovered) break;
      } catch (IOException e) {
        e = RemoteExceptionHandler.checkIOException(e);
        if (e instanceof AlreadyBeingCreatedException) {
          // We expect that we'll get this message while the lease is still
          // within its soft limit, but if we get it past that, it means
          // that the RS is holding onto the file even though it lost its
          // znode. We could potentially abort after some time here.
          long waitedFor = System.currentTimeMillis() - startWaiting;
          if (waitedFor > LEASE_SOFTLIMIT_PERIOD) {
            LOG.warn("Waited " + waitedFor + "ms for lease recovery on " + p +
              ":" + e.getMessage());
          }
        } else if (e instanceof LeaseExpiredException &&
            e.getMessage().contains("File does not exist")) {
          // This exception comes out instead of FNFE, fix it
          throw new FileNotFoundException(
              "The given HLog wasn't found at " + p.toString());
        } else {
          throw new IOException("Failed to open " + p + " for append", e);
        }
      }
      try {
        Thread.sleep(waitingPeriod);
      } catch (InterruptedException ie) {
        InterruptedIOException iioe = new InterruptedIOException();
        iioe.initCause(ie);
        throw iioe;
      }
      // we keep original behavior without retrying lease recovery
      break;
    }
    LOG.info("Finished lease recovery attempt for " + p);
  }
}
