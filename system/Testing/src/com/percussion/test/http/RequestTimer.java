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
