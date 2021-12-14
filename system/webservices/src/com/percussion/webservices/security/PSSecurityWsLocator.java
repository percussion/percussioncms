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
package com.percussion.webservices.security;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;

/**
 * Locator to get the security and security design webservices.
 */
public class PSSecurityWsLocator extends PSBaseServiceLocator
{
   private static volatile IPSSecurityWs sws=null;
   private static volatile IPSSecurityDesignWs sdws=null;
   /**
    * Find and return the security webservice.
    * 
    * @return the security webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSSecurityWs getSecurityWebservice() 
      throws PSMissingBeanConfigurationException
   {
      if (sws==null)
      {
         synchronized (PSSecurityWsLocator.class)
         {
            if (sws==null)
            {
                return (IPSSecurityWs) getCtx().getBean("sys_securityWs");
            }
         }
      }
      return sws;
   }

   /**
    * Find and return the security design webservice.
    * 
    * @return the security design webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSSecurityDesignWs getSecurityDesignWebservice() 
      throws PSMissingBeanConfigurationException
   {
       if (sdws==null)
       {
           synchronized (PSSecurityWsLocator.class)
           {
               if (sdws==null)
               {
                   sdws = (IPSSecurityDesignWs) getCtx().getBean("sys_securityDesignWs");
               }
           }
       }
       return sdws;
   }
}

