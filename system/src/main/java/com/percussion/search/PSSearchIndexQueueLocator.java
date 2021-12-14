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
package com.percussion.search;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;

/**
 * Class description - locates the service for using hibernate to read, write
 * and delete FTS Search Index Queue Items
 * 
 * 
 * @author BillLanglais
 */
public class PSSearchIndexQueueLocator extends PSBaseServiceLocator
{

   private static volatile IPSSearchIndexQueue siq=null;
   /**
    * Get the relationship service (singleton) object.
    * 
    * @return the hibernate object for FTS Search Index Queue Items, never
    * <code>null</code>.
    * 
    * @throws PSMissingBeanConfigurationException if system configuration error
    * occurred during locating the service object.
    */
   public static IPSSearchIndexQueue getPSSearchIndexQueue()
      throws PSMissingBeanConfigurationException
   {
       if (siq==null)
       {
           synchronized (PSSearchIndexQueueLocator.class)
           {
               if (siq==null)
               {
                   siq = (IPSSearchIndexQueue) getBean("sys_searchindexqueue");
               }
           }
       }
      return siq;
   }
}
