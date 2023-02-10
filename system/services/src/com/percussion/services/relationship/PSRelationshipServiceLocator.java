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
package com.percussion.services.relationship;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;

/**
 * The relationship service locator.
 */
public class PSRelationshipServiceLocator extends PSBaseServiceLocator
{
   private static volatile IPSRelationshipService relsvc = null;
   /**
    * Get the relationship service (singleton) object.
    * 
    * @return the relationship service object, never <code>null</code>.
    * 
    * @throws PSMissingBeanConfigurationException if system configuration error
    *   occurred during locating the service object.
    */
   public static IPSRelationshipService getRelationshipService()
         throws PSMissingBeanConfigurationException
   {
      if (relsvc==null)
      {
         synchronized (PSRelationshipServiceLocator.class)
         {
            if (relsvc==null)
            {
               relsvc = (IPSRelationshipService) getBean("sys_relationshipService");
            }
         }
      }
      return relsvc;
   }
}
