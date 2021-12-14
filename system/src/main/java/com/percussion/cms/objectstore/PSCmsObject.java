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
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * An object representation of a row in the PSX_OBJECTS table. This object
 * is immutable.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSCmsObject")
@Table(name = "PSX_OBJECTS")
public class PSCmsObject implements IPSCmsComponent
{

   /**
    * Default constructor, which is needed for Hibernate.
    */
   private PSCmsObject()
   {
   }
   
   /**
    * Creates an instance from a previously serialized (using <code>toXml
    * </code>) system object.
    *
    * @param source A valid element that meets the dtd defined in the
    *    description of {@link #toXml(Document)}. Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException If the supplied source element does
    *    not conform to the dtd defined in the <code>fromXml<code> method.
    */
   public PSCmsObject(Element source) throws PSUnknownNodeTypeException
   {
      fromXml(source);
   }


   /**
    * Get the name of the object.
    *
    * @return The object name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Get short message that describes this object.
    *
    * @return The description, may be empty, but never <code>null</code>.
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * Get the factory class name of this object.
    *
    * @return The full path of a class name, may be empty, but never
    *    <code>null</code>.
    */
   public String getFactory()
   {
      return m_factory;
   }

   /**
    * Get the object type id that is the primary key of the object table
    *
    * @return The type id of this object.
    */
   public int getTypeId()
   {
      return m_typeId;
   }

   /**
    * Determines if the object is permitted to perform the workflow process
    *
    * @return The <code>true</code> if it is workflowable; <code>false</code>
    *    if it is not permitted to perform workflow operation.
    */
   public boolean isWorkflowable()
   {
      return m_isWorkflowable == 1;
   }

   /**
    * Determines if the object can have different revisions.
    *
    * @return The <code>true</code> if it is permitted to perform revision
    *    operation.
    */
   public boolean isRevisionable()
   {
      return m_isRevisionable == 1;
   }


   /*
    *  (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals( Object o )
   {
      if ( !(o instanceof PSCmsObject ))
         return false;

      PSCmsObject obj2 = (PSCmsObject) o;

      return m_name.equals(obj2.m_name) &&
          m_typeId == obj2.m_typeId &&
          m_isWorkflowable == obj2.m_isWorkflowable &&
          m_isRevisionable == obj2.m_isRevisionable &&
          m_description.equals(obj2.m_description) &&
          m_factory.equals(obj2.m_factory);
   }


   /*
    *  (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return m_name.hashCode() + m_description.hashCode() +
         m_factory.hashCode() + m_typeId +
         m_isWorkflowable +
         m_isRevisionable;
   }

   /**
    * Serializes this object into an xml element that can be attached to the
    * supplied document. It will conform to the following dtd:
    * <pre>
    * &lt;!ELEMENT PSXCmsObject (Description, Factory)>
    * &lt;!ATTLIST PSXCmsObject
    *    name            CDATA #REQUIRED
    *    typeId          CDATA #REQUIRED
    *    isWorkflowable  CDATA #REQUIRED
    *    isRevisionable  CDATA #REQUIRED
    *    >
    * &lt;!ELEMENT Description (#PCDATA)>
    * &lt;!ELEMENT Factory (#PCDATA)>
    * </pre>
    *
    * @param doc Used to generate the element. Never <code>null</code>.
    *
    * @return the generated element, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (null == doc)
         throw new IllegalArgumentException("doc must be supplied");

      Element root = doc.createElement(XML_NODE_NAME);

      root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_TYPEID, "" + m_typeId);
      root.setAttribute(XML_ATTR_ISWORKFLOWABLE, "" + m_isWorkflowable);
      root.setAttribute(XML_ATTR_ISREVISIONABLE, "" + m_isRevisionable);

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_DESCRIPTION,
         m_description);
      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_FACTORY,
         m_factory);

      return root;
   }

   /**
    * See {@link IPSCmsComponent#fromXml(Element)}
    */
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      PSXMLDomUtil.checkNode(sourceNode, XML_NODE_NAME);

