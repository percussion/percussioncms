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
package com.percussion.community;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;

import java.io.File;

/**
 * @author dougrand
 *
 * This exit modifies the user's community based on the parameters passed into
 * the pre-exit. The user's community is modified if:
 * <ul>
 * <li>The bypass flag (param 0) does not have the value "true". This is 
 * set to true for portals or other circumstances when the user community 
 * should not be modified.
 * <li>The passed community is different from the user's current community.
 * <li>The user has the passed community in the list of possible communities.
 * </ul>
 */
public class PSModifyCommunity implements IPSRequestPreProcessor
{
   /**
    * The name of this extension, defined in the {@link #init(IPSExtensionDef, 
    * File) init} method and never modified after.
    */
   private static String ms_fullExtensionName = null;

   /*
    * Implementation of the interface method
    */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
   }

   /* (non-Javadoc)
    * @see com.percussion.extension.IPSRequestPreProcessor#preProcessRequest(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public void preProcessRequest(Object[] params, IPSRequestContext ctx)
      throws PSExtensionProcessingException
   {
      // Grab the params
      try
      {
         if (params.length < 2)
         {
            throw new IllegalArgumentException("Two arguments are required");
         }
         if (params[1] == null)
         {
            throw new 
               IllegalArgumentException("The community id must be specified");
         }
         String flag = params[0] != null ? params[0].toString() : "";
         String communityid = params[1].toString();

         if (flag.equals("true") == false)
         {
            ctx.setParameter(
               IPSHtmlParameters.SYS_OVERRIDE_COMMUNITYID,
               communityid);
            ctx.setParameter(
               IPSHtmlParameters.SYS_FALLBACK_COMMUNITYID,
               "true");
            PSServer.verifyCommunity(ctx);
         }
      }
      catch (Exception e)
      {
         PSConsole.printMsg(ms_fullExtensionName, e);
         throw new PSExtensionProcessingException(ms_fullExtensionName, e);
      }
   }

}
