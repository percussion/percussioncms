/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.test.http;

public class RequestStats
{
   public RequestStats()
   {
   }

   public RequestStats(RequestTimer timer)
   {
      m_totalRespTime = timer.endRequest() - timer.startRequest();
      if (m_totalRespTime < 0)
         m_totalRespTime = 0;

      m_totalRespTimeSq = (double)m_totalRespTime * (double)m_totalRespTime;

      m_minRespTime = m_totalRespTime;
      m_maxRespTime = m_totalRespTime;

      m_totalConnTime = timer.afterConnect() - timer.beforeConnect();
      m_totalConnTimeSq = (double)m_totalConnTime * (double)m_totalConnTime;

      m_minConnTime = m_totalConnTime;
      m_maxConnTime = m_totalConnTime;

      m_totalBytes = timer.totalBytes();
      m_totalBytesSq = (double)m_totalBytes * (double)m_totalBytes;
      m_minBytes = m_totalBytes;
      m_maxBytes = m_totalBytes;

      m_totalBodyBytes = timer.bodyBytes();
      m_totalBodyBytesSq = (double)m_totalBodyBytes * (double)m_totalBodyBytes;
      m_minBodyBytes = m_totalBodyBytes;
      m_maxBodyBytes = m_totalBodyBytes;

      m_totalConnects = 1;
      m_totalErrors = (timer.httpCode() == 200) ? 0 : 1;
   }

   public void add(RequestStats stats)
   {
      m_totalRespTime += stats.m_totalRespTime;
      m_totalRespTimeSq += stats.m_totalRespTimeSq;
      m_minRespTime = Math.min(m_minRespTime, stats.m_minRespTime);
      m_maxRespTime = Math.max(m_maxRespTime, stats.m_maxRespTime);

      m_totalConnTime += stats.m_totalConnTime;
      m_totalConnTimeSq += stats.m_totalConnTimeSq;
      m_minConnTime = Math.min(m_minConnTime, stats.m_minConnTime);
      m_maxConnTime = Math.max(m_maxConnTime, stats.m_maxConnTime);

      m_totalBytes += stats.m_totalBytes;
      m_totalBytesSq += stats.m_totalBytesSq;
      m_minBytes = Math.min(m_minBytes, stats.m_minBytes);
      m_maxBytes = Math.max(m_maxBytes, stats.m_maxBytes);

      m_totalBodyBytes += stats.m_totalBodyBytes;
      m_totalBodyBytesSq += stats.m_totalBodyBytesSq;
      m_minBodyBytes = Math.min(m_minBodyBytes, stats.m_minBodyBytes);
      m_maxBodyBytes = Math.min(m_maxBodyBytes, stats.m_maxBodyBytes);

      m_totalConnects += stats.m_totalConnects;
      m_totalErrors += stats.m_totalErrors;
   }

   public String toXMLString()
   {
      StringBuilder buf = new StringBuilder(300);
      buf.append("<HttpRequestStats>\n");
      buf.append("<ResponseTime>\n");
      buf.append("<Total>" + m_totalRespTime + "</Total>\n");
      buf.append("<Min>" + m_minRespTime + "</Min>\n");
      buf.append("<Max>" + m_maxRespTime + "</Max>\n");
      buf.append("</ResponseTime>\n");
      buf.append("<ConnectTime>\n");
      buf.append("<Total>" + m_totalConnTime + "</Total>\n");
      buf.append("<Min>" + m_minConnTime + "</Min>\n");
      buf.append("<Max>" + m_maxConnTime + "</Max>\n");
      buf.append("</ConnectTime>\n");
      buf.append("<Bytes>\n");
      buf.append("<Total>" + m_totalBytes + "</Total>\n");
      buf.append("<Min>" + m_minBytes + "</Min>\n");
      buf.append("<Max>" + m_maxBytes + "</Max>\n");
      buf.append("</Bytes>\n");
      buf.append("<Misc>\n");
      buf.append("<Connects>" + m_totalConnects + "</Connects>\n");
      buf.append("<Errors>" + m_totalErrors + "</Errors>\n");
      buf.append("</Misc>\n");
      buf.append("</HttpRequestStats>\n");
      return buf.toString();
   }

   private long m_totalRespTime = 0;
   private double m_totalRespTimeSq = 0;
   private long m_minRespTime = 0;
   private long m_maxRespTime = 0;
   
   private long m_totalConnTime = 0;
   private double m_totalConnTimeSq = 0;
   private long m_minConnTime = 0;
   private long m_maxConnTime = 0;

   private long m_totalBytes = 0;
   private double m_totalBytesSq = 0;
   private long m_minBytes = 0;
   private long m_maxBytes = 0;

   private long m_totalBodyBytes = 0;
   private double m_totalBodyBytesSq = 0;
   private long m_minBodyBytes = 0;
   private long m_maxBodyBytes = 0;

   private int m_totalConnects = 0;
   private int m_totalErrors = 0;
}
