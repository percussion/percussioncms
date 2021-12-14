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

public class RequestTimer
{
   public void startRequest(long time)
   {
      m_enterTime = time;
   }

   public long startRequest()
   {
      return m_enterTime;
   }

   public void endRequest(long time)
   {
      m_exitTime = time;
   }

   public long endRequest()
   {
      return m_exitTime;
   }

   public void beforeConnect(long time)
   {
      m_beforeConnect = time;
   }

   public long beforeConnect()
   {
      return m_beforeConnect;
   }

   public void afterConnect(long time)
   {
      m_afterConnect = time;
   }

   public long afterConnect()
   {
      return m_afterConnect;
   }

   public void afterHeader(long time)
   {
      m_afterHeader = time;
   }

   public long afterHeader()
   {
      return m_afterHeader;
   }

   public void afterBody(long time)
   {
      m_afterBody = time;
   }

   public long afterBody()
   {
      return m_afterBody;
   }

   public void totalBytes(long bytes)
   {
      m_totalBytes = bytes;
   }

   public long totalBytes()
   {
      return m_totalBytes;
   }

   public void reportedContentLength(long bytes)
   {
      m_repContentLen = bytes;
   }

   public long reportedContentLength()
   {
      return m_repContentLen;
   }

   public void httpCode(int code)
   {
      m_httpCode = code;
   }

   public int httpCode()
   {
      return m_httpCode;
   }

   public void bodyBytes(long bytes)
   {
      m_bodyBytes = bytes;
   }

   public long bodyBytes()
   {
      return m_bodyBytes;
   }

   private int m_httpCode = 0;
   private long m_repContentLen = 0L;
   private long m_enterTime = 0L;
   private long m_beforeConnect = 0L;
   private long m_afterConnect = 0L;
   private long m_afterHeader = 0L;
   private long m_afterBody = 0L;
   private long m_exitTime = 0L;
   private long m_totalBytes = 0L;
   private long m_bodyBytes = 0L;
}
