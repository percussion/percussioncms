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
package com.percussion.services.publisher.data;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSEditionContentList;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.utils.xml.IPSXmlErrors;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.utils.xml.PSXmlUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

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
 * Configuration that associates a content list with an edition.
 * 
 * @author dougrand
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSEditionContentList")
@Table(name = "RXEDITIONCLIST")
public class PSEditionContentList implements IPSCatalogItem, 
IPSEditionContentList, Cloneable
{
   @EmbeddedId
   PSEditionContentListPK pk;
   
   @Basic
   Integer sequence;
   
   @Basic
   Integer authtype;
   
   @Basic
   int context;
   
   @Basic
   @Column(name = "ASSEMBLY_CONTEXT")
   Integer assemblyContext;

   /**
    * Default constructor, needed for hibernate.
    */
   @SuppressWarnings("unused")
   private PSEditionContentList()
   {
   }
   
   /**
    * Minimal constructor.
    * 
    * @param id The unique ID of the created object, may not be
    * <code>null</code>.
    */
   public PSEditionContentList(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      pk = new PSEditionContentListPK();
      pk.setEditionclistid(id.longValue());
   }
   
   /**
    * Creates an object from the given IDs.
    * 
    * @param id the unique ID of the created object, never <code>null</code>.
    * @param editionId the Edition ID, never <code>null</code>.
    * @param clistId the Content List ID, never <code>null</code>.
    */
   public PSEditionContentList(IPSGuid id, IPSGuid editionId, IPSGuid clistId)
   {
      pk = new PSEditionContentListPK();
      pk.setEditionclistid(id.longValue());
      pk.setEditionid(editionId.longValue());
      pk.setContentlistid(clistId.longValue());
   }
   
   public IPSGuid getGUID()
   {
      return PSGuidUtils.makeGuid(pk.getEditionclistid(),
            PSTypeEnum.EDITION_CONTENT_LIST);
   }
   
   public IPSGuid getEditionId()
   {
      return PSGuidUtils.makeGuid(pk.getEditionid(),
            PSTypeEnum.EDITION);      
   }
   
   public IPSGuid getContentListId()
   {
      return PSGuidUtils.makeGuid(pk.getContentlistid(),
            PSTypeEnum.CONTENT_LIST);            
   }
      
   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionContentList#getAuthtype()
    */
   public Integer getAuthtype()
   {
      return authtype;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionContentList#setAuthtype(java.lang.Integer)
    */
   public void setAuthtype(Integer authtype)
   {
      this.authtype = authtype;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionContentList#getContext()
    */
   public IPSGuid getDeliveryContextId()
   {
      return new PSGuid(PSTypeEnum.CONTEXT, context);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionContentList#setContext(java.lang.Integer)
    */
   public void setDeliveryContextId(IPSGuid contextId)
   {
      if (contextId == null)
         throw new IllegalArgumentException("contextId may not be null.");

      this.context = contextId.getUUID();
   }

   public IPSGuid getAssemblyContextId()
   {
      if (assemblyContext != null)
         return new PSGuid(PSTypeEnum.CONTEXT,assemblyContext);
      else
         return null;
   }

   /**
    * @param assemblyContextId the assemblyContext to set
    */
   public void setAssemblyContextId(IPSGuid assemblyContextId)
   {
      if (assemblyContextId == null)
         this.assemblyContext = null;
      else
         this.assemblyContext = assemblyContextId.getUUID();
   }

   /**
    * @return the editionContentListPK
    */
   public PSEditionContentListPK getEditionContentListPK()
   {
      return pk;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionContentList#setEditionContentListPK(com.percussion.services.publisher.data.PSEditionContentListPK)
    */
   public void setEditionContentListPK(PSEditionContentListPK editionContentListPK)
   {
      this.pk = editionContentListPK;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionContentList#getSeq()
    */
   public Integer getSequence()
   {
      return sequence;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionContentList#setSeq(java.lang.Integer)
    */
   public void setSequence(Integer seq)
   {
      this.sequence = seq;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionContentList#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionContentList#hashCode()
    */
   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   /**
    * Copy properties of the given Edition/ContentList association to this 
    * object excluding repository IDs.
    */
   public void copy(IPSEditionContentList other)
   {
      setAssemblyContextId(other.getAssemblyContextId());
      setAuthtype(other.getAuthtype());
      setDeliveryContextId(other.getDeliveryContextId());
      setSequence(other.getSequence());
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionContentList#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
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
      String acId = PSXmlUtils.checkAttribute(elem, XML_ATTR_ASSMBLY_CTX_NAME,
            false);
      if (acId.trim().length() > 0)
         setAssemblyContextId(new PSGuid(acId));
      else
         setAssemblyContextId(null);
      
      int atId = PSXmlUtils.checkAttributeInt(elem, XML_ATTR_AUTHTYPE_NAME,
            false);
      if (atId != -1)
         setAuthtype(new Integer(atId));
      else
         setAuthtype(null);
            
      String dcId = PSXmlUtils.checkAttribute(elem, XML_ATTR_DLVRY_CTX_NAME,
            true);
      setDeliveryContextId(new PSGuid(dcId));
       
      String eclPk = PSXmlUtils.checkAttribute(elem, XML_ATTR_ECL_PK_NAME,
            true);
      try
      {
         String[] fields = eclPk.split(",");
         if (pk == null)
            pk = new PSEditionContentListPK();

         pk.setContentlistid((new Integer(fields[0])).longValue());
         pk.setEditionclistid((new Integer(fields[1])).longValue());
         pk.setEditionid((new Integer(fields[2])).longValue());
      }
      catch (Exception e)
      {
         Object[] args =
         {elem.getNodeName(), XML_ATTR_ECL_PK_NAME, eclPk};
         throw new PSInvalidXmlException(
               IPSXmlErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      
      int seq = PSXmlUtils.checkAttributeInt(elem, XML_ATTR_SEQUENCE_NAME,
            false);
      if (seq != -1)
         setSequence(new Integer(seq));
      else
         setSequence(null);
   }
   
   /**
    * Serializes this object's state to its XML representation as a string.  The
    * format is:
    * <pre><code>
    * &lt;!ELEMENT PSXEditionContentList>
    * &lt;!ATTLIST PSXEditionContentList
    *    assembly-context-id CDATA
    *    authtype CDATA
    *    delivery-context-id CDATA #REQUIRED
    *    edition-content-list-pk CDATA #REQUIRED
    *    sequence CDATA
    * >
    * </code></pre>
    *
    * Note: the edition-content-list-pk attribute value is a comma separated
    * list of id's in the following order: content list, edition content list,
    * edition.  See {@link IPSCatalogItem#toXML()} for more info.
    */
   public String toXML()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = doc.createElement(XML_NODE_NAME);
      
      if (assemblyContext != null)
      {
         root.setAttribute(XML_ATTR_ASSMBLY_CTX_NAME, 
            getAssemblyContextId().toString());
      }
      
      if (authtype != null)
         root.setAttribute(XML_ATTR_AUTHTYPE_NAME, getAuthtype().toString());
            
      root.setAttribute(XML_ATTR_DLVRY_CTX_NAME,
            getDeliveryContextId().toString());
           
      root.setAttribute(XML_ATTR_ECL_PK_NAME, 
         pk.contentlistid + "," + pk.editionclistid + "," + pk.editionid);
      
      if (sequence != null)
         root.setAttribute(XML_ATTR_SEQUENCE_NAME, sequence.toString());
      
      doc.appendChild(root);
      
      return PSXmlDocumentBuilder.toString(doc);
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#setGUID(com.percussion.utils.guid.IPSGuid)
    */
   public void setGUID(IPSGuid newguid)
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null");
      
      if (pk.isInitialized())
         throw new IllegalStateException("guid can only be set once");
            
      pk.editionclistid = newguid.longValue();
   }
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXEditionContentList";
   
   // private XML constants
   private static final String XML_ATTR_ASSMBLY_CTX_NAME =
      "assembly-context-id";
   private static final String XML_ATTR_AUTHTYPE_NAME = "authtype";
   private static final String XML_ATTR_DLVRY_CTX_NAME = "delivery-context-id";
   private static final String XML_ATTR_ECL_PK_NAME = "edition-content-list-pk";
   private static final String XML_ATTR_SEQUENCE_NAME = "sequence"; 
}
