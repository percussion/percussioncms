/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.services.sitemgr;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;
import com.percussion.services.pubserver.IPSPubServerDao;

/**
 * Get the site manager from the Spring configuration.
 * 
 * @author dougrand
 */
public class PSSiteManagerLocator extends PSBaseServiceLocator
{
   private static volatile IPSSiteManager smgr=null;
   private static volatile IPSPubServerDao psrd=null;
   /**
    * Get the site manager
    * @return the site manager, never <code>null</code>
    * @throws PSMissingBeanConfigurationException if the bean doesn't exist
    */
   public static IPSSiteManager getSiteManager()
      throws PSMissingBeanConfigurationException
   {
       if (smgr==null)
       {
           synchronized (PSSiteManagerLocator.class)
           {
               if (smgr==null)
               {
                   smgr = (IPSSiteManager) getBean("sys_sitemanager");
               }
           }
       }
      return smgr;
   }
   
   public static IPSPubServerDao getPubServerDao() throws PSMissingBeanConfigurationException
   {
       if (psrd==null)
       {
           synchronized (PSSiteManagerLocator.class)
           {
               if (psrd==null)
               {
                   psrd = (IPSPubServerDao) getBean("sys_pubserverdao");
               }
           }
       }
      return psrd;
   }
}