      m_name = PSXMLDomUtil.checkAttribute(sourceNode, XML_ATTR_NAME, true);

      m_typeId = PSXMLDomUtil.checkAttributeInt(sourceNode,
         XML_ATTR_TYPEID, true);

      String sWorkflowable = PSXMLDomUtil.checkAttribute(sourceNode,
         XML_ATTR_ISWORKFLOWABLE, true);
      m_isWorkflowable = sWorkflowable.equals("1") ? 1 : 0;

      String sRevisionable = PSXMLDomUtil.checkAttribute(sourceNode,
         XML_ATTR_ISREVISIONABLE, true);
      m_isRevisionable = sRevisionable.equals("1") ? 1 : 0;

      Element descEl = PSXMLDomUtil.getFirstElementChild(sourceNode,
         XML_NODE_DESCRIPTION);
      m_description = PSXMLDomUtil.getElementData(descEl);

      Element factoryEl = PSXMLDomUtil.getNextElementSibling(descEl,
         XML_NODE_FACTORY);
      m_factory = PSXMLDomUtil.getElementData(factoryEl);
   }


   /*
    *  (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   @Override
   public Object clone()
   {
      PSCmsObject copy = null;

      try
      {
         copy = (PSCmsObject) super.clone();
      }
      catch (Exception e) {} // not possible

      return copy;
   }

   /**
    * @see IPSCmsComponent#getNodeName()
    */
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   /**
    * Checks whether the supplied type is one of the TYPE_xxx values.
    *
    * @param type Any value.
    *
    * @return <code>true</code> if the supplied type is one of the
    *    TYPE_xxx values, <code>false</code> otherwise.
    */
   public static boolean isValidType(int type)
   {
      return type >= TYPE_ITEM && type <= TYPE_META;
   }
   
   /**
    * The object type for items, as one of the values in the OBJECTTYPE
    * column of the object table
    */
   public static final int TYPE_ITEM = 1;

   /**
    * The object type for folders, as one of the values in the OBJECTTYPE
    * column of the object table
    */
   public static final int TYPE_FOLDER = 2;

   /**
    * The object type for meta data items, such as template and widget items.
    */
   public static final int TYPE_META = 3;

   /**
    * The name of the object. Initialized by the constructor.
    * Never <code>null</code> or empty after that.
    */
   @Basic
   @Column(name = "OBJECTNAME")
   private String m_name;

   /**
    * The object type id that is the primary key of the object table
    * Initialized by constructor, never modified after that.
    */
   @Id
   @Column(name = "OBJECTTYPE")
   private int m_typeId;

   /**
    * The description of the object. Initialized by constructor, never
    * <code>null</code>, but may be empty, after that.
    */
   @Basic
   @Column(name = "DESCRIPTION")
   private String m_description;

   /**
    * The factory class name of the object. Initialized by constructor,
    * never <code>null</code>, but may be empty, after that.
    */
   @Basic
   @Column(name = "FACTORY")
   private String m_factory;

   /**
    * See {@link #isWorkflowable()} for description
    */
   @Basic
   @Column(name = "ISWORKFLOWABLE")
   private int m_isWorkflowable;

   /**
    * See {@link #isRevisionable()} for description
    */
   @Basic
   @Column(name = "ISREVISIONABLE")
   private int m_isRevisionable;

   /**
    * The XML node name of this object
    */
   public final static String XML_NODE_NAME = "PSXCmsObject";

   // Private constants for XML attribute and element name
   private final static String XML_ATTR_NAME = "name";
   private final static String XML_ATTR_TYPEID = "typeId";
   private final static String XML_ATTR_ISWORKFLOWABLE = "isWorkflowable";
   private final static String XML_ATTR_ISREVISIONABLE = "isRevisionable";
   private final static String XML_NODE_DESCRIPTION = "Description";
   private final static String XML_NODE_FACTORY = "Factory";
   private final static String XML_TRUE = "1";
   private final static String XML_FALSE = "0";
}
