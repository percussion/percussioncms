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
package com.percussion.utils.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a simple wrapper that allows access to an object "property" by
 * referencing the java get method through reflection. The first time a property
 * is accessed, the method is looked up and stored in a hashmap with a key of
 * the property name and the class of the object. After that, the method can be
 * found directly.
 * 
 * @author dougrand
 */
public class PSPropertyWrapper
{
   private static final Class[] noparams = new Class[0];

   private static final Object[] emptyargs = new Object[0];

   /**
    * The wrapped object, only <code>null</code> after the constructor is
    * called if this should be lazy loaded
    */
   protected Object m_wrappedObject;

   /**
    * The loader, defined for lazy loaded properties and <code>null</code>
    * otherwise.
    */
   protected IPSPropertyLoader m_loader = null;

   /**
    * Set to true once initialized
    */
   protected boolean m_initialized = false;

   /**
    * Lookup key for the method map
    */
   private static class Key
   {
      private String m_property;

      private String m_type; // get or set

      private Class m_class;

      /**
       * Create a new key, assume that arguments are always correct
       * @param clazz 
       * @param prop 
       * @param type 
       */
      public Key(Class clazz, String prop, String type) {
         m_property = prop;
         m_class = clazz;
         m_type = type;
      }

      /**
       * @see java.lang.Object#equals(java.lang.Object)
       */
      @Override
      public boolean equals(Object b)
      {
         EqualsBuilder eq = new EqualsBuilder();
         Key keyb = (Key) b;
         return eq.append(m_property, keyb.m_property).append(m_class,
               keyb.m_class).append(m_type, keyb.m_type).isEquals();
      }

      /**
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode()
      {
         HashCodeBuilder hash = new HashCodeBuilder();
         return hash.append(m_property).append(m_class).append(m_type)
               .toHashCode();
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         StringBuilder b = new StringBuilder(40);
         b.append("Key(");
         b.append(m_class.getSimpleName());
         b.append("#");
         b.append(m_type);
         b.append(".");
         b.append(m_property);
         b.append(")");
         return b.toString();
      }
   }

   /**
    * Storage for method maps by property and class
    */
   private static Map<Key, Method> ms_methodMap = new ConcurrentHashMap<>(16, 0.9f, 1);;

   /**
    * Create instance with an object to wrap
    * 
    * @param obj an object to wrap, never <code>null</code>
    */
   public PSPropertyWrapper(Object obj) {
      if (obj == null)
      {
         throw new IllegalArgumentException("obj may not be null");
      }
      m_wrappedObject = obj;
   }

   /**
    * Ctor intended for subclasses that don't need to always use the
    * capabilities of this class
    */
   protected PSPropertyWrapper() {

   }

   /**
    * Ctor
    * 
    * @param loader the property loader to use when initializing this property,
    *           never <code>null</code>
    */
   protected PSPropertyWrapper(IPSPropertyLoader loader) {
      if (loader == null)
      {
         throw new IllegalArgumentException("loader may not be null");
      }
      m_loader = loader;
   }

   /**
    * Lookup a property value on wrapped object
    * 
    * @param pname the property name, never <code>null</code> or empty
    * @return the value, may be any value
    * @throws PSPropertyAccessException if there is a problem accessing the
    *            property method, class or another runtime error
    */
   public Object getPropertyValue(String pname) throws PSPropertyAccessException
   {
      if (pname == null || pname.trim().length() < 2)
      {
         throw new IllegalArgumentException(
               "pname may not be null or contain less than 2 characters");
      }

      try
      {
         Method m = findMethod(pname, "get");
         return m.invoke(m_wrappedObject, emptyargs);
      }
      catch (SecurityException | InvocationTargetException | IllegalAccessException e)
      {
         throw new PSPropertyAccessException(e);
      }
      catch (NoSuchMethodException e)
      {
         throw new PSPropertyAccessException(
               "No matching get method found for property " + pname, e);
      }
   }

   /**
    * Find and return the method for the given property and type
    * 
    * @param pname the property name assumed not <code>null</code>
    * @param type the type, either get or set
    * @return a method, which will be cached
    * @throws NoSuchMethodException
    */
   private synchronized Method findMethod(String pname, String type)
         throws NoSuchMethodException
   {
      if (!type.equals("get") && !type.equals("set"))
      {
         throw new IllegalArgumentException("Type must be get or set");
      }
      if (m_wrappedObject == null)
      {
        this.init();
      }
      Class clazz = m_wrappedObject.getClass();
      Key k = new Key(clazz, pname, type);
      Method m = null;
      Class[] args = noparams;
      if (type.equals("set"))
      {
         Method getm = findMethod(pname, "get");
         args = new Class[]
         {getm.getReturnType()};
      }
      m = ms_methodMap.get(k);

      if (m == null) {
         String str = type +
                 Character.toTitleCase(pname.charAt(0)) +
                 pname.substring(1);
         m = clazz.getMethod(str, args);
         ms_methodMap.put(k, m);
      }

      return m;
   }

   /**
    * Set the named property on the wrapped object.
    * 
    * @param propertyName the name of the property to get, never
    *           <code>null</code> or empty
    * @param value the new value, may be <code>null</code>
    * @throws PSPropertyAccessException if there is a problem accessing the
    *            property method, class or another runtime error
    */
   public void setProperty(String propertyName, Object value)
         throws PSPropertyAccessException
   {
      if (propertyName == null || propertyName.trim().length() < 2)
      {
         throw new IllegalArgumentException(
               "pname may not be null or contain less than 2 characters");
      }

      try
      {
         Method m = findMethod(propertyName, "set");
         Object[] args = new Object[]
         {value};
         m.invoke(m_wrappedObject, args);
      }
      catch (SecurityException | IllegalAccessException | InvocationTargetException e)
      {
         throw new PSPropertyAccessException(e);
      }
      catch (NoSuchMethodException e)
      {
         throw new PSPropertyAccessException(
               "No matching set method found for property " + propertyName, e);
      }
   }

   /**
    * Initialize, if this is a lazy loaded object, call the loader
    */

   public synchronized void init()
   {
      if (m_initialized && m_wrappedObject != null) return;

      m_initialized = false;
      if (m_loader == null )
      {
         if(m_wrappedObject == null) {
            throw new IllegalStateException(
                    "Invalid state, wrapped object is null and no loader");
         }
      }else {
         m_wrappedObject = m_loader.getLazy();
         m_initialized = true;
      }


   }
}
