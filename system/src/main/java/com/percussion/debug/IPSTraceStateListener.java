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

package com.percussion.debug;

import com.percussion.design.objectstore.PSTraceInfo;

/**
 * Used by listeners to be informed of Trace start and stop events.
 */
public interface IPSTraceStateListener
{
   
   /**
    * Used to notify listeners when tracing has been enabled.
    * 
    * @param traceInfo the PSTraceInfo object that has been enabled
    * @roseuid 39F84A1A008C
    */
   public void traceStarted(PSTraceInfo traceInfo);
   
   /**
    * Used to notify listeners when tracing has been disabled.
    * 
    * @param traceInfo the PSTraceInfo object that has been disabled
    * @roseuid 39F84A86030D
    */
   public void traceStopped(PSTraceInfo traceInfo);

   /**
    * Used to notify listeners when tracing has been restarted.
    *
    * @param traceInfo the PSTraceInfo object that has been re-enabled
    */
   public void traceRestarted(PSTraceInfo traceInfo);

}
