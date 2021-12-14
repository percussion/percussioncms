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
package com.percussion.share.data;

import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple wrapper around a map class to allow it to be serialized by CXF.
 * @author erikserating
 *
 */

@JsonRootName(value = "psmap")

public class PSMapWrapper{
   
      
   public Map<String, String> getEntries()
   {
      return entries;
   }
   
   public void setEntries(Map<String, String> map)
   {
      this.entries = map;
   }
   
   
    @Override
    public int hashCode()
    {
        return entries.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean isEqual = false;
        if (obj instanceof PSMapWrapper)
        {
            isEqual = entries.equals(((PSMapWrapper)obj).getEntries());
        }
        
        return isEqual;
    }
    

private Map<String, String> entries = new HashMap<>();
   
   private static final long serialVersionUID = 8252999104256582955L;
}
