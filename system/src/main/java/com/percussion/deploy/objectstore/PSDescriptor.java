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

package com.percussion.deploy.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for all descriptors used to run deployment jobs.
 */
public abstract class PSDescriptor  implements IPSDeployComponent
{
   /**
    * Constructs this descriptor specifying the name.
    * 
    * @param name The name, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>name</code> is invalid.
    */
   protected PSDescriptor(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      
      m_name = name;
   }
   
   /**
    * Construct this object from its XML representation.  See 
    * {@link #toXml(Document)} for the format expected.
    * 
    * @param src The source XML element, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>src</code> is <code>null</code>.
    * @throws PSUnknownNodeTypeException if <code>src</code> is malformed.
    */
   protected PSDescriptor(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");
         
      fromXml(src);
   }
   
   /**
    * Parameterless ctor for use by derived classes only.
    */
   protected PSDescriptor()
   {
   }
   
   /**
    * Sets the name of this descriptor.
    * 
    * @param name The name, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>name</code> is invalid.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
         
      m_name = name;
   }

   /**
    * Gets the name of this descriptor.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /** 
    * Sets the description for this object.
    * 
    * @param desc The description, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>desc</code> is invalid.
    */
   public void setDescription(String description)
   {
      if (description == null)
         throw new IllegalArgumentException(
            "description may not be null");
      
      m_description = description;
   }
   
   /**
    * Get the description for this object. 
    * 
    * @return The description, never <code>null</code>, may be empty.
    */
   public String getDescription()
   {
      return m_description;
   }
   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXDescriptor (Description)>
    * %lt;!ATTLIST PSXDescriptor
    *    Name CDATA #REQUIRED
    * >
    * </code></pre>
    * 
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
         
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, m_name);
      PSXmlDocumentBuilder.addElement(doc, root, XML_EL_DESC, m_description);
      
      return root;
   }

   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See 
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
         
      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      m_name = PSDeployComponentUtils.getRequiredAttribute(sourceNode, 
         XML_ATTR_NAME);
         
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      String description = tree.getElementData(XML_EL_DESC);
      if (description == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_EL_DESC);
      }
      
      m_description = description;
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");
         
      if (!(obj instanceof PSDescriptor))
         throw new IllegalArgumentException("obj wrong type");

      PSDescriptor desc = (PSDescriptor)obj;
      m_name = desc.m_name;
      m_description = desc.m_description;
      
   }

   // see IPSDeployComponent interface
   public int hashCode()
   {
      return m_name.hashCode() + m_description.hashCode();
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;
      if (!(obj instanceof PSDescriptor))
         isEqual = false;
      else
      {
         PSDescriptor other = (PSDescriptor)obj;
         if (!m_name.equals(other.m_name))
            isEqual = false;
         else if (!m_description.equals(other.m_description))
         {
            isEqual = false;
         }
      }
      
      return isEqual;
   }
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXDescriptor";

   /**
    * Name of this descriptor, never <code>null</code> or empty after ctor, 
    * may be modified by a call to {@link #setName(String)}
    */
   private String m_name;

   /**
    * The description for this object, initialized to an empty string. May be 
    * modified by calls to {@link #setDescription(String)}. Never <code>null
    * </code>
    */
   private String m_description = "";
   
   // private Xml constants
   private static final String XML_ATTR_NAME = "name";
   private static final String XML_EL_DESC = "Description";
}
