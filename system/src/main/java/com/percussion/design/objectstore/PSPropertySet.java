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
package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * This class encapsulates a collection of <code>PSProperty</code> objects as
 * a <code>PSCollectionComponent</code>. 
 */
public class PSPropertySet extends PSCollectionComponent
{
   /**
    * Constucts an empty property set.
    */
   public PSPropertySet()
   {
      super(PSProperty.class);
   }
   
   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of 
    *    the appropriate type
    */
   public PSPropertySet(Element sourceNode) throws PSUnknownNodeTypeException
   {
      super(PSProperty.class);

      fromXml(sourceNode, null, null);
   }

   /** @see IPSComponent */
   @Override
   public void fromXml(Element sourceNode, 
         @SuppressWarnings("unused") IPSDocument parentDoc, 
         List parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element node = null;
      try 
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
         
         node = tree.getNextElement(PSProperty.XML_NODE_NAME, firstFlags);
         String propName;
         while (node != null)
         {
            propName = node.getAttribute("name");
            if (propName != null && propName.trim().length() > 0)
            {
               PSProperty property = new PSProperty((Element) tree.getCurrent());
               add(property);
            }
            
            node = tree.getNextElement(PSProperty.XML_NODE_NAME, nextFlags);
         }
      } 
      finally 
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /** @see IPSComponent */
   @Override
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      
      // sort the properties by name to ensure XML has predictable order
      int numProps = size();
      List<PSProperty> properties = new ArrayList<>(numProps);
      for (int i=0; i<numProps; i++)
      {
         PSProperty property = (PSProperty) get(i);
         properties.add(property);
      }
      Collections.sort(properties, new Comparator<PSProperty>()
            {
               // order by property names
               public int compare(PSProperty p1, PSProperty p2)
               {
                  return p1.getName().compareTo(p2.getName());
               }        
            });

      for (PSProperty property : properties)
      {
         root.appendChild(property.toXml(doc));
      }

      return root;
   }
   
   /**
    * Get the property for the supplied name. Names are compared case
    * sensitive.
    * 
    * @param name the property name, may be <code>null</code> or empty in
    *    which case this always returns <code>null</code>.
    * @return the property if found, <code>null</code> otherwise.
    */
   public PSProperty getProperty(String name)
   {
      if (name == null || name.trim().length() == 0)
         return null;
         
      Iterator properties = iterator();
      while (properties.hasNext())
      {
         PSProperty property = (PSProperty) properties.next();
         if (property.getName().equals(name))
            return property;
      }
      
      return null;
   }

   /**
    * Sets the property of type <code>String</code> for the supplied name,
    * value and lock.
    * 
    * @param name the property name to set, not <code>null</code> or empty.
    * @param value the property value to set, may be <code>null</code> or empty.
    * @param locked <code>true</code> if the property created is locked for
    *    overriding at runtime, <code>false</code> otherwise.
    */
   public void setProperty(String name, String value, boolean locked)
   {
      // name constraint is enforced in PSProperty
      PSProperty property = new PSProperty(name);
      property.setValue(value);
      property.setType(PSProperty.TYPE_STRING);
      property.setLock(locked);
      
      setProperty(property);
   }
   
   /**
    * Convenience method that calls {@link #setProperty(String,String,boolean)
    * setProperty(name, value, true)}.
    */
   public void setProperty(String name, String value)
   {
      setProperty(name, value, true);
   }
   
   /**
    * Sets the supplied property. If a property with the same name as the 
    * supplied one exists, it is replaced with the new property, otherwise
    * it is appendend, the comparison is done using the property name in a
    * case-sensitive manner.
    * 
    * @param property the property to set, not <code>null</code>.
    */
   public void setProperty(PSProperty property)
   {
      if (property == null)
         throw new IllegalArgumentException("property cannot be null");
         
      PSProperty current = getProperty(property.getName());
      if (current != null)
         remove(current);
         
      add(property);
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   @Override
   public Object clone()
   {
      PSPropertySet clone = new PSPropertySet();
      Iterator iter = iterator();
      while (iter.hasNext())
         clone.add(((PSProperty)iter.next()).clone());

      return clone;
   }

   /** The XML node name */
   public static final String XML_NODE_NAME = "PSXPropertySet";
}
