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
