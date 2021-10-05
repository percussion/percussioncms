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
 * Used to generate trace messages for the Output Convesion trace message type (0x4000).  Includes:
 * When loading XSL file, display all URL conversions
 * For each request:
 * Is conversion being done
 * Type of conversion
 * If HTML, display XSL URL
 */
public class PSTraceOutputConversion extends PSTraceMessage
{
   
   /**
    * Constructor for this class.
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDE02D02DE
    */
   public PSTraceOutputConversion(int typeFlag)
   {
      super(typeFlag);
   }

   //see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceOutputConv_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    *
    * @param source an array of objects containing the information required for the
    * trace message:
    * - String type (i.e. "html")
    * - String stylesheet name
    * @return the message body
    * @roseuid 39FEE2F402FD
    */
   protected String getMessageBody(java.lang.Object source) 
   {
      Object[] args = (Object[])source;
      if (args.length != 2)
         throw new IllegalArgumentException("Invalid number of args");

      return MessageFormat.format(ms_bundle.getString("traceOutputConv_msg"), args);
   }
}
