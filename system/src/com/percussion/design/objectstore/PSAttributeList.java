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

package com.percussion.design.objectstore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class defines a set of attributes stored as a database component.
 * Each attribute will be defined as a mapping from a name
 * (type <code>String</code>) and a collection of values
 * (The collection will contain <code>String</code> objects.
 *
 */
public class PSAttributeList extends PSDatabaseComponentCollection
   implements IPSComponent
{
   /**
    * Constructor for serialization, fromXml, etc.
    */
   public PSAttributeList()
   {
      super((new PSAttribute()).getClass(),
            (new PSAttribute()).getDatabaseAppQueryDatasetName());
   }

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param   sourceNode     the XML element node to construct this
    *                             object from
    *
    * @exception   PSUnknownNodeTypeException
    *                             if the XML element node is not of the
    *                             appropriate type
    */
   public PSAttributeList(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, null, null);
   }
   
   /**
    * Constructs a new attribute list for the supplied map of attributes.
    * 
    * @param attributes a map of attributes, may be <code>null</code> or empty.
    *    The map keys must be <code>String</code> objects, while the map
    *    values must either be of type <code>String</code> or <code>List</code>.
    */
   public PSAttributeList(Map attributes)
   {
      this();
      
      if (attributes != null)
      {
         Iterator keys = attributes.keySet().iterator();
         while (keys.hasNext())
         {
            Object key = keys.next();
            if (!(key instanceof String))
               throw new IllegalArgumentException(
                  "map keys must be of type string");
               
            String name = (String) key;

            PSAttribute attribute = new PSAttribute(name);
            Object value = attributes.get(name);
            if (value instanceof List)
            {
               attribute.setValues((List) value);
            }
            else if (value instanceof String)
            {
               List valueList = new ArrayList();
               valueList.add(value);
               attribute.setValues(valueList);
            }
            else if (value != null)
               throw new IllegalArgumentException(
                  "map values must be of type string or list");
         }
      }
   }

   /**
    * Set an attribute's values, replacing existing values if they are already
    * defined.
    *
    * @param name The attribute's name, may not be <code>null</code> or empty.
    *
    * @param value The list of values.  Can be <code>null</code>.
    */
   public void setAttribute(String name, List value)
   {
      if ((name == null) || (name.length() == 0))
         throw new IllegalArgumentException("Name must be specified.");

      if (value == null)
         value = new ArrayList();
         
      /* find the attribute with the specified name and modify,
       * add an attribute with the specified name if none found.
       */
      PSAttribute attr = getAttribute(name);
      if (attr == null)
      {
         attr = new PSAttribute(name);
         add(attr);
      }
      attr.setValues(value);
   }

   /**
    * Get the attribute specified.
    *
    * @param name The name of the attribute to retrieve, may not be
    * <code>null</code> or empty.  A case-insensitive search is done.
    *
    * @return The attribute.  May be <code>null</code>, indicating
    * the attribute is not defined in this list.  May be empty, indicating
    * the attribute is in the list but has no value defined.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public PSAttribute getAttribute(String name)
   {
      if ((name == null) || (name.trim().length() == 0))
         throw new IllegalArgumentException("Name must be specified.");

      PSAttribute result = null;

      for (int i=0; i < size(); i++)
      {
         PSAttribute attr = (PSAttribute)get(i);
         if (attr.getName().equalsIgnoreCase(name))
         {
            result = attr;
            break;
         }
      }

      return result;
   }
   
   /**
    * Merges the supplied attributes into this attribute list.
    * 
    * @param attributes The attributes to merge, may not be <code>null</code>.
    */
   public void mergeAttributes(PSAttributeList attributes)
   {
      if (attributes == null)
         throw new IllegalArgumentException("attributes may not be null");
      
      Iterator attrs = attributes.iterator();
      while (attrs.hasNext())
      {
         PSAttribute attr = (PSAttribute)attrs.next();
         PSAttribute match = getAttribute(attr.getName());
         if (match == null)
            add(attr);
         else
            match.merge(attr);
      }
   }   
   
   /**
    * Test if this attribute list is equal to the supplied attribute list. Two 
    * attribute lists are equal if list 1 has the same entries as list 2. The
    * order is not important.
    * 
    * @param obj the object to test against, may be <code>null</code>. 
    */
   public boolean equals(Object obj)
   {
      if (obj instanceof PSAttributeList)
      {
         PSAttributeList attributeList = (PSAttributeList) obj;
         if (size() != attributeList.size())
            return false;
         
         Iterator attributes = iterator();
         while (attributes.hasNext())
         {
            PSAttribute attribute1 = (PSAttribute) attributes.next();
            PSAttribute attribute2 = attributeList.getAttribute(
               attribute1.getName());
            if (!attribute1.equals(attribute2))
               return false;
         }
         
         return true;
      }
      
      return false;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   /** IPSComponent interface implementation */

   /**
    * This method is called to populate a PSRole Java object
    * from a PSXRole XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @throws   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXRole
    *
    * @see IPSComponent#fromXml(Element, IPSDocument, ArrayList) for the 
    * interface description
    */
   @Override
   public void fromXml(Element sourceNode, IPSDocument parentDoc, 
                       ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      if (false == ms_NodeType.equals (sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      // just delegate to our collection
      super.fromXml(sourceNode, parentDoc, parentComponents);
   }
   
   /**
    * This method is called to create a PSXAttributeList XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    * &lt;!ELEMENT PSXAttributeList (PSXAttribute*)&gt;
    * &lt;!ATTLIST PSXAttributeList
    *    id CDATA #REQUIRED
    * &gt;
    * </code></pre>
    *
    * @param doc The parent document.   May not be <code>null</code>.
    *
    * @return the newly created PSXAttributeList XML element node
    *
    * @throws IllegalArgumentException if doc is <code>null</code>
    */
   @Override
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      //create PSXSubject element and add type attribute
      Element root = doc.createElement(ms_NodeType);

      //add id attribute
      root.setAttribute(ID_ATTR, String.valueOf(m_id));

      // add our attributes
      addCollectionComponents(root, doc);

      return root;
   }

   static String ms_NodeType = "PSXAttributeList";
}
