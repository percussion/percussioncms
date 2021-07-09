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
 * Used to generate trace messages for the Exit Processing trace message type (0x0400).  Includes:
 * Type of exit
 * Name of each exit executed
 * For each exit:
 * Value of each input param (by doing a toString on it) in the form 'param=value'.
 */
public class PSTraceExitProc extends PSTraceMessage
{
   
   /**
    * The constructor for this class
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDDA100271
    */
   public PSTraceExitProc(int typeFlag)
   {
      super(typeFlag);
   }

   // see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceExitProc_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    *
    * @param source an array of objects containing the information required for the
    * trace message:
    * - String exit type
    * - String classname of exit
    * - object[] args for the exit
    * @return the message body
    * @roseuid 39FEE2F4009C
    */
   protected String getMessageBody(java.lang.Object source)
   {
      StringBuffer buf = new StringBuffer();

      Object[] args = (Object[])source;

      if (args.length < 3)
         throw new IllegalArgumentException("PSTraceExitProc: invalid source args");
      
      // Add the name and type
      buf.append(
         MessageFormat.format(
            ms_bundle.getString("traceExitProc_message"), args));

      // Add each parameter
      Object[] params = (Object[])args[2];
      if (params != null)
      {
         for (int i = 0; i < params.length; i++)
         {
            buf.append(NEW_LINE);
            buf.append("Param[");
            buf.append(Integer.toString(i + 1));
            buf.append("] = ");

            if (params[i] != null)
               buf.append(params[i].toString());
            else
               buf.append("null");
         }
      }

      return new String(buf);
   }
}
