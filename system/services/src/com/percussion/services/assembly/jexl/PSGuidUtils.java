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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
