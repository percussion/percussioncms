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
package com.percussion.services.guidmgr;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;

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
