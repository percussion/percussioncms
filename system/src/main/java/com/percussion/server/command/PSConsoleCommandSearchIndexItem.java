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
