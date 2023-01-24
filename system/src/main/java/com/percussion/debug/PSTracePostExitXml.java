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
