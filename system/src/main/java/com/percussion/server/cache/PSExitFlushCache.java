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
package com.percussion.server.cache;

import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.util.PSCacheException;

/**
 * Exit to expose flush all caches, Assembler Cache and Folder caches, 
 * capabilities to application resources. This only occurs on a Publishing Hub;
 * otherwise, do nothing.
 */
public class PSExitFlushCache extends PSDefaultExtension
   implements IPSRequestPreProcessor
{
   /**
    * Flushes all caches, Assembler, Resource and Folder Caches, if the 
    * current server is a Publishing Hub. Do nothing if the server is used
    * as a Content Hub.
    *
    * @param params
    *           The parameter for this exit, which is not used. It may be 
    *           <code>null</code>.
    * @param request
    *           The request context; it may be <code>null</code>.
    *
    * @see PSExitFlushAssemblerCache
    *
    * @throws IllegalArgumentException
    *            if request is <code>null</code>
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSParameterMismatchException
   {
      PSServerConfiguration srvConfig = PSServer.getServerConfiguration();
      if (srvConfig.getServerType()
            == PSServerConfiguration.SERVER_TYPE_PUBLISHING_HUB)
      {
         try
         {
            PSCacheProxy.flushAll();
         }
         catch (PSCacheException e)
         {
            throw new RuntimeException(e);
         }
      }
   }
}
