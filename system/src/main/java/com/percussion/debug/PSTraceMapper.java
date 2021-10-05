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
 * Used to generate trace messages for the Mapper trace message type (0x0080).
 * Includes:
 * Each mapping that was skipped (query only)
 * For each UDF: 
 * Value of each input param (by doing a toString on it) in the form 'param=value'.
 * The value returned (by doing a toString on it) in the form 'return=value'
 */
public class PSTraceMapper extends PSTraceMessage 
{
   
   /**
    * The constructor for this class.
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDD58F0157
    */
   public PSTraceMapper(int typeFlag)
   {
      super(typeFlag);
   }

   // see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceMapper_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    *
    * @param source an object containing the information required for the trace
    * message.  Three different cases handled:
    * For skipped/used mappings on a query:
    * - Boolean: used or not
    * For before and after params for a UDF:
    * - String udfName, object[] args, object result
    * For mapping itself:
    * - columnname, nodename
    * @return the message body
    * @roseuid 39FEE2F30242
    */
   protected String getMessageBody(Object source)
   {
      StringBuilder buf = new StringBuilder();

      // check the inputs
      Object[] args = (Object[])source;
      if (args.length == 1)
      {
         // skipped or used mapping: Boolean
         String msg;
         if (((Boolean)args[0]).booleanValue())
            msg = "traceMapper_useMapping";
         else
            msg = "traceMapper_skipMapping";

         buf.append(ms_bundle.getString(msg));
      }
      else if (args.length == 3)
      {
         // udf execution - print out name and result
         buf.append(MessageFormat.format(
            ms_bundle.getString("traceMapper_Udf"), args));

         // now each param
         Object[] params = (Object[])args[1];
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
      }
      else if (args.length == 2)
      {
         // display mapping: dataname, nodename
         buf.append(MessageFormat.format(
            ms_bundle.getString("traceMapper_dispMapping"), args));
      }
      else
         throw new IllegalArgumentException(
            "Invalid number of arguments in source Object array");

      return new String(buf);
   }
}
