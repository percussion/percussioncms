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
package com.percussion.services.legacy;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;

/**
 * Finds and loads the class that implements the {@link IPSCmsContentSummaries}
 * interface.
 * 
 * @author paulhoward
 */
public class PSCmsContentSummariesLocator extends PSBaseServiceLocator
{
   private static volatile IPSCmsContentSummaries ccs=null;
   /**
    * Get summary manager.
    * 
    * @return Summary manager, never <code>null</code>
    * @throws PSMissingBeanConfigurationException If the configuration does not
    * contain the required bean.
    */
   public static IPSCmsContentSummaries getObjectManager()
      throws PSMissingBeanConfigurationException
   {
      if (ccs==null)
      {
         synchronized (PSCmsContentSummariesLocator.class)
         {
            if (ccs==null)
            {
               ccs = (IPSCmsContentSummaries) getBean("sys_cmsObjectMgr");
            }
         }
      }
      return ccs;
   }
}
