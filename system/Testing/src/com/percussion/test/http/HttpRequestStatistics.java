/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.test.http;

import com.percussion.test.statistics.AverageAccumulator;
import com.percussion.test.io.DataLoader;
import java.util.List;
import java.util.ArrayList;

public class HttpRequestStatistics implements Cloneable
{
   public HttpRequestStatistics()
   {
   }

   /**
    * Record that the timing started at the specified time,
    * expressed in milliseconds since the epoch.
    */
   public void startedTimingAt(long start)
   {
      m_startedTiming = start;
   }

   public void finishedTimingAt(long finish)
   {
      m_finishedTiming = finish;
   }

   /**
    * Add the given timings to this statistics.
    */
   public void add(HttpRequestTimings timings)
   {
      /* connect timings (milliseconds) */
      final long conMS = timings.afterConnect() - timings.beforeConnect();
      m_connectTimings.add(new Long(conMS));
      m_connectAvgMS.accumulate(conMS);
      if (conMS < m_connectMinMS) m_connectMinMS = conMS;
      if (conMS > m_connectMaxMS) m_connectMaxMS = conMS;
      m_connectTotMS += conMS;
      m_connectTotSqMS = (double)m_connectTotMS * (double)m_connectTotMS;

      /* request send timings (milliseconds) */
      final long reqSendMS = timings.afterRequest() - timings.afterConnect();
      m_reqSendTimings.add(new Long(reqSendMS));
      m_reqSendAvgMS.accumulate(reqSendMS);
      if (reqSendMS < m_reqSendMinMS) m_reqSendMinMS = reqSendMS;
      if (reqSendMS > m_reqSendMaxMS) m_reqSendMaxMS = reqSendMS;
      m_reqSendTotMS += reqSendMS;
      m_reqSendTotSqMS = (double)m_reqSendTotMS * (double)m_reqSendTotMS;

      /* header receive timings (milliseconds) */
      final long hdrReadMS = timings.afterHeaders() - timings.afterRequest();
      m_hdrReadTimings.add(new Long(hdrReadMS));
      m_hdrReadAvgMS.accumulate(hdrReadMS);
      if (hdrReadMS < m_hdrReadMinMS) m_hdrReadMinMS = hdrReadMS;
      if (hdrReadMS > m_hdrReadMaxMS) m_hdrReadMaxMS = hdrReadMS;
      m_hdrReadTotMS += hdrReadMS;
      m_hdrReadTotSqMS = (double)m_hdrReadTotMS * (double)m_hdrReadTotMS;

      /* content read timings (milliseconds) */
      final long cntReadMS = timings.afterContent() - timings.afterHeaders();
      m_cntReadTimings.add(new Long(cntReadMS));
      m_cntReadAvgMS.accumulate(cntReadMS);
      if (cntReadMS < m_cntReadMinMS) m_cntReadMinMS = cntReadMS;
      if (cntReadMS > m_cntReadMaxMS) m_cntReadMaxMS = cntReadMS;
      m_cntReadTotMS += cntReadMS;
      m_cntReadTotSqMS = (double)m_cntReadTotMS * (double)m_cntReadTotMS;

      /* round trip timings (milliseconds) */
      final long rndTripMS = conMS + reqSendMS + hdrReadMS + cntReadMS;
      m_rndTripTimings.add(new Long(rndTripMS));
      m_rndTripAvgMS.accumulate(rndTripMS);
      if (rndTripMS < m_rndTripMinMS) m_rndTripMinMS = rndTripMS;
      if (rndTripMS > m_rndTripMaxMS) m_rndTripMaxMS = rndTripMS;
      m_rndTripTotMS += rndTripMS;
      m_rndTripTotSqMS = (double)m_rndTripTotMS * (double)m_rndTripTotMS;

      /* header bytes */
      final long hdrBytes = timings.headerBytes();
      m_hdrBytesAvg.accumulate(hdrBytes);
      if (hdrBytes < m_hdrBytesMin) m_hdrBytesMin = hdrBytes;
      if (hdrBytes > m_hdrBytesMax) m_hdrBytesMax = hdrBytes;
      m_hdrBytesTot += hdrBytes;
      m_hdrBytesTotSq = (double)m_hdrBytesTot * (double)m_hdrBytesTot;

      /* content bytes */
      final long cntBytes = timings.contentBytes();
      m_cntBytesAvg.accumulate(cntBytes);
      if (cntBytes < m_cntBytesMin) m_cntBytesMin = cntBytes;
      if (cntBytes > m_cntBytesMax) m_cntBytesMax = cntBytes;
      m_cntBytesTot += cntBytes;
      m_cntBytesTotSq = (double)m_cntBytesTot * (double)m_cntBytesTot;

      m_numReqs++;
   }

