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


/**
 * Used to generate trace messages for the App Handler Processing trace message type (0x0004).  Includes the number of resources checked, Handler used to process a request, and the request name and dataset name of the resource used.
 */
public class PSTraceAppHandlerProc extends PSTraceMessage
{
   
   /**
    * The constructor for this class
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDD0B9008C
    */
   public PSTraceAppHandlerProc(int typeFlag) 
   {
      super(typeFlag);
   }
   
   // see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceAppHandlerProc_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    *
    * @param source an object array containing info required:
    * Object[] = {arg, MessageFormat}
    * - Message for request page name
    * - Message for each before dataset checked, and after if it was selected or not.
    * - Message if a file or a static page
    *
    * @return the message body as a String
    * @roseuid 39FEE2F203C8
    */
   protected String getMessageBody(java.lang.Object source)
   {
      // just use message format in args
      return getMessageFromArgs((Object[])source);
   }

}
