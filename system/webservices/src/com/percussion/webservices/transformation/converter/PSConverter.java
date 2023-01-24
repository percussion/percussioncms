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

import com.percussion.cms.IPSConstants;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * The base converter class provides functionality useful for all converters
 * and implements a standard converter copying all properties for which the
 * names are the same in the source and target objects.
 */
public class PSConverter implements Converter
{
   /**
    * Construct a new converter for the supplied parameters.
    * 
    * @param beanUtils the bean utils instance to be used for all conversions,
    *    not <code>null</code>.
    */
   public PSConverter(BeanUtilsBean beanUtils)
   {
      if (beanUtils == null)
         throw new IllegalArgumentException("beanUtils cannot be null");
      
      m_beanUtils = beanUtils;
   }

   /* (non-Javadoc)
    * @see Converter#convert(Class, Object)
    */
   public Object convert(Class type, Object value)
   {
      try
      {
         if (value == null)
            return null;

         Object convertedValue = type.newInstance();
         copyProperties(convertedValue, value);
         
         return convertedValue;
      }
      catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e)
      {
         throw new ConversionException(value.toString(), e);
      }
   }

   /**
    * Copy all properties from the supplied origin to the destination but
    * exclude all special properties from the copy process.
    * 
    * @param dest the destination object, not <code>null</code>.
    * @param origin the origin object, not <code>null</code>
    * @throws IllegalAccessException for reflection errors.
    * @throws InvocationTargetException for reflection errors.
    * @throws NoSuchMethodException for reflection errors.
    */
   protected void copyProperties(Object dest, Object origin)
      throws IllegalAccessException, InvocationTargetException, 
         NoSuchMethodException
   {
      if (dest == null)
         throw new IllegalArgumentException("dest cannot be null");

      if (origin == null)
         throw new IllegalArgumentException("origin cannot be null");

      PropertyDescriptor[] origDescriptors =
         getBeanUtils().getPropertyUtils().getPropertyDescriptors(origin);
      for (PropertyDescriptor origDescriptor : origDescriptors) {
         String name = origDescriptor.getName();
         if ("class".equals(name))
            continue;

         if (m_specialProperties.contains(name))
            continue;

         if (getBeanUtils().getPropertyUtils().isReadable(origin, name) &&
                 getBeanUtils().getPropertyUtils().isWriteable(dest, name)) {
            Object value =
                    getBeanUtils().getPropertyUtils().getSimpleProperty(origin,
                            name);
            getBeanUtils().copyProperty(dest, name, value);
         }
      }
   }

   /**
    * Is the requested conversion from client to server object?
    * 
    * @param source the object to be converted, not <code>null</code>.
    * @return <code>true</code> if the source object needs to be converted 
    *    from a client to a server object, <code>false</code> for server to
    *    client object.
    */
   protected boolean isClientToServer(Object source)
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot bne null");
      
      return source.getClass().getName().startsWith(
         "com.percussion.webservices");
   }
   
   /**
    * Get the converter for the specified type.
    * 
    * @param type the type for which to get the converter, not
    *    <code>null</code>.
    * @return the converter for the specified type, never <code>null</code>.
    * @throws ConversionException if no converter was found for the type 
    *    specified.
    */
   protected Converter getConverter(Class type) throws ConversionException
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");
      
      Converter converter = getBeanUtils().getConvertUtils().lookup(type);
      if (converter == null)
         throw new ConversionException(
            "No converter registered for type " + type.getName());
      
      return converter;
   }
   
   /**
    * Get the bean utils used for all conversions.
    * 
    * @return the bean utils, never <code>null</code>.
    */
   protected BeanUtilsBean getBeanUtils()
   {
      return m_beanUtils;
   }
   
   /**
    * A set of property names that will be handled special within the 
    * implementing converter, never <code>null</code>, may be empty.
    */
   protected Set<String> m_specialProperties = new HashSet<>();

   /**
    * Base logger
    */
   protected static final Logger log = LogManager.getLogger(IPSConstants.SERVER_LOG);
   
   /**
    * The bean utils instance to use for all conversions. Initialized in 
    * constructor, never <code>null</code> or changed after that.
    */
   private BeanUtilsBean m_beanUtils = null;
}