   /**
    * Combine the given statistics with this statistics.
    */
   public void add(HttpRequestStatistics stats)
   {
      {
      /* connect timings (milliseconds) */
      m_connectAvgMS.accumulate(stats.m_connectAvgMS);
      if (stats.m_connectMinMS < m_connectMinMS) m_connectMinMS
         = stats.m_connectMinMS;

      if (stats.m_connectMaxMS > m_connectMaxMS) m_connectMaxMS
         = stats.m_connectMaxMS;

      m_connectTotMS += stats.m_connectTotMS;
      m_connectTotSqMS = (double)m_connectTotMS * (double)m_connectTotMS;
      }

      {
      /* request send timings (milliseconds) */
      m_reqSendAvgMS.accumulate(stats.m_reqSendAvgMS);
      if (stats.m_reqSendMinMS < m_reqSendMinMS) m_reqSendMinMS
         = stats.m_reqSendMinMS;

      //FB: SA_FIELD_SELF_COMPARISON NC 1-17-16
      if (stats.m_reqSendMaxMS > m_reqSendMaxMS) m_reqSendMaxMS
         = stats.m_reqSendMaxMS;

      m_reqSendTotMS += stats.m_reqSendTotMS;
      m_reqSendTotSqMS = (double)m_reqSendTotMS * (double)m_reqSendTotMS;
      }

      {
      /* header receive timings (milliseconds) */
      m_hdrReadAvgMS.accumulate(stats.m_hdrReadAvgMS);
      if (stats.m_hdrReadMinMS < m_hdrReadMinMS) m_hdrReadMinMS
         = stats.m_hdrReadMinMS;

      if (stats.m_hdrReadMaxMS > m_hdrReadMaxMS) m_hdrReadMaxMS
         = stats.m_hdrReadMaxMS;

      m_hdrReadTotMS += stats.m_hdrReadTotMS;
      m_hdrReadTotSqMS = (double)m_hdrReadTotMS * (double)m_hdrReadTotMS;
      }

      {
      /* content read timings (milliseconds) */
      m_cntReadAvgMS.accumulate(stats.m_cntReadAvgMS);
      if (stats.m_cntReadMinMS < m_cntReadMinMS) m_cntReadMinMS
         = stats.m_cntReadMinMS;

      //FB: SA_FIELD_SELF_COMPARISON NC 1-17-16
      if (stats.m_cntReadMaxMS > m_cntReadMaxMS) m_cntReadMaxMS
         = stats.m_cntReadMaxMS;

      m_cntReadTotMS += stats.m_cntReadTotMS;
      m_cntReadTotSqMS = (double)m_cntReadTotMS * (double)m_cntReadTotMS;
      }

      {
      /* round trip timings (milliseconds) */
      m_rndTripAvgMS.accumulate(stats.m_rndTripAvgMS);
      if (stats.m_rndTripMinMS < m_rndTripMinMS) m_rndTripMinMS
         = stats.m_rndTripMinMS;

      //FB:SA_FIELD_SELF_COMPARISON 1-17-16 
      if (stats.m_rndTripMaxMS > m_rndTripMaxMS) m_rndTripMaxMS
         = stats.m_rndTripMaxMS;

      m_rndTripTotMS += stats.m_rndTripTotMS;
      m_rndTripTotSqMS = (double)m_rndTripTotMS * (double)m_rndTripTotMS;
      }

      {
      /* header bytes */
      m_hdrBytesAvg.accumulate(stats.m_hdrBytesAvg);
      if (stats.m_hdrBytesMin < m_hdrBytesMin) m_hdrBytesMin
         = stats.m_hdrBytesMin;

      if (stats.m_hdrBytesMax > m_hdrBytesMax) m_hdrBytesMax
         = stats.m_hdrBytesMax;

      m_hdrBytesTot += stats.m_hdrBytesTot;
      m_hdrBytesTotSq = (double)m_hdrBytesTot * (double)m_hdrBytesTot;
      }

      {
      /* content bytes */
      m_cntBytesAvg.accumulate(stats.m_cntBytesAvg);
      if (stats.m_cntBytesMin < m_cntBytesMin) m_cntBytesMin
         = stats.m_cntBytesMin;

      //FB:SA_FIELD_SELF_COMPARISON NC 1-17-16 
      if (stats.m_cntBytesMax > m_cntBytesMax) m_cntBytesMax
         = stats.m_cntBytesMax;

      m_cntBytesTot += stats.m_cntBytesTot;
      m_cntBytesTotSq = (double)m_cntBytesTot * (double)m_cntBytesTot;
      }

      m_numReqs += stats.m_numReqs;
   }

