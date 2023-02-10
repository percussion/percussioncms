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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * The class that is used to represent properties as defined by
 * 'sys_Props.dtd'.
 */
public class PSActionProperties
   extends PSDbComponentCollection
{
   /**
    * The default constructor to create properties with empty list.
    */
   public PSActionProperties()
   {
      super(PSActionProperty.class);
   }

   /**
    * Constructs this object from the supplied element. See {@link
    * #toXml(Document) } for the expected form of xml.
    *
    * @param element the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if element is <code>null</code>
    */
   public PSActionProperties(Element element) throws PSUnknownNodeTypeException
   {
      super(element);
   }

   /**
    * Convenience method that calls {@link #getProperty(String,String)
    * getProperty(name, null)}.
    */
   public String getProperty(String name)
   {
      return getProperty(name, null);
   }

   /**
    * Gets the value of the specified property. Uses case-insensitive comparison
    * to get the property. Returns the supplied default value if the requested
    * named property does not exist.
    *
    * @param name name of the property, may not be <code>null</code> or empty.
    *
    * @param defaultValue Returned if the property does not exist in the list,
    *    may be <code>null</code> or empty.
    *
    * @return the property value, may be <code>null</code> or <code>empty</code>
    * depending on whether the named property exists and the default value
    * supplied.
    */
   public String getProperty(String name, String defaultValue)
   {
      if (null == name || name.trim().length() == 0)
         throw new IllegalArgumentException("Name cannot be null or empty.");

      PSActionProperty prop = getPropertyObject(name);
      if(prop == null)
         return defaultValue;
      return prop.getValue();
   }

   /**
    * Sets the specified property with supplied value. If the property with
    * that name exists it will be replaced. The property name is compared
    * case-insensitive.
    *
    * @param name name of the property, may not be <code>null</code> or empty.
    * @param value value of the property, may be <code>null</code> or empty.
    *    If <code>null</code> supplied, "" is stored.
    */
   public void setProperty(String name, String value)
   {
      if (null == value)
         value = "";
      PSActionProperty prop = getPropertyObject(name);
      if (null == prop)
         super.add(new PSActionProperty(name, value));
      else
         prop.setValue(value);
   }

   /**
    * This method is overridden to guarantee that the names of all members
    * forms a set.
    *
    * @param comp Never <code>null</code>. Must be a PSActionProperty.
    */
   public void add(IPSDbComponent comp)
   {
      if (null == comp || !(comp instanceof PSActionProperty))
      {
         throw new IllegalArgumentException(
               "Can only add PSActionProperty components");
      }

      PSActionProperty newProp = (PSActionProperty)comp;
      PSActionProperty prop = getPropertyObject(newProp.getName());
      if (null != prop)
         prop.setValue(newProp.getValue());
      else
         super.add(comp);
   }

   /**
    * If a property by the supplied name exists in this set, it is removed.
    * Otherwise, no action is taken.
    *
    * @param name If <code>null</code> or empty, no action is taken. The name
    *    is compared case insensitive.
    *
    * @return <code>true</code> if an entry was removed, <code>false</code>
    *    otherwise.
    */
   public boolean removeProperty(String name)
   {
      if (null == name || name.trim().length() == 0)
         return false;

      return remove(getPropertyObject(name));
   }


   /**
    * Finds the property in this collection that has the specified name.
    *
    * @param name Never <code>null</code> or empty. The name is compared
    *    case insensitive.
    *
    * @return A valid property, or <code>null</code> if one can't be found.
    */
   private PSActionProperty getPropertyObject(String name)
   {
      Iterator it = iterator();
      while (it.hasNext())
      {
         PSActionProperty prop = (PSActionProperty) it.next();
         if (prop.getName().equalsIgnoreCase(name))
            return prop;
      }
      return null;
   }

   public static final String XML_NODE_NAME = "PSXActionProperties";
}
