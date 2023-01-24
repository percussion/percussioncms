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

package com.percussion.server;

import java.util.Date;


/**
 * This class is used to store the statistics for the server.
 *
 * @see         com.percussion.server.PSServer#getStatistics
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSServerStatistics extends PSHandlerStatistics
{
   /**
    * Construct an Server statistics object with the specified
    *   Server start time.
    *
    * @param   startTime      the time/date the Server started
    */
   public PSServerStatistics(Date startTime)
   {
      super(startTime);
   }

   /**
    * Construct an Server statistics object using the current time as
    * the time the Server was started.
    */
   public PSServerStatistics()
   {
      this(new Date());
   }
}