   public void load(DataLoader loader, String name)
   {
      DataLoader ldr = loader.getChildLoader(name, this);

      m_numReqs = ldr.getLong("numReqs");
      m_startedTiming = ldr.getLong("startedTiming");
      m_finishedTiming = ldr.getLong("finishedTiming");

      m_connectAvgMS.load(ldr, "connectAvgMS");
      m_connectMinMS   = ldr.getLong("connectMinMS");
      m_connectMaxMS   = ldr.getLong("connectMaxMS");
      m_connectTotMS   = ldr.getLong("connectTotMS");
      m_connectTotSqMS = ldr.getDouble("connectTotSqMS");

      m_reqSendAvgMS.load(ldr, "reqSendAvgMS");
      m_reqSendMinMS   = ldr.getLong("reqSendMinMS");
      m_reqSendMaxMS   = ldr.getLong("reqSendMaxMS");
      m_reqSendTotMS   = ldr.getLong("reqSendTotMS");
      m_reqSendTotSqMS = ldr.getDouble("reqSendTotSqMS");

      m_hdrReadAvgMS.load(ldr, "hdrReadAvgMS");
      m_hdrReadMinMS   = ldr.getLong("hdrReadMinMS");
      m_hdrReadMaxMS   = ldr.getLong("hdrReadMaxMS");
      m_hdrReadTotMS   = ldr.getLong("hdrReadTotMS");
      m_hdrReadTotSqMS = ldr.getDouble("hdrReadTotSqMS");

      m_cntReadAvgMS.load(ldr, "cntReadAvgMS");
      m_cntReadMinMS   = ldr.getLong("cntReadMinMS");
      m_cntReadMaxMS   = ldr.getLong("cntReadMaxMS");
      m_cntReadTotMS   = ldr.getLong("cntReadTotMS");
      m_cntReadTotSqMS = ldr.getDouble("cntReadTotSqMS");

      m_rndTripAvgMS.load(ldr, "rndTripAvgMS");
      m_rndTripMinMS   = ldr.getLong("rndTripMinMS");
      m_rndTripMaxMS   = ldr.getLong("rndTripMaxMS");
      m_rndTripTotMS   = ldr.getLong("rndTripTotMS");
      m_rndTripTotSqMS = ldr.getDouble("rndTripTotSqMS");

      m_hdrBytesAvg.load(ldr, "hdrBytesAvg");
      m_hdrBytesMin    = ldr.getLong("hdrBytesMin");
      m_hdrBytesMax    = ldr.getLong("hdrBytesMax");
      m_hdrBytesTot    = ldr.getLong("hdrBytesTot");
      m_hdrBytesTotSq  = ldr.getDouble("hdrBytesTotSq");

      m_cntBytesAvg.load(ldr, "cntBytesAvg");
      m_cntBytesMin    = ldr.getLong("cntBytesMin");
      m_cntBytesMax    = ldr.getLong("cntBytesMax");
      m_cntBytesTot    = ldr.getLong("cntBytesTot");
      m_cntBytesTotSq  = ldr.getDouble("cntBytesTotSq");
   }

   public void store(DataLoader loader, String name)
   {
      DataLoader ldr = loader.getChildLoader(name, this);
      
      ldr.setLong("numReqs", m_numReqs);
      ldr.setLong("startedTiming", m_startedTiming);
      ldr.setLong("finishedTiming", m_finishedTiming);

      m_connectAvgMS.store(ldr, "connectAvgMS");
      ldr.setLong("connectMinMS", m_connectMinMS);
      ldr.setLong("connectMaxMS", m_connectMaxMS);
      ldr.setLong("connectTotMS", m_connectTotMS);
      ldr.setDouble("connectTotSqMS", m_connectTotSqMS);

      m_reqSendAvgMS.store(ldr, "reqSendAvgMS");
      ldr.setLong("reqSendMinMS", m_reqSendMinMS);
      ldr.setLong("reqSendMaxMS", m_reqSendMaxMS);
      ldr.setLong("reqSendTotMS", m_reqSendTotMS);
      ldr.setDouble("reqSendTotSqMS", m_reqSendTotSqMS);

      m_hdrReadAvgMS.store(ldr, "hdrReadAvgMS");
      ldr.setLong("hdrReadMinMS", m_hdrReadMinMS);
      ldr.setLong("hdrReadMaxMS", m_hdrReadMaxMS);
      ldr.setLong("hdrReadTotMS", m_hdrReadTotMS);
      ldr.setDouble("hdrReadTotSqMS", m_hdrReadTotSqMS);

      m_cntReadAvgMS.store(ldr, "cntReadAvgMS");
      ldr.setLong("cntReadMinMS", m_cntReadMinMS);
      ldr.setLong("cntReadMaxMS", m_cntReadMaxMS);
      ldr.setLong("cntReadTotMS", m_cntReadTotMS);
      ldr.setDouble("cntReadTotSqMS", m_cntReadTotSqMS);

      m_rndTripAvgMS.store(ldr, "rndTripAvgMS");
      ldr.setLong("rndTripMinMS", m_rndTripMinMS);
      ldr.setLong("rndTripMaxMS", m_rndTripMaxMS);
      ldr.setLong("rndTripTotMS", m_rndTripTotMS);
      ldr.setDouble("rndTripTotSqMS", m_rndTripTotSqMS);

      m_hdrBytesAvg.store(ldr, "hdrBytesAvg");
      ldr.setLong("hdrBytesMin", m_hdrBytesMin);
      ldr.setLong("hdrBytesMax", m_hdrBytesMax);
      ldr.setLong("hdrBytesTot", m_hdrBytesTot);
      ldr.setDouble("hdrBytesTotSq", m_hdrBytesTotSq);

      m_cntBytesAvg.store(ldr, "cntBytesAvg");
      ldr.setLong("cntBytesMin", m_cntBytesMin);
      ldr.setLong("cntBytesMax", m_cntBytesMax);
      ldr.setLong("cntBytesTot", m_cntBytesTot);
      ldr.setDouble("cntBytesTotSq", m_cntBytesTotSq);

   }

