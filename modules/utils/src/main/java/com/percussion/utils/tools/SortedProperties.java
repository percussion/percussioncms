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

package com.percussion.utils.tools;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class SortedProperties extends Properties {
   private static final long serialVersionUID = 1L;

   public Enumeration<Object> keys() {
       Enumeration<Object> keysEnum = super.keys();
       Vector<Object> keyList = new Vector<Object>();

       while (keysEnum.hasMoreElements()) {
           keyList.add(keysEnum.nextElement());
       }

       Collections.sort(keyList, new Comparator<Object>() {
           @Override
           public int compare(Object o1, Object o2) {
               return o1.toString().compareTo(o2.toString());
           }
       });

       return keyList.elements();
   }
}
