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
package com.percussion.server;

import com.percussion.data.PSExecutionData;
import com.percussion.security.PSThreadRequestUtils;

/**
 * This class extends {@link com.percussion.data.PSExecutionData 
 * PSExecutionData} from com.percussion.data package and has a ctor that can 
 * be instantiated with {@link  com.percussion.server.IPSRequestContext}. 
 * This is light in the sense that it is not a full fledged execution data and
 * does not have anything outside of {@link com.percussion.server.PSRequest}. 
 * Note that the ctor calls the super ctor with <code>null</code> for both 
 * application handler and request handler (the first and second parameters).
 * This class be used with care and intended (based on the current 
 * requirements) to execute conditionals that do not depend any data outside
 * of the {@link com.percussion.server.PSRequest} object.  
 * @author RammohanVangapalli
 */
public class PSExecutionDataLight extends PSExecutionData
{
   /**
    * Ctor that takes a {@link IPSRequestContext request context} object.
    */
   public PSExecutionDataLight()
   {
      super(null, null, PSThreadRequestUtils.getPSRequest());
   }
}
