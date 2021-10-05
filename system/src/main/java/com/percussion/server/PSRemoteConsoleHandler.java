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
package com.percussion.server;

import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.error.PSErrorHandler;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.error.PSRemoteConsoleError;
import com.percussion.server.command.PSConsoleCommandParser;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSRemoteConsoleHandler class is used to process remote console
 * commands issued against the E2 server. The PSRemoteConsole class is
 * used by an administrator to issue the console commands.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSRemoteConsoleHandler implements IPSRequestHandler, 
                                               IPSValidateSession
{
   /**
    * Creates a remote console handler for this server.
    */
   public PSRemoteConsoleHandler()
   {
      super();
   }


   /* ************ IPSRequestHandler Interface Implementation ************ */

   /**
    * Process a remote console request. The ACL is first checked for admin
    * access. If the requestor has the appropriate access, the
    * command will be executed.
    * 
    * @param   request      the request object containing all context
    *                        data associated with the request
    */
   public void processRequest(PSRequest request)
   {
      String commandString = null;
      PSResponse resp = request.getResponse();
      if (resp == null) {
          Object[] args = { request.getUserSessionId(), "null response object" };
          com.percussion.log.PSLogManager.write(
             new com.percussion.log.PSLogServerWarning(
             com.percussion.server.IPSServerErrors.RESPONSE_SEND_ERROR, args,
             true, "RemoteConsoleHandler"));
         return;
      }

      /* see if they have access (it logs any problems) */
      try {
         // check the access (which throws an exception on failure)
         PSServer.checkAccessLevel(request, PSAclEntry.SACE_ADMINISTER_SERVER);

         Document   doc = request.getInputDocument();
         Element   root = null;
         if (   (doc == null) ||
               ((root = doc.getDocumentElement()) == null) ) {
            Object[] args = { ms_RequestDTD };
            sendErrorResponse(request, resp, commandString,
               new PSIllegalArgumentException(
                  IPSServerErrors.REQ_DOC_MISSING, args));
            return;
         }

         /* verify this is the appropriate request type */
         if (!ms_RequestDTD.equals(root.getTagName())) {
            Object[] args = { ms_RequestDTD, root.getTagName() };
            sendErrorResponse(request, resp, commandString,
               new PSIllegalArgumentException(
                  IPSServerErrors.REQ_DOC_INVALID_TYPE, args));
            return;
         }

         PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);

         // parse the command string (which validates it, etc.) and exec it
         commandString = tree.getElementData("command");
         IPSConsoleCommand parsedCmd = 
            PSConsoleCommandParser.parse(commandString);
         Document respDoc = parsedCmd.execute(request);

         try {
            resp.setContent(respDoc);
         } catch (Exception e) {
            /*log this*/
            Object[] args = { request.getUserSessionId(),
                  com.percussion.error.PSException.getStackTraceAsString(e) };
            com.percussion.log.PSLogManager.write(
               new com.percussion.log.PSLogServerWarning(
               com.percussion.server.IPSServerErrors.RESPONSE_SEND_ERROR, args,
               true, "RemoteConsoleHandler"));
         }
      } catch (Throwable t) {
         sendErrorResponse(request, resp, commandString, t);
      }
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
   }


   void sendErrorResponse(
      PSRequest request, PSResponse resp, String command, Throwable t)
   {
      PSRemoteConsoleError err = new PSRemoteConsoleError(command, t);
      PSServer.getLogHandler().write(err);

      try {
         resp.setContent(PSErrorHandler.fillErrorResponse(t));
      } catch (Exception e) {
         /*log this*/
         Object[] args = { request.getUserSessionId(),
            com.percussion.error.PSException.getStackTraceAsString(e) };
         com.percussion.log.PSLogManager.write(
            new com.percussion.log.PSLogServerWarning(
            com.percussion.server.IPSServerErrors.RESPONSE_SEND_ERROR, args,
            true, "RemoteConsoleHandler"));
      }
   }

   static final String ms_RequestDTD   = "PSXRemoteConsoleCommand";
}

