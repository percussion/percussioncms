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
package com.percussion.utils.thread;

import com.percussion.utils.request.PSRequestInfoBase;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author JaySeletz
 *
 */
public class PSThreadUtils
{
   public static final String JOB_CANCELLED = "JOB_CANCELLED";
   


   public static void setInterrupt(AtomicBoolean bool)
   {
      PSRequestInfoBase.setRequestInfo(JOB_CANCELLED, bool);
   }
   
   /**
    * Check if the current thread is interrupted, and if so, throw an exception.  Does not
    * clear the interrupted state of the current thread.  Should be called from potentially long
    * running methods that may be used within a thread that should be interruptable. 
    * 
    * @throws PSThreadInterruptedException if the current thread is interrupted.
    */
   public static void checkForInterrupt() throws PSThreadInterruptedException
   {
      boolean jobCancelled = false;
      /**
       * Workaround for    https://issues.jboss.org/browse/JBAS-1234
       * We set our own interrupt and Check.  Can revert to old mechanism after Jetty.
       */
      if (PSRequestInfoBase.isInited())
      {
         Object cancelledObj = PSRequestInfoBase.getRequestInfo(JOB_CANCELLED);
         jobCancelled = (cancelledObj!=null && cancelledObj.equals(Boolean.TRUE));
      }
      
      if (Thread.currentThread().isInterrupted() || jobCancelled)
         throw new PSThreadInterruptedException();
   }
}
