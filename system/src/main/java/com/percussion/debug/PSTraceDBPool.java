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

import java.text.MessageFormat;

/**
 * Used to generate trace messages for the DB Pool trace message type (0x0200).
 * Includes:
 * When connection requested, did it come from pool or was a new connection made. For each new connection attempt, all info used (except pw) and whether successful or failed.
 */
public class PSTraceDBPool extends PSTraceMessage
{

   /**
    * Constructor for this class
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDD70103A9
    */
   public PSTraceDBPool(int typeFlag)
   {
      super(typeFlag);
   }

   // see parent class for javadoc
   protected String getMessageHeader() 
   {
      return ms_bundle.getString("traceDbPool_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    *
    * @param source an array of objects containing the information required for the
    * trace message:
    * - Boolean IsNewConnection
    * - String driver
    * - String server
    * - String database
    * - String userid
    * - int Wait in Milliseconds
    *
    * If IsNewConnection = <code>false</code>, the the others will not be included in
    * the trace message.
    * @return the message body
    * @roseuid 39FEE2F303B9
    */
   protected String getMessageBody(java.lang.Object source)
   {
      Object[] args = (Object[])source;
      String message = "";

      // if first arg is false, log a message to that effect
      String foo;
      if (((String)args[0]).toLowerCase().equals("false"))
      {
         message = MessageFormat.format(
            ms_bundle.getString("traceDbPool_fromPool"), args);
      }
      else
      {
         message = MessageFormat.format(
            ms_bundle.getString("traceDbPool_newConnection"), args);
      }
      return message;
   }
}
