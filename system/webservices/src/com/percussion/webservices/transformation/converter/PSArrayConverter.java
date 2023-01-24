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

import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;

/**
 * Converts inputs of type Array to outputs of type Array for the type 
 * mappings registered.
 */
public class PSArrayConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSArrayConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see Converter#convert(Class, Object)
    */
   public Object convert(Class arrayType, Object value)
   {
      if (value == null)
         return null;
      
      if (arrayType == null)
         throw new IllegalArgumentException("arrayType cannot be null");
      
      if (!value.getClass().isArray())
         throw new IllegalArgumentException("value must be of type array");
      
      try
      {
         Object[] valueArray = (Object[]) value;
         if (valueArray.length == 0 || valueArray[0] == null)
            return Array.newInstance(arrayType.getComponentType(), 0);
         
         Class inputElementType = valueArray[0].getClass();
         Converter converter = getBeanUtils().getConvertUtils().lookup(
            inputElementType);
   
         Class outputElementType = arrayType.getComponentType();
         // We always get Object.class when this is called by 
         // PSArrayToListConverter(...), then we have to use the map to get
         // the correct type.
         if (outputElementType == Object.class)
            outputElementType = getOutputElementType(inputElementType);
         if (outputElementType == null)
            throw new ConversionException(
               "No output element type mapping found for input element type " + 
               inputElementType.getName());
            
         Object[] convertedArray = (Object[]) Array.newInstance(
            outputElementType, valueArray.length);
         for (int i=0; i<valueArray.length; i++)
         {
            if (converter == null)
            {
               Object convertedValue = outputElementType.newInstance();
               getBeanUtils().copyProperties(convertedValue, valueArray[i]);
               convertedArray[i] = convertedValue;
            }
            else
               convertedArray[i] = converter.convert(outputElementType, 
                  valueArray[i]);
         }
         
         return convertedArray;
      }
      catch (InstantiationException e)
      {
         throw new ConversionException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new ConversionException(e);
      }
      catch (InvocationTargetException e)
      {
         throw new ConversionException(e);
      }
   }
   
   /**
    * Get the type of the array output element type for the supplied array 
    * input element type.
    * 
    * @param inputElementType the type of the array input elements, assumed
    *    not <code>null</code>.
    * @return the type of the array outout elements, never <code>null</code>.
    * @throws ConversionException if we could not find a mapping for the 
    *    supplied array input element type.
    */
   public static Class getOutputElementType(Class inputElementType) 
      throws ConversionException
   {
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      Map<Class, Class> typeMapper = factory.getTypeMapper();
      
      Class outputElementType = typeMapper.get(inputElementType);
      if (outputElementType == null)
      {
         // try the reverse mapping
         Iterator keys = typeMapper.keySet().iterator();
         while (keys.hasNext() && outputElementType == null)
         {
            Class key = (Class) keys.next();
            Class value = (Class) typeMapper.get(key);
            if (value.equals(inputElementType))
               outputElementType = key;
         }
      }
      
      if (outputElementType == null)
         throw new ConversionException(
            "No output element type mapping found for input element type " + 
            inputElementType.getName());
      
      return outputElementType;
   }   
}
