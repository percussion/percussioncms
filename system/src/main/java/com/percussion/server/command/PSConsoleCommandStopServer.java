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
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRemoteConsoleHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Locale;


/**
 * The PSConsoleCommandStopServer class implements processing of the
 * "stop server" console command.
 *
 * @see         PSRemoteConsoleHandler
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSConsoleCommandStopServer extends PSConsoleCommand
{
   /**
    * The constructor for this class.
    *
    * @param      cmdArgs      the argument string to use when executing
    *                           this command
    *
    */
   public PSConsoleCommandStopServer(String cmdArgs)
      throws PSIllegalArgumentException
   {
      super(cmdArgs);

      // there should be no other args for this command
      if ((cmdArgs != null) && (cmdArgs.length() > 0)) {
         Object[] args = { ms_cmdName, cmdArgs };
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_UNEXPECTED_ARGS, args);
      }
   }

   /**
    * Execute the command specified by this object. The results are returned
    * as an XML document of the appropriate structure for the command.
    *   
    * @param      request                     the requestor object
    *   <P>
    * The execution of this command results in the following XML document
    * structure:
    * <PRE><CODE>
    *      &lt;ELEMENT PSXConsoleCommandResults   (command, resultCode, resultText)&gt;
    *
    *      &lt;--
    *         the command that was executed
    *      --&gt;
    *      &lt;ELEMENT command                     (#PCDATA)&gt;
    *
    *      &lt;--
    *         the result code for the command execution
    *      --&gt;
    *      &lt;ELEMENT resultCode                  (#PCDATA)&gt;
    *
    *      &lt;--
    *         the message text associated with the result code
    *      --&gt;
    *      &lt;ELEMENT resultText                  (#PCDATA)&gt;
    * </CODE></PRE>
    *
    * @return                                 the result document
    *
    * @exception   PSConsoleCommandException   if an error occurs during
    *                                          execution
    */
   public Document execute(PSRequest request)
      throws PSConsoleCommandException
   {
      long stopTime = 0;
      
      stopTime = PSServer.getServerConfiguration().getShutDownDelayMS();

      // build the response doc for the user
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(
         respDoc, "PSXConsoleCommandResults");

      PSXmlDocumentBuilder.addElement(respDoc, root, "command", ms_cmdName);

      /* the result message tells them when it's scheduled to terminate.
       * the message is provided in the requestor's locale
       */
      PSXmlDocumentBuilder.addElement(respDoc, root, "resultCode",
         String.valueOf(IPSServerErrors.RCONSOLE_SERVER_SHUTDOWN_SCHEDULED));

      Locale loc;
      if (request != null)
         loc = request.getPreferredLocale();
      else
         loc = Locale.getDefault();

      Object[] args = { String.valueOf(stopTime) };
      String termMsg = PSErrorManager.createMessage(
         IPSServerErrors.RCONSOLE_SERVER_SHUTDOWN_SCHEDULED, args, loc);
      PSXmlDocumentBuilder.addElement(respDoc, root, "resultText", termMsg);

      // schedule the shutdown in so we can get our response off
      PSConsole.printMsg("RemoteConsole", termMsg);
      PSServer.scheduleShutdown(stopTime);

      return respDoc;
   }

   /**
    * allow package members to see our command name
    */
   final static String   ms_cmdName = "stop server";
}

