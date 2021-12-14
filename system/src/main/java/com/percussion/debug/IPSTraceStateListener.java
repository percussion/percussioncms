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
