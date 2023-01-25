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
package com.percussion.services.ui;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;

/**
 * Locator to get the ui service.
 */
public class PSUiServiceLocator extends PSBaseServiceLocator
{
   private static volatile IPSUiService usr=null;
   /**
    * Find and return the ui service.
    * 
    * @return the ui service, never <code>null</code>.
    * @throws PSMissingBeanConfigurationException if the requested service 
    *    bean is missing.
    */
   public static IPSUiService getUiService() 
      throws PSMissingBeanConfigurationException
   {
       if (usr==null)
       {
           synchronized (PSUiServiceLocator.class)
           {
               if (usr==null)
               {
                   usr = (IPSUiService) getCtx().getBean("sys_uiService");
               }
           }
       }
      return usr;
   }
}
