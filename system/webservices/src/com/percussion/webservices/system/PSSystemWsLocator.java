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
package com.percussion.webservices.system;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;

/**
 * Locator to get the system and system design webservices.
 */
public class PSSystemWsLocator extends PSBaseServiceLocator
{
   private static volatile IPSSystemWs sws=null;
   private static volatile IPSSystemDesignWs sdws=null;
   /**
    * Find and return the system webservice.
    * 
    * @return the system webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSSystemWs getSystemWebservice() 
      throws PSMissingBeanConfigurationException
   {
       if (sws==null)
       {
           synchronized (PSSystemWsLocator.class)
           {
               if (sws==null)
               {
                   sws = (IPSSystemWs) getCtx().getBean("sys_systemWs");
               }
           }
       }
      return sws;
   }

   /**
    * Find and return the system design webservice.
    * 
    * @return the system design webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSSystemDesignWs getSystemDesignWebservice() 
      throws PSMissingBeanConfigurationException
   {
       if (sdws==null)
       {
           synchronized (PSSystemWsLocator.class)
           {
               if (sdws==null)
               {
                   sdws = (IPSSystemDesignWs) getCtx().getBean("sys_systemDesignWs");
               }
           }
       }
      return sdws;
   }
}

