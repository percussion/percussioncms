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

import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;

import org.w3c.dom.Document;

/**
 * This class implements the "debug search" command handling.
 *
 * @author paulhoward
 */
public class PSConsoleCommandDebugSearch extends PSConsoleCommand
{
   /**
    * Ctor required by framework.
    * 
    * @param args Only validated that not empty. During execution, we
    * check for 'on' (we also allow 'true', 'enable' and 'yes'), otherwise it 
    * is intrpreted as 'off'.
    *  
    * @throws PSIllegalArgumentException If empty. 
    * <p>Note: we use PSIllegal... here because that's what the framework uses.
    */
   public PSConsoleCommandDebugSearch(String args)
      throws PSIllegalArgumentException
   {
      super(args);   
      // need the debug mode ('on/'off') for this command
      if ((args == null) || (args.length() == 0)) 
      {
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_DEBUGMODE_REQD, null);
      }
   }
   
   /**
    * Enables/disables the debug flag on the search engine.
    * 
    * @return A document conforming to the format recommended in the interface. 
    */
   public Document execute(PSRequest request) throws PSConsoleCommandException
   {
      Document doc = PSConsoleCommandShowStatusSearch.getDisabledDoc(request,
            COMMAND_NAME);
      if (null != doc)
         return doc;
         
      boolean enabled = false;
      if(m_cmdArgs.equalsIgnoreCase("yes") ||
            m_cmdArgs.equalsIgnoreCase("on") ||
            m_cmdArgs.equalsIgnoreCase("enable") ||
            m_cmdArgs.equalsIgnoreCase("true"))
      {
         enabled = true;
      }
      PSServer.getServerConfiguration().getSearchConfig()
            .setTraceEnabled(enabled);
      String [] args =
      {
         enabled ? "on" : "off", 
      };
      doc = getResultsDocument(request, COMMAND_NAME,
            IPSServerErrors.RCONSOLE_DEBUG_SETTING, args);
      return doc;
   }

   /**
    * The full command that launched this handler. 
    * <p>Note: this should be passed in rather than having it in two places,
    * but the whole framework needs reworking so I'm not going to take that on
    * now. 
    */
   private final static String COMMAND_NAME = "debug search";
}
