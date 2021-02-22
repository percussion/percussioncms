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
package com.percussion.services.filestorage.data;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * Stores base information about each unique binary in the system inclding its
 * hash and the core metadata.
 * @author stephenbolton
 *
 */
@Entity
@Table(name = "PSX_BINARY", uniqueConstraints =
{@UniqueConstraint(columnNames =
{"hash"})})
public class PSBinary implements Serializable
{

   public PSBinary()
   {

   }

   /**
    * Serial Version ID
    */
   private static final long serialVersionUID = 1L;

  
   /**
    * The logger for this class
    */
   private static Logger ms_logger = Logger.getLogger(PSBinary.class);

   /**
    * Unique id for the binary, uses the internal next number table to generate
    */
   @Id
   @GenericGenerator(name = "id", strategy = "com.percussion.data.utils.PSNextNumberHibernateGenerator")
   @GeneratedValue(generator = "id")
   @Column(name = "ID", nullable = false)
   int id;

   /**
    * The hash value for the binary
    */
   @Column(updatable = false, name = "HASH", nullable = false, length = 40)
   private String hash;

   /**
    * Reference to the table containing the binary content
    */
   @OneToOne(mappedBy = "binary", fetch = FetchType.LAZY, optional=false, orphanRemoval = true)
   @PrimaryKeyJoinColumn
   PSBinaryData data;

   /**
    * The metadata known for the item.
    */
   @LazyCollection(LazyCollectionOption.TRUE)
   @OneToMany(mappedBy = "binary", cascade = CascadeType.ALL)
   @Fetch(FetchMode.SELECT)
   // @JoinColumn( name="BINARY_ID", referencedColumnName="ID", nullable=false)
   Set<PSBinaryMetaEntry> metaEntries = new HashSet<>();

   /**
    * The mimetype for the item.  should not include encoding
    */
   @Column(name = "TYPE")
   String mimeType;

   /**
    * The encoding if approprate for the file.  Usually only for
    * textual based content
    */
   @Column(name = "ENCODING")
   String encoding;

   /**
    * The filename passed in when the item was originally created.
    * This will not be updated if the same file is attempted to be stored
    * with a different filename.  Mainly used for identification and
    * mimetype identification based upon extension
    */
   @Column(name = "ORIG_FILENAME")
   String originalFilename;

   /**
    * The size of the binary
    */
   @Column(name = "LENGTH")
   long length;

   /**
    * If there was any error in extracting the item.  The
    * error should be stored in the items metadata.
    */
   @Column(name = "EXTRACT_ERROR")
   private boolean extractError;

   /**
    * when this binary was last accessed or touched
    * This is only updated on a daily basis
    */
   @Column(name = "LAST_ACCESS")
   private Date lastAccessedDate;

   /**
    * Whether the item has been marked for re-extraction of the metadata.
    */
   @Column(name = "REPARSE_META")
   private boolean reparseMeta;


   
   public PSBinary(String hash, PSBinaryData data, Set<PSBinaryMetaEntry> metaEntries)
   {
      this.hash = hash;
      this.data = data;
      this.metaEntries = metaEntries;
      this.setData(data);
      this.setMetaEntries(metaEntries);
   }
   
   /**
    * @return the date last accessed set to midnight on the day
    */
   public Date getLastAccessedDate()
   {
      return lastAccessedDate;
   }

   /**
    * @param lastAccessedDate date passed in will be truncated to midnight of the day
    */
   public void setLastAccessedDate(Date lastAccessedDate)
   {
      // We only check if an item is accessed in a day.
      this.lastAccessedDate = DateUtils.truncate(lastAccessedDate, Calendar.DATE);
   }

   /**
    * The hibernate version
    */
   @Version
   @Column(name = "version")
   private Integer version;


   /**
    * @return the row id
    */
   public int getId()
   {
      return id;
   }

   /**
    * @param id the db row id
    */
   public void setId(int id)
   {
      this.id = id;
   }

   /**
    * @return the hash string
    */
   public String getHash()
   {
      return hash;
   }

   /**
    * @param hash the hash string
    */
   public void setHash(String hash)
   {
      this.hash = hash;
   }

   /**
    * @return The data entity containing the file stream
    */
   public PSBinaryData getData()
   {
      return data;
   }

   /**
    * @param data the data entity containing the file stream
    */
   public void setData(PSBinaryData data)
   {
      if (data!=null)
         data.setBinary(this);
      this.data = data;
   }

   /**
    * @return the mime type
    */ 
   public String getMimeType()
   {
      return mimeType;
   }

   /**
    * @param mimeType the mimetype
    */
   public void setMimeType(String mimeType)
   {
      this.mimeType = mimeType;
   }

   /**
    * @return the encoding
    */
   public String getEncoding()
   {
      return encoding;
   }

   /**
    * @param encoding the encoding
    */
   public void setEncoding(String encoding)
   {
      this.encoding = encoding;
   }

   /**
    * @return the original filename.
    */
   public String getOriginalFilename()
   {
      return originalFilename;
   }

