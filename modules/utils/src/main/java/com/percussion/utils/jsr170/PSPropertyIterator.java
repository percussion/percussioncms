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
package com.percussion.utils.jsr170;


import com.percussion.utils.beans.PSPropertyWrapper;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import java.util.Map;

/**
 * This is an implementation of the JSR-170 property iterator
 * 
 * @author dougrand
 */
public class PSPropertyIterator extends PSItemIterator<Property>
   implements PropertyIterator
{
   /**
    * Ctor
    * @param things the map of properties, never <code>null</code>
    * @param filterpattern the filter pattern, may be <code>null</code>
    */
   public PSPropertyIterator(Map<String, Property> things, String filterpattern) {
      super(things, filterpattern);
   }

   public Property nextProperty()
   {
      Property p = next();
      
      if (p != null && p instanceof PSPropertyWrapper)
      {
         PSPropertyWrapper wrapper = (PSPropertyWrapper) p;
         try
         {
            wrapper.init();
         }
         catch(IllegalStateException e)
         {
            return nextProperty();
         }
      }
      
      return p;
   }
}
