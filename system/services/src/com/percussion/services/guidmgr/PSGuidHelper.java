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

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;


/**
 * A helper class to aid in constructing guids
 * 
 * @author dougrand
 */
public class PSGuidHelper
{   
   /**
    * Generate just the UUID part of a guid. Useful for initializing 
    * child table ids
    * @param e the enumeration to use
    * @return just the UUID portion
    */
   public static long generateNextLong(PSTypeEnum e)
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      return mgr.createGuid(e).getUUID();
   }
   
   /**
    * Generate just the UUID part of a guid. Useful for initializing 
    * child table ids
    * @param e the enumeration to use
    * @return the new GUID, never <code>null</code>
    */
   public static IPSGuid generateNext(PSTypeEnum e)
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      return mgr.createGuid(e);
   }   
}
