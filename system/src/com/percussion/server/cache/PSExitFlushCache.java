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
package com.percussion.server.cache;

import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;

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
            throw new RuntimeException(e.getLocalizedMessage());
         }
      }
   }
}
