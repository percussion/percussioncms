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
package com.percussion.services.general;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;

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
