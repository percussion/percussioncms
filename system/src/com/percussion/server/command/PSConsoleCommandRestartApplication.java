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
package com.percussion.server.command;

import com.percussion.error.PSErrorManager;
import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRemoteConsoleHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSConsoleCommandRestartApplication class implements processing of the
 * "restart application appName" console command.
 *
 * @see         PSRemoteConsoleHandler
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSConsoleCommandRestartApplication extends PSConsoleCommand
{
   /**
    * The constructor for this class.
    *
    * @param      cmdArgs      the argument string to use when executing
    *                           this command
    *
    */
   public PSConsoleCommandRestartApplication(String cmdArgs)
      throws PSIllegalArgumentException
   {
      super(cmdArgs);

      // need the application name for this command
      if ((cmdArgs == null) || (cmdArgs.length() == 0)) {
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_APP_NAME_REQD, ms_cmdName);
      }
   }

   /**
    * Execute the command specified by this object. The results are returned
    * as an XML document of the appropriate structure for the command.
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
    * @param      request                     the requestor object
    *
    * @return                                 the result document
    *
    * @exception   PSConsoleCommandException   if an error occurs during
    *                                          execution
    */
   public Document execute(PSRequest request)
      throws PSConsoleCommandException
   {
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(
         respDoc, "PSXConsoleCommandResults");
      PSXmlDocumentBuilder.addElement(respDoc, root, "command", ms_cmdName + " " + m_cmdArgs);

      Locale loc;
      if (request != null)
         loc = request.getPreferredLocale();
      else
         loc = Locale.getDefault();

      // shut it down (nothing happens if it's already down)
      PSServer.shutdownApplication(m_cmdArgs);

      // and then start it back up
      try {
         PSServer.startApplication(m_cmdArgs);
         PSXmlDocumentBuilder.addElement(respDoc, root, "resultCode",
            String.valueOf(IPSServerErrors.RCONSOLE_APP_RESTARTED));
         Object[] args = { m_cmdArgs };
         String termMsg = PSErrorManager.createMessage(
            IPSServerErrors.RCONSOLE_APP_RESTARTED, args, loc);
         PSXmlDocumentBuilder.addElement(respDoc, root, "resultText", termMsg);
      } catch (Exception e) {
         String msg;
         if (e instanceof com.percussion.error.PSException)
            msg = ((PSException)e).getLocalizedMessage(loc);
         else
            msg = e.getMessage();

         Object[] args = { (ms_cmdName + " " + m_cmdArgs), msg };
         throw new PSConsoleCommandException(
            IPSServerErrors.RCONSOLE_EXEC_EXCEPTION, args);
      }

      return respDoc;
   }


   /**
    * allow package members to see our command name
    */
   final static String   ms_cmdName = "restart application";
}

