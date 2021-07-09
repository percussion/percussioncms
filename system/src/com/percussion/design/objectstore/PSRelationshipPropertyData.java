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
package com.percussion.design.objectstore;

import com.percussion.cms.IPSConstants;
import com.percussion.util.PSXMLDomUtil;

import java.io.Serializable;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Represents a single back-end row value in 
 * {@link IPSConstants#PSX_RELATIONSHIPPROPERTIES} table
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSRelationshipPropertyData")
@IdClass(PSRelationshipPropertyDataPk.class)
@Table(name = IPSConstants.PSX_RELATIONSHIPPROPERTIES)
public class PSRelationshipPropertyData implements Serializable
{
   /**
    * Computed serial number
    */
   private static final long serialVersionUID = 4793180603390459489L;

   /**
    * The relationship (or parent) id.
    */
   @SuppressWarnings("unused")
   @Id
   @Column(name = "RID")
   private int m_rid;

   /**
    * The name of the property. Set by ctor, never <code>null</code> or empty.
    */
   @Id
   @Column(name = "PROPERTYNAME")
   private String m_propertyName;
   
   /**
    * The value of the property, it may be <code>null</code>.
    */
   @Basic
   @Column(name = "PROPERTYVALUE")
   private String m_propertyValue;

   /**
    * Indicate if this object has been persisted in the repository.
    * <code>true</code> if it is persisted; otherwise <code>false</code>.
    * Default to <code>false</code>.
    */
   @Transient
   private boolean m_isPersisted = false;
   
   /**
    * Default ctor, needed by the services of the persistent layer.
    */
   private PSRelationshipPropertyData()
   {
      // Empty
   }

   /**
    * Creates an instance of this class from the given parameters.
    * This should be used to create an object that does not exist in the
    * repository.
    *
    * @param name the name of the property, never <code>null</code> or empty.
    * @param value the value of the property, may be <code>null</code> or empty.
    *
    */
   public PSRelationshipPropertyData(String name, String value)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null.");

      m_propertyName = name;
      m_propertyValue = value;
   }

   /**
    * Creates an instance from a XML representation.
    *
    * @param sourceNode the element contains XML representation of the object.
    *   Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if sourceNode contains malformed XML.
    */
   public PSRelationshipPropertyData(Element sourceNode)
         throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode);
   }


   /**
    * Set the parent relationship id for this object. This is used by the persistent layer right
    * before save the property to the repository.
    *
    * @param rid the new parent id, must be a valid (greater than zero) id.
    */
   public void setRid(int rid)
   {
      if (rid <= 0)
         throw new IllegalArgumentException("rid must be > 0.");

      m_rid = rid;
   }

   /**
    * @return the value of the property, it may be <code>null</code> or empty.
    */
   public String getName()
   {
      return m_propertyName;
   }

   /**
    * Set the persistent status. This is typically used by the persisted
    * layer.
    * 
    * @param isPersisted the to be set persistent status. <code>true</code> if
    *    the object is already persisted in the repository; otherwise 
    *    <code>false</code>.
    */
   public void setPersisted(boolean isPersisted)
   {
      m_isPersisted = isPersisted;
   }

  /**
    * Determines if this object is persisted in the repository.
    *
    * @return <code>true</code> if this object does exist in the repository;
    *    otherwise return <code>false</code>. Default to <code>false</code>.
   */
   public boolean isPersisted()
   {
      return m_isPersisted;
   }
   
   /**
    * @return the value of the property, it may be <code>null</code> or empty.
    */
   public String getValue()
   {
      return m_propertyValue;
   }

   /**
    * @param value the name to set.
    */
   public void setValue(String value)
   {
      m_propertyValue = value;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof PSRelationshipPropertyData))
         return false;
      PSRelationshipPropertyData other = (PSRelationshipPropertyData) obj;

      // skip compare m_isPersisted, which may not be set after save to repository
      return new EqualsBuilder()
         .append(m_propertyName, other.m_propertyName)
         .append(m_propertyValue, other.m_propertyValue)
         .isEquals();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      // skip m_isPersisted, keep consistent with equals()
      return new HashCodeBuilder(13, 3)
            .append(m_propertyName)
            .append(m_propertyValue)
            .toHashCode();
   }

   /**
    * Populates the object from its XML representation.
    *
    * @throws PSUnknownNodeTypeException if sourceNode contains malformed XML.
    *
    * @see #toXml(Document) for its XML format.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      PSXMLDomUtil.checkNode(sourceNode, XML_NODE_NAME);
      m_isPersisted = PSXMLDomUtil.checkAttributeBool(sourceNode,
            XML_ATTR_ISPERSISTED, false, PSRelationship.XML_TRUE);
      m_propertyName = PSXMLDomUtil.checkAttribute(sourceNode, XML_ATTR_NAME,
            true);
      m_propertyValue = PSXMLDomUtil.getElementData(sourceNode);
   }

   /**
    * Produce the XML representation of this object. The XML format is:
    * <pre><code>
    * &lt;!ELEMENT PSXRelationshipProperty (#PCDATA)>
    * &lt;!ATTLIST Action
    *    isPersisted (0, 1) #REQUIRED
    *    name CDATA #REQUIRED
    *    >
    * </code></pre>
    *
    * @param doc the document used to create XML elements, must not be
    *   <code>null</code>.
    *
    * @return the created XML format of the object, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      Element propEl = doc.createElement(XML_NODE_NAME);
      String isPersisted = m_isPersisted
            ? PSRelationship.XML_TRUE
            : PSRelationship.XML_FALSE;
      propEl.setAttribute(XML_ATTR_ISPERSISTED, isPersisted);
      propEl.setAttribute(XML_ATTR_NAME, String.valueOf(m_propertyName));
      Text textNode = propEl.getOwnerDocument().createTextNode(m_propertyValue == null ? "" : m_propertyValue);
      propEl.appendChild(textNode);

      return propEl;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      return "<persisted=" + m_isPersisted + ",pname=" + m_propertyName
            + ",pvalue=" + m_propertyValue + ">";
   }

   private final static String XML_NODE_NAME = "Property";
   private final static String XML_ATTR_ISPERSISTED = "persisted";
   private final static String XML_ATTR_NAME = "name";
}
