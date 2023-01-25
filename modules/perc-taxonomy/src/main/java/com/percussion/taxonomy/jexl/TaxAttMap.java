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

import com.percussion.taxonomy.domain.Attribute_lang;

import java.util.HashMap;
import java.util.List;

/**
 * This class Wraps a HashMap to provide extra functions to the Jexl author
 * 
 * @author stephenbolton
 * 
 */
public class TaxAttMap extends HashMap<String, TaxAttribute>
{
   public TaxAttMap()
   {
      super();
   }

   /**
    * Generate the hash map from the Hibernate object
    * @param attLangs
    */
   public TaxAttMap(List<Attribute_lang> attLangs)
   {
      super();

      for (Attribute_lang attLang : attLangs)
      {
         TaxAttribute att = new TaxAttribute(attLang);
         this.put(attLang.getName(), att);
      }
   }
}
