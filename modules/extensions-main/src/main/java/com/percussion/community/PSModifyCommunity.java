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
