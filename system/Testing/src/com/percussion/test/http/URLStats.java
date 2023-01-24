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

import java.net.URL;

public class URLStats
{
   public URLStats(URL url)
   {
      m_url = url;
      m_stats = new RequestStats();
      m_totalReqs = 0;
   }

   public void add(RequestTimer timer)
   {
      m_stats.add(new RequestStats(timer));
      m_totalReqs++;
   }

   private URL m_url;
   private RequestStats m_stats;
   private long m_totalReqs;
}
