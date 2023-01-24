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
package com.percussion.services.assembly.jexl;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;

/**
 * Utility functions for use with guid data 
 * 
 * @author dougrand
 */
public class PSGuidUtils extends PSJexlUtilBase
{
   /**
    * Extract the content id if this is a legacy guid
    * @param guid the guid, may be <code>null</code>
    * @return the content id value or <code>0</code> for an unrecognized type 
    * or a <code>null</code> guid
    */
   @IPSJexlMethod(description = "Extract the content id if this is a legacy guid",
         params = {@IPSJexlParam(name = "guid", description = "The guid, may be null")},
         returns = "the content id value or 0 for an unrecognized type or a null guid"
    )
   public int getContentId(IPSGuid guid)
   {
      if (guid == null)
      {
         return 0;
      }
      if (guid instanceof PSLegacyGuid)
      {
         PSLegacyGuid lguid = (PSLegacyGuid) guid;
         return lguid.getContentId();
      }
      else
      {
         return 0;
      }
   }
}
