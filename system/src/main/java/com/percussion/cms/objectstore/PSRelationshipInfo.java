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

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is a read only class, it contains partial information of a specific
 * relationship configuration.
 */
public class PSRelationshipInfo implements IPSCmsComponent
{
   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSRelationshipInfo(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode);
     
   }


   /**
    * Constructs a configuration info object from a config object.
    *
    * @param config the config object, may not be <code>null</code> or empty.
    */
   public PSRelationshipInfo(PSRelationshipConfig config)
   {
      if(config == null)
         throw new IllegalArgumentException("config may not be null.");


      m_name = config.getName();
      m_label = config.getLabel();
      m_description = (config.getDescription() == null) ? "" :
         config.getDescription();
      m_category = config.getCategory();
   }

 
      
   
   /**
    * Test if the provided object and this are equal.
    *
    * @param o the object to compare to, may be <code>null</code>.
    * @return <code>true</code> if this and o are equal,
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSRelationshipInfo))
         return false;

      PSRelationshipInfo t = (PSRelationshipInfo) o;

      return m_name.equals(t.m_name) &&
         m_label.equals(t.m_label) &&
         m_description.equals(t.m_description);
   }
   
   /**
    * Not implemented. Overrides {@link Object#hashCode()}.
    */
   @Override
   public int hashCode()
   {
      throw new UnsupportedOperationException("Not Implemented");
   }

   /**
    * Get the relationship name. The name is unique server-wide.
    *
    * @return the relationship name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Get the relationship label.
    *
    * @return the relationship label, never <code>null</code> or empty.
    */
   public String getLabel()
   {
      return m_label;
   }

   /**
    * Get the relationship description.
    *
    * @return the relationship description, never <code>null</code>,
    *    but may be empty.
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * Get the relationship category.
    *
    * @return the relationship category, never <code>null</code> or empty.
    */
   public String getCategory()
   {
      return m_category;
   }


   /**
    * See {@link IPSCmsComponent#toXml(Document) interface} for details.
    * The DTD for this class is as follows:
    * <pre><code>
    *    &lt;!ELEMENT PSXRelationshipInfo (Explanation?)>
    *    &lt;!ATTLIST PSXRelationshipInfo
    *       name  CDATA #REQUIRED
    *       label CDATA #REQUIRED
    *       >
    * </code></pre>
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(LABEL_ATTR, m_label);

      if(m_description != null)
      {
         PSXmlDocumentBuilder.addElement(
            doc, root, DESCRIPTION_ELEM, m_description);
      }
      if(m_category != null)
      {
         PSXmlDocumentBuilder.addElement(
            doc, root, CATEGORY_ATTR, m_category);
      }

      return root;
     
   }

   // See IPSCmsComponent interface
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
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

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      // REQUIRED: name attribute
      m_name = tree.getElementData(NAME_ATTR);
      if (m_name == null || m_name.trim().length() == 0)
      {
         Object[] args =
         {
            XML_NODE_NAME,
            NAME_ATTR,
            "null or empty"
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      m_label = tree.getElementData(LABEL_ATTR);
      if (m_label == null || m_label.trim().length() == 0)
      {
         Object[] args =
         {
            XML_NODE_NAME,
            LABEL_ATTR,
            "null or empty"
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

    
      m_category = tree.getElementData(CATEGORY_ATTR);
      if (m_category == null || m_category.trim().length() == 0)
      {
         Object[] args =
         {
            XML_NODE_NAME,
            CATEGORY_ATTR,
            "null or empty"
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }        
    
      // OPTIONAL: Description element
      Element node = tree.getNextElement(DESCRIPTION_ELEM, firstFlags);
      if (node != null)
         m_description = tree.getElementData(node);
      else
         m_description = "";

      
     


   }

   /**
    * See {@link IPSCmsComponent#getNodeName() interface}
    */
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   /**
    * See {@link IPSCmsComponent#clone() interface}
    *
    * @throws UnsupportedOperationException Always.
    */
   public Object clone()
   {
      throw new UnsupportedOperationException(
            "Not supported by this processor.");
   }

   /**
    * Gets the string representation of this object (Label of the relationship).
    *
    * @return the label, never <code>null</code> or empty.
    */
   public String toString()
   {
      return m_label;
   }


   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXRelationshipInfo";
   /**
    * The relationship category.  
    */
   private String m_category = null;
   /**
    * The relationship name, server-wide unique.  Initialized in ctor, may be
    * modified through calls to <code>setName(String)</code>, never <code>null
    * </code> or empty.
    */
   private String m_name = null;

   /**
    * The relationship label.  Initialized in ctor, may be modified through
    * calls to <code>setLabel(String)</code> and never <code>null</code> or
    * empty.
    */
   private String m_label = null;

   /**
    * The relationship description. Initialized in ctor, may be modified through
    * calls to <code>setDescription(String)</code>. May be <code>null</code> or
    * empty.
    */
   private String m_description = null;

   /*
    * The following strings define all elements/attributes used to parse/create
    * the XML for this object. No Java documentation will be added to this.
    */
   public static final String NAME_ATTR = "name";
   public static final String LABEL_ATTR = "label";
   private static final String DESCRIPTION_ELEM = "Explanation";
   public static final String CATEGORY_ATTR = "category";
   public static final String CONFIG_ATTR = "config";
  

}
