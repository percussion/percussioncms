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
               e.getMessage());
      }
      return new PSContentTypeVariantSet(templates);
   }
}



