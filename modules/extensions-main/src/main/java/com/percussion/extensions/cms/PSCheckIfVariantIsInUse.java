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
package com.percussion.extensions.cms;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSRelationshipProcessor;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
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
            StringBuffer ownerids = new StringBuffer();
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
