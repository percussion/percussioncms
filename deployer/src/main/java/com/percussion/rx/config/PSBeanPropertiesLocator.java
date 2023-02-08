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
package com.percussion.rx.config;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;

/**
 * This class provides a way to locate the instance of {@link IPSBeanProperties}.
 *
 * @author YuBingChen
 */
public class PSBeanPropertiesLocator extends PSBaseServiceLocator
{
    private static volatile IPSBeanProperties bpr=null;
   /**
    * Return the instance of {@link IPSBeanProperties}.
    * 
    * @return the instance of {@link IPSBeanProperties}, never <code>null</code>
    * 
    * @throws PSMissingBeanConfigurationException if bean is missing.
    */
   public static IPSBeanProperties getBeanProperties()
         throws PSMissingBeanConfigurationException
   {
       if (bpr==null)
       {
           synchronized (PSBeanPropertiesLocator.class)
           {
               if (bpr==null)
               {
                   bpr = (IPSBeanProperties) getBean("sys_beanProperties");
               }
           }
       }
      return bpr;
   }

}
