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
package com.percussion.services.content;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;

/**
 * Locator to get the content service.
 */
public class PSContentServiceLocator extends PSBaseServiceLocator
{
   private static volatile IPSContentService csr=null;
   /**
    * Find and return the content service.
    * 
    * @return the content service, never <code>null</code>.
    * @throws PSMissingBeanConfigurationException if the bean is missing for 
    *    the requested service.
    */
   public static IPSContentService getContentService() 
      throws PSMissingBeanConfigurationException
   {
       if (csr==null)
       {
           synchronized (PSContentServiceLocator.class)
           {
               if(csr==null)
                   csr = (IPSContentService) getCtx().getBean("sys_contentService");
           }
       }
      return csr;
   }
}