   /**
    * The average time, in milliseconds, it took to connect.
    */
   public double avgToConnectMS()
   {
      return m_connectAvgMS.average();
   }

   /**
    * The minimum time, in milliseconds, it took to connect.
    */
   public long minToConnectMS()
   {
      return m_connectMinMS;
   }

   /**
    * The maximum time, in milliseconds, it took to connect.
    */
   public long maxToConnectMS()
   {
      return m_connectMaxMS;
   }

   /**
    * The total time, in milliseconds, spent connecting.
    */
   public long totalToConnectMS()
   {
      return m_connectTotMS;
   }
   

   /* misc statistics */
   private long m_numReqs = 0L;
   private long m_startedTiming = 0L;
   private long m_finishedTiming = 0L;

   /* connect timings (milliseconds) */
   private List m_connectTimings = new ArrayList();
   private AverageAccumulator m_connectAvgMS = new AverageAccumulator();
   private long m_connectMinMS = Long.MAX_VALUE;
   private long m_connectMaxMS = 0L;
   private long m_connectTotMS = 0L;
   private double m_connectTotSqMS = 0.0;

   /* request send timings (milliseconds) */
   private List m_reqSendTimings = new ArrayList();
   private AverageAccumulator m_reqSendAvgMS = new AverageAccumulator();
   private long m_reqSendMinMS = Long.MAX_VALUE;
   private long m_reqSendMaxMS = 0L;
   private long m_reqSendTotMS = 0L;
   private double m_reqSendTotSqMS = 0.0;

   /* header receive timings (milliseconds) */
   private List m_hdrReadTimings = new ArrayList();
   private AverageAccumulator m_hdrReadAvgMS = new AverageAccumulator();
   private long m_hdrReadMinMS = Long.MAX_VALUE;
   private long m_hdrReadMaxMS = 0L;
   private long m_hdrReadTotMS = 0L;
   private double m_hdrReadTotSqMS = 0.0;

   /* content read timings (milliseconds) */
   private List m_cntReadTimings = new ArrayList();
   private AverageAccumulator m_cntReadAvgMS = new AverageAccumulator();
   private long m_cntReadMinMS = Long.MAX_VALUE;
   private long m_cntReadMaxMS = 0L;
   private long m_cntReadTotMS = 0L;
   private double m_cntReadTotSqMS = 0.0;

   private List m_rndTripTimings = new ArrayList();
   private AverageAccumulator m_rndTripAvgMS = new AverageAccumulator();
   private long m_rndTripMinMS = Long.MAX_VALUE;
   private long m_rndTripMaxMS = 0L;
   private long m_rndTripTotMS = 0L;
   private double m_rndTripTotSqMS = 0.0;

   /* header bytes */
   private AverageAccumulator m_hdrBytesAvg = new AverageAccumulator();
   private long m_hdrBytesMin = Long.MAX_VALUE;
   private long m_hdrBytesMax = 0L;
   private long m_hdrBytesTot = 0L;
   private double m_hdrBytesTotSq = 0.0;

   /* content bytes */
   private AverageAccumulator m_cntBytesAvg = new AverageAccumulator();
   private long m_cntBytesMin = Long.MAX_VALUE;
   private long m_cntBytesMax = 0L;
   private long m_cntBytesTot = 0L;
   private double m_cntBytesTotSq = 0.0;

}
