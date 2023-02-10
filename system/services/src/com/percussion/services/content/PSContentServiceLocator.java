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
package com.percussion.services.content;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;

/**
 * Locator to get the content service.
 */
public class PSContentServiceLocator extends PSBaseServiceLocator
{
   private static volatile IPSContentService csr=null;
   /**
    * Find and return the content service.
    * 
    * @return the content service, never <code>null</code>.
    * @throws PSMissingBeanConfigurationException if the bean is missing for 
    *    the requested service.
    */
   public static IPSContentService getContentService() 
      throws PSMissingBeanConfigurationException
   {
       if (csr==null)
       {
           synchronized (PSContentServiceLocator.class)
           {
               if(csr==null)
                   csr = (IPSContentService) getCtx().getBean("sys_contentService");
           }
       }
      return csr;
   }
}