   /**
    * @param originalFilename
    */
   public void setOriginalFilename(String originalFilename)
   {
      this.originalFilename = originalFilename;
   }

   /**
    * @return the file length
    */
   public long getLength()
   {
      return length;
   }

   /**
    * @param length the file length
    */
   public void setLength(long length)
   {
      this.length = length;
   }

   /**
    * @return a flag to indicate there was an error in tika metadata extraction
    */
   public boolean isExtractError()
   {
      return extractError;
   }

   /**
    * @param extractError if there was an error in extraction
    */
   public void setExtractError(boolean extractError)
   {
      this.extractError = extractError;
   }

   /**
    * @return the set of metadata entities
    */
   public Set<PSBinaryMetaEntry> getMetaEntries()
   {
      return metaEntries;
   }

   /**
    * @param metaEntries the set or metadata entities
    */
   public void setMetaEntries(Set<PSBinaryMetaEntry> metaEntries)
   {
      for (PSBinaryMetaEntry metaEntry : metaEntries)
      {
         metaEntry.setBinary(this);
         String value = metaEntry.getValue();
         if (value != null)
         {
            String keyName = metaEntry.getKey().getName();
            if (keyName.equals(Metadata.CONTENT_ENCODING))
            {
               this.setEncoding(metaEntry.getValue());
            }
            else if (keyName.equals(Metadata.CONTENT_LENGTH))
            {
               this.setLength(NumberUtils.toLong(metaEntry.getValue()));
            }
            else if (keyName.equals(Metadata.CONTENT_TYPE))
            {
               // Sometimes metadata includes encoding, we want to strip this
               // off e.g. text/plain; charset=UTF-;
               String type = StringUtils.substringBefore(metaEntry.getValue(), ";");
               this.setMimeType(type);
            }
            else if (keyName.equals(Metadata.RESOURCE_NAME_KEY))
            {
               this.setOriginalFilename(metaEntry.getValue());
            }
            else if (keyName.equals(PSMeta.PARSE_ERROR))
            {
               this.setExtractError(true);
            }

         }
      }

      this.metaEntries = metaEntries;
   }

   /**
    * @return is the binary marked for re-extraction tika metadata.
    */
   public boolean isReparseMeta()
   {
      return reparseMeta;
   }

   /**
    * @param reparseMeta set the binary for re-extractio of tika metadata
    */
   public void setReparseMeta(boolean reparseMeta)
   {
      this.reparseMeta = reparseMeta;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((data == null) ? 0 : data.hashCode());
      result = prime * result + ((encoding == null) ? 0 : encoding.hashCode());
      result = prime * result + (extractError ? 1231 : 1237);
      result = prime * result + ((hash == null) ? 0 : hash.hashCode());
      result = prime * result + id;
      result = prime * result + ((lastAccessedDate == null) ? 0 : lastAccessedDate.hashCode());
      result = prime * result + (int) (length ^ (length >>> 32));
      result = prime * result + ((metaEntries == null) ? 0 : metaEntries.hashCode());
      result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
      result = prime * result + ((originalFilename == null) ? 0 : originalFilename.hashCode());
      result = prime * result + (reparseMeta ? 1231 : 1237);
      result = prime * result + ((version == null) ? 0 : version.hashCode());
      return result;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PSBinary other = (PSBinary) obj;
      if (data == null)
      {
         if (other.data != null)
            return false;
      }
      else if (!data.equals(other.data))
         return false;
      if (encoding == null)
      {
         if (other.encoding != null)
            return false;
      }
      else if (!encoding.equals(other.encoding))
         return false;
      if (extractError != other.extractError)
         return false;
      if (hash == null)
      {
         if (other.hash != null)
            return false;
      }
      else if (!hash.equals(other.hash))
         return false;
      if (id != other.id)
         return false;
      if (lastAccessedDate == null)
      {
         if (other.lastAccessedDate != null)
            return false;
      }
      else if (!lastAccessedDate.equals(other.lastAccessedDate))
         return false;
      if (length != other.length)
         return false;
      if (metaEntries == null)
      {
         if (other.metaEntries != null)
            return false;
      }
      else if (!metaEntries.equals(other.metaEntries))
         return false;
      if (mimeType == null)
      {
         if (other.mimeType != null)
            return false;
      }
      else if (!mimeType.equals(other.mimeType))
         return false;
      if (originalFilename == null)
      {
         if (other.originalFilename != null)
            return false;
      }
      else if (!originalFilename.equals(other.originalFilename))
         return false;
      if (reparseMeta != other.reparseMeta)
         return false;
      if (version == null)
      {
         if (other.version != null)
            return false;
      }
      else if (!version.equals(other.version))
         return false;
      return true;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "PSBinary [id=" + id + ", hash=" + hash + ", data=" + data + ", metaEntries=" + metaEntries
            + ", mimeType=" + mimeType + ", encoding=" + encoding + ", originalFilename=" + originalFilename
            + ", length=" + length + ", extractError=" + extractError + ", lastAccessedDate=" + lastAccessedDate
            + ", reparseMeta=" + reparseMeta + ", version=" + version + "]";
   }

}
