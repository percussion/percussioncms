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
package com.percussion.webservices.ui;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;

/**
 * Locator to get the ui and ui design webservices.
 */
public class PSUiWsLocator extends PSBaseServiceLocator
{

   private static volatile IPSUiWs uiws = null;
   private static volatile IPSUiDesignWs uidws = null;
   /**
    * Find and return the ui webservice.
    * 
    * @return the ui webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSUiWs getUiWebservice() 
      throws PSMissingBeanConfigurationException
   {
       if (uiws==null)
       {
           synchronized (PSUiWsLocator.class)
           {
               if (uiws==null)
               {
                   uiws = (IPSUiWs) getCtx().getBean("sys_uiWs");
               }
           }
       }
      return uiws;
   }

   /**
    * Find and return the ui design webservice.
    * 
    * @return the ui design webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSUiDesignWs getUiDesignWebservice() 
      throws PSMissingBeanConfigurationException
   {
       if (uidws==null)
       {
           synchronized (PSUiWsLocator.class)
           {
               if (uidws==null)
               {
                   uidws = (IPSUiDesignWs) getCtx().getBean("sys_uiDesignWs");
               }
           }
       }
      return uidws;
   }
}
