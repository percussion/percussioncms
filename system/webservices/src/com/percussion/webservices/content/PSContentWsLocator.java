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
package com.percussion.webservices.content;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;

/**
 * Locator to get the content and content design webservices.
 */
public class PSContentWsLocator extends PSBaseServiceLocator
{
   
   private static volatile IPSContentWs cws = null;
   private static volatile IPSContentDesignWs cdws = null;
   
   /**
    * Find and return the content webservice.
    * 
    * @return the content webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSContentWs getContentWebservice() 
      throws PSMissingBeanConfigurationException
   {
      if (cws==null)
      {
         synchronized (PSContentWsLocator.class)
         {
            if (cws==null)
            {
               cws = (IPSContentWs) getCtx().getBean("sys_contentWs");
            }
         }
      }
      return cws;
   }

   /**
    * Find and return the content design webservice.
    * 
    * @return the content design webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSContentDesignWs getContentDesignWebservice() 
      throws PSMissingBeanConfigurationException
   {
      if (cdws==null)
      {
         synchronized (PSContentWsLocator.class)
         {
            if (cdws==null)
            {
               cdws=(IPSContentDesignWs) getCtx().getBean("sys_contentDesignWs");
            }
         }
      }
      return cdws;
   }
}

