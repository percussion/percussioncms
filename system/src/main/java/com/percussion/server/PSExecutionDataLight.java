/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.server;

import com.percussion.data.PSExecutionData;
import com.percussion.security.PSThreadRequestUtils;

/**
 * This class extends {@link com.percussion.data.PSExecutionData 
 * PSExecutionData} from com.percussion.data package and has a ctor that can 
 * be instantiated with {@link  com.percussion.server.IPSRequestContext}. 
 * This is light in the sense that it is not a fullfledged execution data and 
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
    * @param reqCxt requets context object, must not be <code>null</code>.
    */
   public PSExecutionDataLight()
   {
      super(null, null, PSThreadRequestUtils.getPSRequest());
   }
}
