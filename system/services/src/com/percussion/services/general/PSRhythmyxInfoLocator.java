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
package com.percussion.services.general;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;

/**
 * Rhythmyx Information service locator.
 */
public class PSRhythmyxInfoLocator extends PSBaseServiceLocator
{
   private static volatile IPSRhythmyxInfo rhy=null;
   /**
    * Get the rhythmyx info service, which is used to discover basic information
    * about the configuration.
    * 
    * @see IPSRhythmyxInfo
    * @return the service, never <code>null</code> on a correctly configured
    *         server
    * @throws PSMissingBeanConfigurationException if the spring bean cannot be
    *            found
    */
   public static IPSRhythmyxInfo getRhythmyxInfo()
         throws PSMissingBeanConfigurationException
   {
       if (rhy==null)
       {
           synchronized (PSRhythmyxInfoLocator.class)
           {
               if (rhy==null)
               {
                   rhy = (IPSRhythmyxInfo) getBean("sys_rhythmyxinfo");
               }
           }
       }
      return rhy;
   }
}
