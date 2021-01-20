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

package com.percussion.debug;

import com.percussion.design.objectstore.PSTraceInfo;

/**
 * This interface is implemented by classes that are used to generate trace 
 * messages of different types.  Each type of trace information will be 
 * represented by a class that
 * implements this interface.
 */
public interface IPSTraceMessage
{

   /**
    * Retreives the necessary information from the object it was instantiated
    * with, and then formats the output of its message and sends it to the
    * supplied output stream.  Will not supply a messge body if Timestamp only
    * option is true.
    * @param traceInfo The trace options set by the application.  May not be
    * <code>null</code>.
    * @param source the source of the information to be used in generating the
    * trace message.   May not be <code>null</code>.
    * @param target The writer to which the formatted message written.
    * May not be <code>null</code>.
    * @throws IOException if there is a problem writing to the writer.
    *
    */
   public void printTrace(PSTraceInfo traceInfo, Object source,
      PSTraceWriter target)
   throws java.io.IOException;

   /**
    * Used to identify the type of information this object is used to trace.
    *
    * @return The type flag of the object implementing this interface.
    */
   public int getTypeFlag();
}
