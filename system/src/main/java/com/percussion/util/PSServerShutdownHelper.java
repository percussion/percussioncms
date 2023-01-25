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
package com.percussion.util;

import com.percussion.process.IPSShutdownListener;

import java.io.File;

/**
 * Shutdown helper class: Any user of this class must implement
 * IPSShutdownListener.psShutdown().
 */
public abstract class PSServerShutdownHelper extends Thread 
   implements Runnable,  IPSShutdownListener {
   public static File  shutdownDescriptor = null;
  
   /**
    * TODO: vamsi update the doc.
    * Returns shutdown descriptor file path.
    * you need to  update the rxservice.c to reflect where it needs creation
    * @return 
    */
   public static String getShutdownDescriptor()
   {
      return System.getProperty("user.dir") + "/rxservice.sd";
   }

   /**
    * Daemons can use {DaemonName}.sd as the shutdown descriptor under the 
    * Rx root directory. 
    */
   public static void cleanItsDescriptor()
   {
      if ( shutdownDescriptor == null )
         shutdownDescriptor = new File(getShutdownDescriptor());
      if ( shutdownDescriptor.exists() )
         shutdownDescriptor.delete();
   }
   
   /**
    * watches for the shutdown file to exist and then execute psShutdown else
    * else sleep 5 sec...
    */
   public void run() {
      int next = 0;
      while (true) {
         if ( shutdownDescriptor == null )
            shutdownDescriptor = new File(getShutdownDescriptor());
         if ( shutdownDescriptor.exists() )
         {
            shutdownDescriptor.delete();
            psShutdown();
         }
         else
         {
            try 
            {
               Thread.sleep(5000);
            }
            catch (InterruptedException ie) 
            {
               Thread.currentThread().interrupt();
               return;
            }
         }
         
      }
   }
}

