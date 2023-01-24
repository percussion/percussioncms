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
package com.percussion.webservices.transformation.converter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.beanutils.BeanUtilsBean;

public class PSArrayToSetConverter extends PSArrayConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSArrayToSetConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see Converter#convert(Class, Object)
    */
   @SuppressWarnings("unchecked")
   public Object convert(Class listType, Object value)
   {
      if (value == null)
         return null;
      
      if (listType == null)
         throw new IllegalArgumentException("listType cannot be null");
      
      if (!value.getClass().isArray())
         throw new IllegalArgumentException("value must be of type array");
      
      Class arrayType = Array.newInstance(Object.class, 0).getClass();
      Object[] convertedArray = (Object[]) super.convert(arrayType, value);
      return new HashSet(Arrays.asList(convertedArray));
   }
}

