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
package com.percussion.fastforward.managednav;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.cache.PSCacheProxy;

/**
 * This effect flushes the item from the cache when it is added to or removed
 * from a folder. If the user moves a Landing Page, then this effect is not
 * adequate; a Nav Reset is required. However for pages which are not listed in
 * the Nav, this effect will force them to appear in the correct navigation once
 * they are moved.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavFolderCacheFlushEffect extends PSNavAbstractEffect
      implements
         IPSEffect
{

   /**
    * Attempt to process the effect. Items which are added or removed from
    * folders will cause this effect to fire. This method only processes new or
    * deleted relationships. All other changes are ignored.
    * 
    * @see com.percussion.relationship.IPSEffect#attempt(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext,
    *      com.percussion.relationship.IPSExecutionContext,
    *      com.percussion.relationship.PSEffectResult)
    */
   public void attempt(Object[] params, IPSRequestContext req,
         IPSExecutionContext excontext, PSEffectResult result)
         throws PSExtensionProcessingException, PSParameterMismatchException
   {
      try
      {
         if (excontext.isPreConstruction() || excontext.isPreDestruction())
         {
            PSRelationship rel = excontext.getCurrentRelationship();
            if (rel != null)
            {
               PSLocator depLoc = rel.getDependent();
               PSComponentSummary summary = PSNavUtil.getItemSummary(req,
                     depLoc);
               if (summary.isItem())
               {
                  PSCacheProxy.flushAssemblers(null,
                        new Integer(depLoc.getId()), null, null);
               }
            }
         }
         result.setSuccess();
      }
      catch (Exception e)
      {
         m_log.error(getClass().getName(), e);
         throw new PSExtensionProcessingException(0, e);
      }
   }
}