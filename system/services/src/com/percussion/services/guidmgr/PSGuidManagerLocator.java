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
package com.percussion.services.guidmgr;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Find the guid manager service
 * 
 * @author dougrand
 */
@ThreadSafe
public class PSGuidManagerLocator extends PSBaseServiceLocator
{
   private static  IPSGuidManager gmgr=null;
   /**
    * Locator for guid manager
    * @return instance of guid manager
    * @throws PSMissingBeanConfigurationException
    */
   public static synchronized IPSGuidManager getGuidMgr() throws PSMissingBeanConfigurationException
   {
             if (gmgr==null)
             {
                 gmgr = (IPSGuidManager) getBean("sys_guidmanager");
             }

      return gmgr;
   }
}
