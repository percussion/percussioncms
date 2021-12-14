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
package com.percussion.webservices.assembly;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;

/**
 * Locator to get the assembly and assembly design webservices.
 */
public class PSAssemblyWsLocator extends PSBaseServiceLocator
{
   private static volatile IPSAssemblyWs aws=null;
   private static volatile IPSAssemblyDesignWs adws=null;
   /**
    * Find and return the assembly webservice.
    * 
    * @return the assembly webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSAssemblyWs getAssemblyWebservice() 
      throws PSMissingBeanConfigurationException
   {
       if (aws==null)
       {
           synchronized (PSAssemblyWsLocator.class)
           {
               if (aws==null)
               {
                   aws = (IPSAssemblyWs) getCtx().getBean("sys_assemblyWs");
               }
           }
       }
      return aws;
   }

   /**
    * Find and return the assembly design webservice.
    * 
    * @return the assembly design webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSAssemblyDesignWs getAssemblyDesignWebservice() 
      throws PSMissingBeanConfigurationException
   {
       if (adws==null)
       {
           synchronized (PSAssemblyWsLocator.class)
           {
               if (adws==null)
               {
                   adws = (IPSAssemblyDesignWs) getCtx().getBean("sys_assemblyDesignWs");
               }
           }
       }
      return adws;
   }
}

