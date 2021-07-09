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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;

/**
 * The class that is used to represent properties as defined by
 * 'sys_Params.dtd'.
 */
public class PSActionParameters
   extends PSDbComponentCollection
{
   /**
    * The default constructor to create parameters with empty list.
    */
   public PSActionParameters()
   {
      super(PSActionParameter.class);
   }

   /**
    * Constructs this object from the supplied element. See {@link
    * #toXml(Document) } for the expected form of xml.
    *
    * @param element the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if element is <code>null</code>
    */
   public PSActionParameters(Element element)
      throws PSUnknownNodeTypeException
   {
      super(element);
   }


   /**
    * Gets the value of the specified parameter. Uses case-insensitive
    * comparison to get the parameter.
    *
    * @param name name of the parameter, may not be <code>null</code> or empty.
    *
    * @return The parameter value, may be <code>null</code> if the specified
    * parameter does not exist. Never <code>null</code> if the parameter is
    * present but may be empty.
    */
   public String getParameter(String name)
   {
      PSActionParameter param = getParameterObject(name);
      if(param == null)
         return null;
      return param.getValue();
   }

   /**
    * Sets the specified parameter with supplied value. If the parameter with
    * that name (case-insensitive) exists it will be replaced. The values are
    * compared case-sensitive when determing whether the property is dirty.
    *
    * @param name name of the parameter, may not be <code>null</code> or empty.
    * @param value value of the parameter, may be <code>null</code> or empty.
    */
   public void setParameter(String name, String value)
   {
      if (null == name || name.trim().length() == 0)
         throw new IllegalArgumentException("Name cannot be null or empty.");

      if (null == value)
         value = "";
      PSActionParameter param = getParameterObject(name);
      if (null == param)
         super.add(new PSActionParameter(name, value));
      else
         param.setValue(value);
   }

   /**
    * This method is overridden to guarantee that the names of all members
    * forms a set based on case-insensitive property names.
    *
    * @param comp Never <code>null</code>. Must be a PSActionProperty.
    */
   public void add(IPSDbComponent comp)
   {
      if (null == comp || !(comp instanceof PSActionParameter))
      {
         throw new IllegalArgumentException(
               "Can only add PSActionParameter components");
      }

      PSActionParameter newParam = (PSActionParameter)comp;
      PSActionParameter param = getParameterObject(newParam.getName());
      if (null != param)
         param.setValue(newParam.getValue());
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
   public boolean removeParameter(String name)
   {
      if (null == name || name.trim().length() == 0)
         return false;

      return remove(getParameterObject(name));
   }

   /**
    * Creates a map of name/value pairs of this parameter collection. The caller
    * takes over ownership of the returned map. The returned map is sorted in
    * alpha order.
    * 
    * @return a map of name/value pair <code>String</code> objects with all 
    *    parameters defined in this collection, never <code>null</code>, 
    *    may be empty.
    */
   public Map toMap()
   {
      // we use a tree map to sort the map in alpha order
      Map params = new TreeMap();
      Iterator parameters = iterator();
      while (parameters.hasNext())
      {
         PSActionParameter parameter = 
            (PSActionParameter) parameters.next();
            
         params.put(parameter.getName(), parameter.getValue());
      }
      
      return params;
   }
   
   /**
    * Reset this parameter collection with the name/value pairs from the 
    * supplied map.
    * 
    * @param params a map of name/value pairs to reset this collection with,
    *    may be <code>null</code> or empty in which case the collection will be
    *    cleared.
    */
   public void fromMap(Map params)
   {
      if (params == null || params.isEmpty())
      {
         clear();
         return;
      }
      
      // add or update the new/changed parameters
      Iterator parameters = params.keySet().iterator();
      while (parameters.hasNext())
      {
         String name = (String) parameters.next();
         String value = (String) params.get(name);
         
         PSActionParameter parameter = getParameterObject(name);
         if (parameter != null)
            parameter.setValue(value);
         else
            add(new PSActionParameter(name, value));
      }
      
      // remove parameters
      List toBeRemoved = new ArrayList();
      parameters = iterator();
      while (parameters.hasNext())
      {
         PSActionParameter parameter = (PSActionParameter) parameters.next();
         if (params.get(parameter.getName()) == null)
            toBeRemoved.add(parameter);
      }
      for (int i=0; i<toBeRemoved.size(); i++)
         remove((IPSDbComponent) toBeRemoved.get(i));
   }

   /**
    * Finds the property in this collection that has the specified name.
    *
    * @param name Assumed not <code>null</code> or empty. The name is compared
    *    case insensitive.
    *
    * @return A valid parameter, or <code>null</code> if one can't be found.
    */
   private PSActionParameter getParameterObject(String name)
   {
      Iterator it = iterator();
      while (it.hasNext())
      {
         PSActionParameter param = (PSActionParameter) it.next();
         if (param.getName().equalsIgnoreCase(name))
            return param;
      }
      return null;
   }

   /**
    * The constant to indicate root node name.
    */
   public static final String XML_NODE_NAME = "PSXActionParameters";
}
