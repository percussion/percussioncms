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
package com.percussion.server.command;

import com.percussion.error.PSErrorManager;
import com.percussion.server.IPSConsoleCommand;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRemoteConsoleHandler;
import com.percussion.server.PSRequest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Locale;


/**
 * The PSConsoleCommand abstract class is the base for all console command
 * handlers.
 *
 * @see         PSRemoteConsoleHandler
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public abstract class PSConsoleCommand implements IPSConsoleCommand
{
   /**
    * The constructor for this class.
    *
    * @param      cmdArgs      the argument string to use when executing
    *                           this command
    */
   PSConsoleCommand(String cmdArgs)
   {
      super();
      m_cmdArgs = cmdArgs;
   }

   /**
    * Creates basic recommended result document. This document conforms to the 
    * following dtd:
    * <pre><code>
    *    &lt;ELEMENT PSXConsoleCommandResults   (command, resultCode, 
    *       resultText)&gt;
    *
    *    &lt;--
    *       the command that was executed
    *    --&gt;
    *    &lt;ELEMENT command                    (#PCDATA)&gt;
    *
    *    &lt;--
    *       the result code for the command execution
    *    --&gt;
    *    &lt;ELEMENT resultCode                 (#PCDATA)&gt;
    *
    *    &lt;--
    *       the message text associated with the result code
    *    --&gt;
    *    &lt;ELEMENT resultText                 (#PCDATA)&gt;
    * </code></pre>
    * 
    * @param The request supplied to the <code>execute</code> method. May be
    * <code>null</code>.
    * 
    * @param command The full command, w/o args, never <code>null</code> or
    * empty.
    * 
    * @param resultCode The numeric code representing the outcome of the 
    * command. Typically, one of the {@link IPSServerErrors}.RCONSOLE_xxx
    * codes. The numeric value of this code will be used as the resultCode
    * and its associated message will be used as the resultText.
    * <p>Supply {@link IPSServerErrors.RCONSOLE_SUCCESS} if the command is
    * successful and does not have any specific result message.
    * 
    * @return the result document, never <code>null</code>
    */
   protected Document getResultsDocument(PSRequest request, 
         String command, int resultCode, Object[] args)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc,
         "PSXConsoleCommandResults");
      PSXmlDocumentBuilder.addElement(doc, root, "command", command +
            " " + m_cmdArgs);
  
      PSXmlDocumentBuilder.addElement(doc, root, "resultCode", 
            String.valueOf(resultCode));

     Locale loc;
     if (request != null)
        loc = request.getPreferredLocale();
     else
        loc = Locale.getDefault();

      String termMsg = PSErrorManager.createMessage(
            resultCode, args, loc);
      PSXmlDocumentBuilder.addElement(doc, root, "resultText", termMsg);
      return doc;
   }
   

   protected String m_cmdArgs = null;

}

