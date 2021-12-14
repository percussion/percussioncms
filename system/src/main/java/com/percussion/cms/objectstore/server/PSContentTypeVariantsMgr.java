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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSContentTypeVariantSet;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;

import java.util.Set;

/**
 * This is a utility class, used to obtain content type variant set from the 
 * server
 */
public class PSContentTypeVariantsMgr
{
   /**
    * Get the set of all content type variants in the system. This is used in 
    * server only and shares the same resource cache.
    * 
    * @param request The request object. It must be {@link IPSRequestContext} or
    *    {@link PSRequest} object, this argument is ignored
    * 
    * @return Content type variant set, never <code>null</code>, may be
    *        empty.
    * 
    * @throws PSCmsException if it cannot get the set from server for any
    *           reason.
    */
   public static PSContentTypeVariantSet getAllContentTypeVariants(
         @SuppressWarnings("unused") Object request) throws PSCmsException
   {
      IPSAssemblyService assembly = PSAssemblyServiceLocator.getAssemblyService();
      Set<IPSAssemblyTemplate> templates;
      try
      {
         templates = assembly.findAllTemplates();
      }
      catch (PSAssemblyException e)
      {
         throw new PSCmsException(IPSServerErrors.UNEXPECTED_EXCEPTION_CONSOLE,
               e.getLocalizedMessage());
      }
      return new PSContentTypeVariantSet(templates);
   }
}



