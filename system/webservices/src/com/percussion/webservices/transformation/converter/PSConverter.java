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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.webservices.transformation.converter;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;

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
      catch (IllegalAccessException e)
      {
         throw new ConversionException(value.toString(), e);
      }
      catch (InvocationTargetException e)
      {
         throw new ConversionException(value.toString(), e);
      }
      catch (InstantiationException e)
      {
         throw new ConversionException(value.toString(), e);
      }
      catch (NoSuchMethodException e)
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

      PropertyDescriptor origDescriptors[] = 
         getBeanUtils().getPropertyUtils().getPropertyDescriptors(origin);
      for (int i=0; i<origDescriptors.length; i++)
      {
         String name = origDescriptors[i].getName();
         if ("class".equals(name))
            continue;
         
         if (m_specialProperties.contains(name))
            continue;

         if (getBeanUtils().getPropertyUtils().isReadable(origin, name) && 
            getBeanUtils().getPropertyUtils().isWriteable(dest, name))
         {
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
   protected Set<String> m_specialProperties = new HashSet<String>();
   
   /**
    * The bean utils instance to use for all conversions. Initialized in 
    * constructor, never <code>null</code> or changed after that.
    */
   private BeanUtilsBean m_beanUtils = null;
}

