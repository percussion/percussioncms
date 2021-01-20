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
package com.percussion.server.command;

import com.percussion.design.objectstore.server.PSApplicationSummary;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRemoteConsoleHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSConsoleCommandShowApplications class implements processing of the
 * "show applications" console command.
 *
 * @see         PSRemoteConsoleHandler
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSConsoleCommandShowApplications extends PSConsoleCommand
{
   /**
    * The constructor for this class.
    *
    * @param      cmdArgs      the argument string to use when executing
    *                           this command
    *
    */
   public PSConsoleCommandShowApplications(String cmdArgs)
      throws PSIllegalArgumentException
   {
      super(null);

      if ((cmdArgs == null) || (cmdArgs.length() == 0))
         m_statusCodesToShow = SHOW_STATUS_ALL;   // the default
      else {
         cmdArgs = cmdArgs.toLowerCase();
         if ("all".equals(cmdArgs))
            m_statusCodesToShow = SHOW_STATUS_ALL;
         else if ("active".equals(cmdArgs))
            m_statusCodesToShow = SHOW_STATUS_ACTIVE;
         else if ("disabled".equals(cmdArgs))
            m_statusCodesToShow = SHOW_STATUS_DISABLED;
         else {   // what's this?!
            Object[] args = { ms_cmdName, "all, active, disabled", cmdArgs };
            throw new PSIllegalArgumentException(
               IPSServerErrors.RCONSOLE_INVALID_SUBCMD, args);
         }
      }
   }

   /**
    * Execute the command specified by this object. The results are returned
    * as an XML document of the appropriate structure for the command.
    *   <P>
    * The execution of this command results in the following XML document
    * structure:
    * <PRE><CODE>
    *      &lt;ELEMENT PSXConsoleCommandResults   (command, resultCode, resultText, Applications?)&gt;
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
    *
    *      &lt;--
    *         the container for returned applications
    *      --&gt;
    *      &lt;ELEMENT Applications               (Application*)&gt;
    *
    *      &lt;--
    *         a returned application
    *      --&gt;
    *      &lt;ELEMENT Application                  (name)&gt;
    *
    *      &lt;--
    *         the attributes for the application:
    *
    *         - id: the unique id of the application
    *         - active: yes if it's running, no if it's not running
    *         - enabled: yes if it's enabled, no if it's disabled
    *      --&gt;
    *      &lt;ATTLIST Application
    *         id            ID               #REQUIRED
    *         active      (yes | no)      #REQUIRED
    *         enabled      (yes | no)      #REQUIRED
    *      &gt;
    *
    *      &lt;--
    *         the name of the application
    *      --&gt;
    *      &lt;ELEMENT name                        (#PCDATA)&gt;
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
      PSXmlDocumentBuilder.addElement(respDoc, root, "command", ms_cmdName);
      PSXmlDocumentBuilder.addElement(respDoc, root, "resultCode", "0");
      PSXmlDocumentBuilder.addElement(respDoc, root, "resultText", "");

      // this isn't really the root, but it acts as such
      root = PSXmlDocumentBuilder.addEmptyElement(
         respDoc, root, "Applications");

      Element node;
      PSApplicationSummary[] apps
         = PSServer.getApplicationSummaries(request);

      for (int i = 0; i < apps.length; i++) {
         PSApplicationSummary sum = apps[i];
         // make sure we're allowed to log this type
         if (!sum.isActive()) {
            if ((m_statusCodesToShow & SHOW_STATUS_DISABLED) == 0)
               continue;
         }
         else if ((m_statusCodesToShow & SHOW_STATUS_ACTIVE) == 0)
            continue;

         node = PSXmlDocumentBuilder.addEmptyElement(
            respDoc, root, "Application");
         node.setAttribute("id",   "" + sum.getId());
            node.setAttribute("active", sum.isActive() ?   "yes" : "no");
            node.setAttribute("enabled", sum.isEnabled() ? "yes" : "no");
            PSXmlDocumentBuilder.addElement(
               respDoc, node, "name", sum.getName());
      }

      return respDoc;
   }

   private static final int   SHOW_STATUS_ACTIVE   = 0x0001;
   private static final int   SHOW_STATUS_DISABLED   = 0x0002;
   private static final int   SHOW_STATUS_ALL      = SHOW_STATUS_ACTIVE   +
                                                     SHOW_STATUS_DISABLED;

   private int m_statusCodesToShow;

   /**
    * allow package members to see our command name
    */
   final static String   ms_cmdName = "show applications";
}

