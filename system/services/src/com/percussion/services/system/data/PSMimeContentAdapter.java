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
package com.percussion.services.system.data;

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.util.IOTools;
import com.percussion.util.PSCharSetsConstants;
import com.percussion.utils.guid.IPSGuid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.xml.sax.SAXException;

/**
 * This mime content adapter is mainly used for file transport but can handle
 * any type of content.
 */
public class PSMimeContentAdapter implements Serializable, IPSCatalogSummary, 
   IPSCatalogItem
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 6520345876079600993L;
   
   /**
    * This references an attachment in webservice calls for this content.
    * If -1 then the content is transferred with this objects xml 
    * representation, otherwise the content for this object is transferred as 
    * attachment.
    */
   private long m_href = -1;
   
   /**
    * The name for this content is typically a file name but it may be an
    * unstructured descriptive name as well.
    */
   private String m_name = null;
   
   /**
    * The mime type of this content, defaults to 
    * <code>application/octet-stream</code>.
    */
   private String m_mimeType = IPSMimeContentTypes.MIME_TYPE_OCTET_STREAM;
   
   /**
    * The length of this content, -1 if unknown.
    */
   private long m_contentLength = -1;
   
   /**
    * The character encoding of this content, defaults to <code>UTF-8</code>.
    */
   private String m_characterEncoding = PSCharSetsConstants.rxStdEnc();
   
   /**
    * The transfer encoding for this content, <code>null</code> if an 
    * attachment id is supplied, defaults to 
    * {@link IPSMimeContentTypes#MIME_ENC_BASE64}.
    */
   private String m_transferEncoding = IPSMimeContentTypes.MIME_ENC_BASE64;
   
   /**
    * The input stream to the content that this object represents.
    */
   private InputStream m_content = null;
   
   /**
    * A description about the content, may be <code>null</code> or empty.
    */
   private String m_description = null;
   
   /**
    * The guid of this adapter, may be <code>null</code> if not set.
    */
   private IPSGuid m_guid = null;
   
   /**
    * Get the id that references the content as attachment.
    * 
    * @return the attachment id, < 0 if the content is contained in this object.
    */
   public long getAttachmentId()
   {
      return m_href;
   }
   
   /**
    * Set the new attchment id.
    * 
    * @param href the new attachment id, < 0 to indicate that the
    *    content is transferred with this object and not as attachment, 
    *    otherwise any content already set on this object is cleared
    */
   public void setAttachmentId(long href)
   {
      m_href = href;
      m_content = null;
   }
   
   /**
    * Is the content of this object transferred as attachment?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isContentAttached()
   {
      return m_href >= 0;
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getName()
    */
   public String getName()
   {
      if (m_name == null)
         throw new IllegalStateException("setName() was never called");
      
      return m_name;
   }
   
   /**
    * Set the name of this content.
    * 
    * @param name The name to set, never <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      m_name = name;
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getLabel()
    */
   public String getLabel()
   {
      return getName();
   }
   
   /**
    * Get the mime type of the content.
    * 
    * @return The mime type of this content, defaults to 
    * {@link IPSMimeContentTypes#MIME_TYPE_OCTET_STREAM}, never 
    * <code>null</code> or empty.
    */
   public String getMimeType()
   {
      return m_mimeType;
   }
   
   /**
    * Set the new mime type for this content.
    * 
    * @param mimeType The mime type, may not be <code>null</code> or empty.
    */
   public void setMimeType(String mimeType)
   {
      if (StringUtils.isBlank(mimeType))
         throw new IllegalArgumentException(
            "mimeType may not be null or empty");
      m_mimeType = mimeType;
   }

   /**
    * Get the content length.
    * 
    * @return the length, -1 if not known.
    */
   public long getContentLength()
   {
      return m_contentLength;
   }
   
   /**
    * Set the content length.
    * 
    * @param length The length, may not be < -1
    */
   public void setContentLength(long length)
   {
      if (length < -1)
         throw new IllegalArgumentException("length may not be < -1");

      m_contentLength = length;
   }
   
   /**
    * Get the character encoding.
    * 
    * @return the encoding, defaults to {@link PSCharSetsConstants#rxStdEnc()}
    * Never <code>null</code> or empty.
    */
   public String getCharacterEncoding()
   {
      return m_characterEncoding;
   }
   
   /**
    * Set the character encoding.
    * 
    * @param encoding The encoding, may not be <code>null</code> or empty.
    */
   public void setCharacterEncoding(String encoding)
   {
      if (StringUtils.isBlank(encoding))
         throw new IllegalArgumentException(
            "encoding may not be null or empty");
      
      m_characterEncoding = encoding;
   }
   
   /**
    * Get the transfer encoding.
    * 
    * @return The encoding, or <code>null</code> if 
    * {@link #isContentAttached()} is <code>true</code>.
    */
   public String getTransferEncoding()
   {
      return m_transferEncoding;
   }
   
   /**
    * Set the transfer encoding
    * 
    * @param encoding The encoding, may be <code>null</code> if 
    * {@link #isContentAttached()} is <code>true</code>, defaults to 
    * {@link IPSMimeContentTypes#MIME_ENC_BASE64}
    */
   public void setTransferEncoding(String encoding)
   {
      m_transferEncoding = encoding;
   }
   
   /* (non-Javadoc)
    * @see IPSCatalogSummary#getDescription()
    */
   public String getDescription()
   {
      return null;
   }

   /**
    * Get the content.
    * 
    * @return An input stream to the content, or <code>null</code> if 
    * {@link #isContentAttached()} is <code>true</code>.
    */
   public InputStream getContent()
   {
      InputStream content = m_content;
      if (content == null && !isContentAttached())
         content = new ByteArrayInputStream(new byte[0]);
      
      return content;
   }
   
   /**
    * Set the content.
    * 
    * @param content The content, may be <code>null</code> if an attachment
    * id has been supplied.
    */
   public void setContent(InputStream content)
   {
      if (content == null && m_href == -1)
         throw new IllegalArgumentException(
            "content may not be null if an attachment id is not specified.");
      
      m_content = content;
      m_href = -1;
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      if (m_guid == null)
         throw new IllegalStateException("guid has not been set");
      
      return m_guid;
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#setGUID(IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null");
      
      if (m_guid != null)
         throw new IllegalStateException("guid has already been set");
      
      if (newguid.getType() != PSTypeEnum.CONFIGURATION.getOrdinal())
         throw new IllegalArgumentException("invalid guid type");
      
      m_guid = newguid;
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#fromXML(String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }

   /**
    * This equals is implemented for testing purposes as it reads from the 
    * contents input stream in order to compare contents, and resets the input
    * stream using a ByteArrayInputStream - since the object is technically 
    * modified, this should not be considered usable in general.
    */
   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof PSMimeContentAdapter == false)
      {
         return false;
      }
      if (this == obj)
      {
         return true;
      }
      PSMimeContentAdapter other = (PSMimeContentAdapter) obj;
      boolean isEqual = new EqualsBuilder()
         .append(m_characterEncoding, other.m_characterEncoding)
         .append(m_contentLength, other.m_contentLength)
         .append(m_description, other.m_description)
         .append(m_href, other.m_href)
         .append(m_mimeType, other.m_mimeType)
         .append(m_name, other.m_name)
         .append(m_transferEncoding, other.m_transferEncoding)
         .append(m_guid, other.m_guid)
         .isEquals();

      // handle content
      if (m_content == null ^ other.m_content == null)
         isEqual = false;

      if (isEqual && m_content != null)
      {
         ByteArrayOutputStream outThis = new ByteArrayOutputStream();
         ByteArrayOutputStream outOther = new ByteArrayOutputStream();
         try
         {
            IOTools.copyStream(m_content, outThis);
            m_content = new ByteArrayInputStream(outThis.toByteArray());
            IOTools.copyStream(other.m_content, outOther);
            other.m_content = new ByteArrayInputStream(outOther.toByteArray());
            isEqual = outThis.toString(m_characterEncoding).equals(
               outOther.toString(other.m_characterEncoding));            
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }
      
      return isEqual;
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }
}

