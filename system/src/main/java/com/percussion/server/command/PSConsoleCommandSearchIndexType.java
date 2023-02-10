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
import com.percussion.search.PSSearchException;
import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Implements the 'search index type' console command. This command takes
 * 1 optional argument which is a content type identifier, either numeric
 * or text. If no arg is supplied, all running types are processed.
 *
 * @author paulhoward
 */
public class PSConsoleCommandSearchIndexType
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
   public PSConsoleCommandSearchIndexType(String cmdArgs)
      throws PSIllegalArgumentException
   {
      super(cmdArgs);
   }
   
   //see base class   
   @Override
   public PSKey[] doExecute(@SuppressWarnings("unused") PSRequest request,
         PSKey[] ctypeIds) throws PSSearchException
   {
      int tot = ctypeIds.length;
      if (tot > 1)
      {
         ms_log.info("Rebuilding Indexes for " + tot + " Content Types.");
      }
      PSKey[] tmpResults = new PSKey[tot];
      int cTypeCount = 0;
      boolean hadBadType = false;      
      for (int i = 0; i < tot; i++)
      {
         int result = PSSearchIndexEventQueue.getInstance().indexContentType(
            ctypeIds[i].getPartAsInt());
         if (result == -1)
         {
            hadBadType = true;
            tmpResults[cTypeCount++] = ctypeIds[i];            
         }
         else
            m_indexCount += result;
      }
      
      PSKey[] results;
      
      if (hadBadType)
         m_resultCode = IPSServerErrors.RCONSOLE_ITEMS_INDEXED_INVALID_CTYPES;
      else
         m_resultCode = IPSServerErrors.RCONSOLE_ITEMS_INDEXED;
      
      if (cTypeCount < tot)
      {
         results = new PSKey[cTypeCount];
         System.arraycopy(tmpResults, 0, results, 0, cTypeCount);
      }
      else
      {
         results = ctypeIds;
      }

      return results;
   }
   
   // see base class
   @Override
   protected String getCommandName()
   {
      return COMMAND_NAME;
   }
   
   // see base class
   @Override
   protected int getResultCode()
   {
      return m_resultCode;
   }

   // see base class
   @Override
   protected String[] getResultArgs(PSKey[] successes)
   {
      if (null == successes)
      {
         throw new IllegalArgumentException("succcesses cannot be null");
      }
      
      String[] args;
      boolean hadBadType = 
         m_resultCode == IPSServerErrors.RCONSOLE_ITEMS_INDEXED_INVALID_CTYPES;
         
      args =  hadBadType ? new String[2] : new String[1];      
      args[0] = String.valueOf(m_indexCount);      
      if (hadBadType)
         args[1] = super.getResultArgs(successes)[0];
      
      return args;
   }
   
   /**
    * The full command that launched this handler. 
    * <p>Note: this should be passed in rather than having it in two places,
    * but the whole framework needs reworking so I'm not going to take that on
    * now. 
    */
   private final static String COMMAND_NAME = "search index type";
   
   /**
    * Result of the last call to {@link #doExecute(PSRequest, PSKey[])}, will
    * be returned by calls to {@link #getResultCode()}.
    */
   private int m_resultCode = IPSServerErrors.RCONSOLE_CONTENT_TYPES_PROCESSED;
   
   /**
    * Number of items indexed by last call to 
    * {@link #doExecute(PSRequest, PSKey[])}.
    */
   private int m_indexCount = 0; 
   private static final Logger ms_log = 
      LogManager.getLogger(PSConsoleCommandSearchIndexType.class);
}
