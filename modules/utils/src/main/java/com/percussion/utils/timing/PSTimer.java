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
package com.percussion.utils.timing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A utility class used to log elapsed time.
 */
public class PSTimer
{
   PSStopwatch m_watch = new PSStopwatch();
   private static final Logger log = LogManager.getLogger(PSTimer.class);

   public PSTimer(){
      //NOOP
   }

   public PSTimer(Logger log){
      m_watch.start();
   }
   /**
    * Log the message along with the elapsed time.
    * @param msg the log message, assumed not <code>null</code>.
    */
   public void logElapsed(String msg)
   {
      m_watch.stop();
      log.debug( "{} {}",msg, m_watch);

   }
}
