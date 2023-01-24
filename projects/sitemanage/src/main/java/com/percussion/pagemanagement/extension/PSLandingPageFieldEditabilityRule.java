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
package com.percussion.pagemanagement.extension;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldEditabilityRule;
import com.percussion.extension.PSExtensionException;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.fastforward.managednav.PSManagedNavServiceLocator;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;

import java.io.File;

import org.apache.commons.lang.StringUtils;

/**
 * This rule will check to see if the page is a landing page and if so, the field this rule
 * applies to will become read-only.
 * 
 * <pre>
 * Takes 2 required params:
 * 
 * param[0] = content_id (i.e. the pageId)
 * param[1] = revision
 * </pre>
 *
 */
public class PSLandingPageFieldEditabilityRule implements IPSFieldEditabilityRule
{

   /* (non-Javadoc)
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[],
    *  com.percussion.server.IPSRequestContext)
    */
   @SuppressWarnings("unused")
   public Object processUdf(Object[] params, IPSRequestContext req)
            throws PSConversionException
   {
      if(m_navService == null) {
         m_navService = PSManagedNavServiceLocator.getContentWebservice();
      }
      String pageId = (String)params[0];
      String revision = (String)params[1];
      
      if(StringUtils.isBlank(pageId) || StringUtils.isBlank(revision)) {
         return Boolean.TRUE;
      }
      IPSGuid pageGuid = new PSLegacyGuid(
         Integer.parseInt(pageId), Integer.parseInt(revision));
         
      return new Boolean(m_navService.isLandingPage(pageGuid));
   }

   /* (non-Javadoc)
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
    * java.io.File)
    */
   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File file)
            throws PSExtensionException
   {
      //No-op
      
   }
   
   /**
    * Managed Nav service. Initialized the first time {@link #processUdf(Object[], IPSRequestContext)}
    * is called. Never <code>null</code> after that.
    */
   private IPSManagedNavService m_navService;
   
   
}
