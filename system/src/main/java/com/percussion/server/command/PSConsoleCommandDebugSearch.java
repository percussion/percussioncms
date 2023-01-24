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
