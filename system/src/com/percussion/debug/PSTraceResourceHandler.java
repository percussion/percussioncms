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

/**
 * Used to generate trace messages for the Resource Handler trace message type (0x0040).  Includes validation processing, and for each step in the plan, what it is and what the results are.
 * <p>
 * For a Query: 
 * All data associated with the result set returned by the current query. Binary data is displayed in hex.
 * 
 * For an Update:
 * For each row - the action taken (Insert, update, etc). If not skipped, the values bound to each column.
 */
public class PSTraceResourceHandler extends PSTraceMessage
{
   
   /**
    * Constructor for this class.
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDD4DF031C
    */
   public PSTraceResourceHandler(int typeFlag)
   {
      super(typeFlag);
   }

   //see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceResourceHandler_dispname");
   }
   
   /**
    * Formats the output for the body of the message, extracting the information 
    * required from the source object.
    * 
    * @param source an object containing the information required for the
    * trace message.  For most calls, this will be an object array where the last
    * object in the array is a string specifying a message format, and all objects
    * before that constitute the parameters for the message (so that the array can
    * simply be passed to MessageFormat).  If the array contains a single String, 
    * then that is a message format with no parameters.  
    *
    * @return the message body
    * @roseuid 39FEE2F30177
    */
   protected String getMessageBody(Object source)
   {
      String msg = null;

      // just use message format in args
      msg = getMessageFromArgs((Object[])source);

      return msg;
   }

}
