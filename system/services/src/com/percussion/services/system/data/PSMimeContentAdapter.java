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
import java.util.Objects;

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

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSMimeContentAdapter)) return false;
      PSMimeContentAdapter that = (PSMimeContentAdapter) o;
      return m_href == that.m_href && m_contentLength == that.m_contentLength && Objects.equals(m_name, that.m_name) && Objects.equals(m_mimeType, that.m_mimeType) && Objects.equals(m_characterEncoding, that.m_characterEncoding) && Objects.equals(m_transferEncoding, that.m_transferEncoding) && Objects.equals(m_content, that.m_content) && Objects.equals(m_description, that.m_description) && Objects.equals(m_guid, that.m_guid);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_href, m_name, m_mimeType, m_contentLength, m_characterEncoding, m_transferEncoding, m_content, m_description, m_guid);
   }
}

