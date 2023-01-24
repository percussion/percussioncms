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
package com.percussion.extensions.cms;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSRelationshipProcessor;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.IPSHtmlParameters;

import java.io.File;
import java.util.Iterator;

/**
 * This exit checks to see if a given variant is in use in the system. At this
 * time this check looks to see if the relationships includes any sys_variantid
 * properties that match the given variant.
 * <p>
 * The check uses the relationship processor to query the database for matching
 * relationships. If any are found, a list of the owner ids is returned in the
 * error message to enable the user to remove (if desired) slot content in the
 * given items.
 * 
 * @author dougrand
 */
public class PSCheckIfVariantIsInUse implements IPSRequestPreProcessor
{

   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      try
      {
         String variantid = (String) params[0];
         IPSRelationshipProcessor rproc = PSRelationshipProcessor.getInstance();
         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setProperty(IPSHtmlParameters.SYS_VARIANTID, variantid);
         PSRelationshipSet results = rproc.getRelationships(filter);
         if (results.size() > 0)
         { 
            StringBuilder ownerids = new StringBuilder();
            Iterator iter = results.iterator();
            int count = 0;
            while(iter.hasNext())
            {
               count++;
               PSRelationship rel = (PSRelationship) iter.next();
               ownerids.append(Integer.toString(rel.getOwner().getId()));
               if (count > 99) 
               {
                  ownerids.append("...");
                  break;
               }
               else
               {
                  if (iter.hasNext()) ownerids.append(", ");
               }
            }
            Object errorargs[] = new Object[] {
               variantid, ownerids
            };
            throw new PSExtensionProcessingException(
                  IPSExtensionErrors.VARIANT_HAS_RELATIONSHIPS_ERROR, errorargs);
         }
      }
      catch (PSCmsException e)
      {
         throw new PSExtensionProcessingException("Fatal error", e);
      }
   }

   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // Unused, no parameters
   }

}
