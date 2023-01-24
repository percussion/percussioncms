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

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import org.w3c.dom.Document;

/**
 * Implements the 'search index item' console command.
 *
 * @author paulhoward
 */
public class PSConsoleCommandSearchIndexItem
   extends PSConsoleCommand
{
   /**
    * Ctor required by framework.
    * 
    * @param args See class description for reqs.
    * 
    * @throws PSIllegalArgumentException If a numeric arg is not supplied.
    * <p>Note: we use PSIllegal... here because that's what the framework uses.
    */
   public PSConsoleCommandSearchIndexItem(String cmdArgs)
      throws PSIllegalArgumentException
   {
      super(cmdArgs);
      if (null == cmdArgs)
         cmdArgs = "";
      m_id = -1;
      try
      {
          m_id = Integer.parseInt(cmdArgs);
      }
      catch (NumberFormatException e)
      {
         //ignore, later check deals w/ this error
      }
      if (m_id < 0) 
      {
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_REQUIRES_CONTENTID, null);
      }
   }
   
   //see base class   
   public Document execute(PSRequest request) 
      throws PSConsoleCommandException
   {
      Document doc;
      doc = PSConsoleCommandShowStatusSearch.getDisabledDoc(request, 
         getCommandName());
      if (null != doc)
         return doc;

      int code;
      String[] resultArgs = null;
      
      PSLocator locator = new PSLocator(m_id);
      int result = PSSearchIndexEventQueue.getInstance().indexItem(locator);
      if (result == -1)
      {
         code = IPSServerErrors.RCONSOLE_ITEMS_INDEXED_INVALID_CTYPES;
         resultArgs = new String[2];
         resultArgs[0] = "0";
         PSItemDefManager mgr = PSItemDefManager.getInstance();
         long type = mgr.getItemContentType(locator);
         try
         {
            resultArgs[1] = mgr.contentTypeIdToName(type);
         }
         catch (PSInvalidContentTypeException e)
         {
            resultArgs[1] = type + " (invalid)";
         }
      }
      else
      {
         code = IPSServerErrors.RCONSOLE_ITEMS_INDEXED;
         resultArgs = new String[1];
         resultArgs[0] = String.valueOf(result);
      }
      
      doc = getResultsDocument(request, getCommandName(), code, 
            resultArgs);
      return doc;
   }
   
   // see base class
   protected String getCommandName()
   {
      return COMMAND_NAME;
   }
   
   /**
    * The full command that launched this handler. 
    * <p>Note: this should be passed in rather than having it in two places,
    * but the whole framework needs reworking so I'm not going to take that on
    * now. 
    */
   private final static String COMMAND_NAME = "search index item";
   
   /**
    * The content id passed to the ctor. Always a number > 0 after construction.
    */
   private int m_id;
}
