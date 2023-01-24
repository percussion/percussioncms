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
