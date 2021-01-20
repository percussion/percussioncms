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
package com.percussion.services.sitemgr.data;

import com.percussion.services.catalog.IPSCatalogIdentifier;
import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.utils.xml.IPSXmlErrors;
import com.percussion.utils.xml.IPSXmlSerialization;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.utils.xml.PSXmlUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The publishing context controls how links are generated in the HTML documents
 * during assembly.
 * 
 * @author dougrand
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSPublishingContext")
@Table(name = "RXCONTEXT")
public class PSPublishingContext implements IPSCatalogItem, 
   IPSPublishingContext, Serializable, IPSCatalogIdentifier, Cloneable
{
   /**
    * Serial id identifies versions of serialized data
    */
   private static final long serialVersionUID = 1L;
   
   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("default-scheme", PSLocationScheme.class);
   }

   @Id
   @Column(name = "CONTEXTID")
   private long id = -1L;

   @Version
   @Column(name="VERSION")
   private Integer version;
   
   @Basic
   @Column(name = "CONTEXTNAME")
   String name;

   @Basic
   @Column(name = "CONTEXTDESC")
   String description;

   @Basic
   @Column(name = "DEFAULTSCHEMEID")
   Long defaultSchemeId;

   transient IPSLocationScheme m_defaultScheme;

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSPublishingContext#getDescription()
    */
   public String getDescription()
   {
      return description;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSPublishingContext#setDescription(java.lang.String)
    */
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   /**
    * Get the hibernate version information for this object.
    * 
    * @return returns the version, may be <code>null</code>.
    */
   @IPSXmlSerialization(suppress=true)
   public Integer getVersion()
   {
      return version;
   }

   /**
    * Set the hibernate version information for this object.
    * 
    * @param version The version to set.
    * 
    * @throws IllegalStateException if an attempt is made to set a previously
    * set version to a non-<code>null</code> value.
    */
   public void setVersion(Integer version)
   {
      if (this.version != null && version != null)
         throw new IllegalStateException("Version can only be set once");
      
      this.version = version;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSPublishingContext#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.CONTEXT, id);
   }  

   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#setGUID(IPSGuid)
    */
   public void setGUID(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid may not be null");
      
      if (id != -1L)
         throw new IllegalStateException("guid can only be set once");
      
      id = guid.longValue();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSPublishingContext#getName()
    */
   public String getName()
   {
      return name;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSPublishingContext#setName(java.lang.String)
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      this.name = name;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSPublishingContext#getDefaultSchemeId()
    */
   public IPSGuid getDefaultSchemeId()
   {
      return defaultSchemeId == null ? null : new PSGuid(
            PSTypeEnum.LOCATION_SCHEME, defaultSchemeId);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSPublishingContext#setDefaultSchemeId(com.percussion.services.sitemgr.IPSLocationScheme)
    */
   public void setDefaultSchemeId(IPSGuid schemeId)
   {
      if (schemeId == null)
      {
         this.defaultSchemeId = null;
         m_defaultScheme = null;
      }
      else
      {
         this.defaultSchemeId = schemeId.longValue();
         if (m_defaultScheme != null
               && (!m_defaultScheme.getGUID().equals(schemeId)))
         {
            m_defaultScheme = null;
         }
      }
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (! (obj instanceof PSPublishingContext) )
         return false;
      PSPublishingContext pb = (PSPublishingContext) obj;
      
      EqualsBuilder builder = new EqualsBuilder()
         .append(description, pb.description)
         .append(name, pb.name)
         .append(id, pb.id)
         .append(defaultSchemeId, pb.defaultSchemeId);

      return builder.isEquals();
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(name).toHashCode();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this).toString();
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   @Override
   public Object clone() throws CloneNotSupportedException
   {
      return super.clone();
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSPublishingContext#getId()
    */
   public Integer getId()
   {
      return (int) id;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSPublishingContext#setId(java.lang.Integer)
    */
   public void setId(Integer id)
   {
      this.id = id;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSPublishingContext#getDefaultScheme()
    */
   @IPSXmlSerialization(suppress = true)
   public IPSLocationScheme getDefaultScheme()
   {
      return m_defaultScheme;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSPublishingContext#setDefaultScheme(com.percussion.services.sitemgr.IPSLocationScheme)
    */
   public void setDefaultScheme(IPSLocationScheme defaultScheme)
   {
      if (defaultScheme == null)
         throw new IllegalArgumentException("defaultScheme may not be null.");
      
      setDefaultSchemeId(defaultScheme.getGUID());
      this.m_defaultScheme = defaultScheme;
   }
   
   /**
    * Restores this object's state from its XML representation (string).  See
    * {@link #toXML()} for format of XML.  See
    * {@link IPSCatalogItem#fromXML(String)} for more info on method
    * signature.
    */
   public void fromXML(String xmlsource) throws IOException, SAXException,
      PSInvalidXmlException
   {
      PSStringUtils.notBlank(xmlsource, "xmlsource may not be null or empty");
      
      Reader r = new StringReader(xmlsource);
      Document doc = PSXmlDocumentBuilder.createXmlDocument(r, false);
      NodeList nodes = doc.getElementsByTagName(XML_NODE_NAME);
      if (nodes.getLength() == 0)
      {
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING,
               XML_NODE_NAME);
      }
      
      Element elem = (Element) nodes.item(0);
      String schemeId = PSXmlUtils.checkAttribute(elem, 
            XML_ATTR_SCHEME_ID_NAME, false);
      if (schemeId.length() > 0)
         setDefaultSchemeId(new PSGuid(schemeId));
      else
         setDefaultSchemeId(null);
      
      String descr = PSXmlUtils.checkAttribute(elem, XML_ATTR_DESCR_NAME,
            false);
      if (descr.length() > 0)
         setDescription(descr);
      else
         setDescription(null);
            
      int idAttr = PSXmlUtils.checkAttributeInt(elem, XML_ATTR_ID_NAME, true);
      setId(new Integer(idAttr));
      
      String nameAttr = PSXmlUtils.checkAttribute(elem, XML_ATTR_NAME, true);
      setName(nameAttr);
   }
   
   /**
    * Serializes this object's state to its XML representation as a string.  The
    * format is:
    * <pre><code>
    * &lt;!ELEMENT PSXPublishingContext>
    * &lt;!ATTLIST PSXPublishingContext
    *    default-scheme-id CDATA
    *    description CDATA
    *    id CDATA #REQUIRED
    *    name CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * See {@link IPSCatalogItem#toXML()} for more info.
    */
   public String toXML()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      
      Element root = doc.createElement(XML_NODE_NAME);
      
      if (defaultSchemeId != null)
      {
         root.setAttribute(XML_ATTR_SCHEME_ID_NAME, 
               getDefaultSchemeId().toString());
      }
       
      if (description != null)
         root.setAttribute(XML_ATTR_DESCR_NAME, description);
            
      root.setAttribute(XML_ATTR_ID_NAME, String.valueOf(id)); 
      root.setAttribute(XML_ATTR_NAME, name);
      doc.appendChild(root);
      
      return PSXmlDocumentBuilder.toString(doc);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSPublishingContext#copy(com.percussion.services.sitemgr.IPSPublishingContext)
    */
   public void copy(IPSPublishingContext other)
   {
      if (other == null)
         throw new IllegalArgumentException("other may not be null.");
      if (!(other instanceof PSPublishingContext))
         throw new IllegalArgumentException(
               "other must be instance of PSPublishingContext.");
      PSPublishingContext src = (PSPublishingContext) other;
      name = src.name;
      description = src.description;
      defaultSchemeId = src.defaultSchemeId;
      m_defaultScheme = src.m_defaultScheme;
   }
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXPublishingContext";
      
   // private XML constants
   private static final String XML_ATTR_SCHEME_ID_NAME = "default-scheme-id";
   private static final String XML_ATTR_DESCR_NAME = "description";
   private static final String XML_ATTR_ID_NAME = "id";
   private static final String XML_ATTR_NAME = "name";
}
