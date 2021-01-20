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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.utils.thread;

import java.util.concurrent.atomic.AtomicBoolean;

import com.percussion.utils.request.PSRequestInfoBase;

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
