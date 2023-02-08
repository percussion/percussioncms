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
