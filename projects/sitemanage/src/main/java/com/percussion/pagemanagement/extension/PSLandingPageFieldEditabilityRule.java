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
