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
package com.percussion.taxonomy.jexl;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

public class TaxValues extends ArrayList<String>
{
   /**
    * An object that can handle both single and multiple taxonomy attribute
    * values It can be treated like a regular List for multiple values or
    * toString will output a comma separated list for multiple values.
    */
   private static final long serialVersionUID = 1L;

   public TaxValues()
   {
      super();
   }

   public TaxValues(String string)
   {
      super();
      this.add(string);
   }

   public boolean isMultiValued()
   {
      return this.size() > 1;
   }

   public String join(String delim)
   {
      return StringUtils.join(this, delim);
   }

   @Override
   public String toString()
   {
      return this.join(",");
   }

}
