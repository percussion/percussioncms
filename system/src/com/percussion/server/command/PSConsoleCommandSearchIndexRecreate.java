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

import com.percussion.cms.objectstore.PSKey;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.search.PSAdminLockedException;
import com.percussion.search.PSSearchAdmin;
import com.percussion.search.PSSearchEngine;
import com.percussion.search.PSSearchException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;

/**
 * Implements the 'search index recreate' console command.
 *
 * @author paulhoward
 */
public class PSConsoleCommandSearchIndexRecreate
   extends PSConsoleCommandSearchIndex
{
   /**
    * Ctor required by framework.
    * 
    * @param args See class description for reqs.
    * 
    * @throws PSIllegalArgumentException Never. The optional arg is not
    * validated until the command is executed.
    * <p>Note: we use PSIllegal... here because that's what the framework uses.
    */
   public PSConsoleCommandSearchIndexRecreate(String cmdArgs)
      throws PSIllegalArgumentException
   {
      super(cmdArgs);
   }
   
   //see base class   
   public PSKey[] doExecute(PSRequest request, PSKey[] ctypeIds) 
      throws PSSearchException
   {
      PSSearchAdmin sa = null;
      PSSearchEngine eng = PSSearchEngine.getInstance();
      try
      {
         sa = eng.getSearchAdmin(true);
         return sa.rebuildIndexes(ctypeIds);
      }
      catch (PSAdminLockedException e)
      {
         throw new PSSearchException(e.getErrorCode(), e.getErrorArguments());
      }
      catch (IllegalStateException ise)
      {
         throw new PSSearchException(IPSServerErrors.RCONSOLE_COMMAND_CANT_RUN, 
               ise.getLocalizedMessage());
      }
      finally
      {
         if (null != sa)
            eng.releaseSearchAdmin(sa);    
      }
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
   private final static String COMMAND_NAME = "search index recreate";
}
