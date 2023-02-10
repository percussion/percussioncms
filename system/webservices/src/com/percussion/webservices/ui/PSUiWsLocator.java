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
package com.percussion.webservices.ui;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;

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
