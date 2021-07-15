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

import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import java.text.MessageFormat;

/**
 * Used to generate trace message for Post Exit XML Trace message type (0x1000).
 *
 * Prints out the entire XML document sent to the first exit and returned from each successive exit.
 */
public class PSTracePostExitXml extends PSTraceMessage
{

   /**
    * The constructor for this class.
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDDACF0399
    */
   public PSTracePostExitXml(int typeFlag)
   {
      super(typeFlag);
   }

   //see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("tracePostExitXml_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    *
    * @param source an object containing the information required for the trace
    * message.  Two cases are handled:
    * initial doc:
    * - Document the doc
    * after each exit:
    * - Document the doc
    * - String Exitname
    * @return the message body
    * @roseuid 39FEE2F40232
    */
   protected String getMessageBody(Object source)
   {
      Object[] args = (Object[])source;
      StringBuilder buf = new StringBuilder();

      // handle two cases for inputs
      if (args.length == 1)
      {
         buf.append(ms_bundle.getString("tracePostExitXml_initialDoc"));
         buf.append(NEW_LINE);
      }
      else if (args.length == 2)
      {
         buf.append(MessageFormat.format(
            ms_bundle.getString("tracePostExitXml_ExitDoc"), args));
         buf.append(NEW_LINE);
      }
      else
         throw new IllegalArgumentException("invalid source args");

      // XML doc may be null
      if (args[0] != null)
         buf.append(PSXmlDocumentBuilder.toString((Document)args[0]));
      else
         buf.append(ms_bundle.getString("tracePostExitXml_NullDoc"));

      return new String(buf);
   }
}
