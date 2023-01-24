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

package com.percussion.cx;

public class JSClipDataItem {

    public String kind = "text";
    public String type;
    public String data;

    public JSClipDataItem(String type, String data)
    {
        this.type = type;
        this.data = data;
    }

    public String getKind()
    {
        return "text";
    }

    public String getAsString()
    {
        return this.data;
    }

    public String getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "ClipDataItem{" +
                "kind='" + kind + '\'' +
                ", type='" + type + '\'' +
                ", data='" + data + '\'' +
                '}';
    }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      JSClipDataItem other = (JSClipDataItem) obj;
      if (type == null)
      {
         if (other.type != null)
            return false;
      }
      else if (!type.equals(other.type))
         return false;
      return true;
   }

    
    
}
