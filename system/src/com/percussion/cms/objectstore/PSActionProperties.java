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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import java.util.Iterator;

import org.w3c.dom.Element;

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
