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
