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

import com.percussion.services.filestorage.IPSFileMeta;
import com.percussion.services.filestorage.PSBinaryServiceException;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.metadata.ClimateForcast;
import org.apache.tika.metadata.CreativeCommons;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Geographic;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.MSOffice;
import org.apache.tika.metadata.Message;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TIFF;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.metadata.TikaMimeKeys;

/**
 * An adaptor class wrapping the Tika metadata object and providing
 * some getters and setters for the most important metadata values; mimetype,
 * encoding, original filename, and length.
 * @author stephenbolton
 *
 */
/**
 * @author stephenbolton
 *
 */
public class PSMeta
      implements
         CreativeCommons,
         DublinCore,
         Geographic,
         HttpHeaders,
         Message,
         MSOffice,
         ClimateForcast,
         TIFF,
         TikaMetadataKeys,
         TikaMimeKeys,
         IPSFileMeta,
         Serializable,
         Map<String, String>

{

   /**
    * version id for serialization
    */
   private static final long serialVersionUID = 1L;

   /**
    * Logger for this class
    */
   private transient static final Logger log = LogManager.getLogger(PSMeta.class);

   /*
    * A key to store in the metadata map the hash value for the item.
    */
   private static final String HASH_KEY = "HASH";
   
   /*
    * A key to store any error parsing the metadata with Tika.  
    */
   public static final String PARSE_ERROR = "PARSE_ERROR";

   private TreeMap<String, String> properties = new TreeMap<>((String.CASE_INSENSITIVE_ORDER));

   /**
    * Constructor creating the base metadata object from the main properties.
    * These are are used to help Tika with mimetype detection.  These values are not
    * required and only some may be available.  Tika may also modify encoding and content
    * type if it finds a correct or more acurate value.  The extension of originalFilename
    * is also used to help identify mimetype but tika still get the correct mimetype if the
    * extesion is not correct.
    * 
    * @param contentType  The mimetype
    * @param originalFilename The filename used when uploading the file originally
    * @param encoding  The encoding if appropriate e.g. textual format.
    * @param length the size.
    */
   public PSMeta(String contentType, String originalFilename, String encoding, int length)
   {
      setMimeType(contentType);
      setOriginalFilename(originalFilename);
      setEncoding(encoding);
   }

   
   /**
    * Create this object based upon a Tika Metadata object
    * @param metadata
    */
   public PSMeta(Metadata metadata)
   {
      setTikaMetadata(metadata);
   }

   public PSMeta()
   {

   }

   /**
    * Update the hashmap with values from a tika metadata object
    * @param metadata  The tika Metadata object
    */
   public void setTikaMetadata(Metadata metadata)
   {
      // some attributes come through with a different case. We want to match these. Also
      // Db Collation is case insensitive by default
      this.properties = new TreeMap<>((String.CASE_INSENSITIVE_ORDER));;
      for (String name : metadata.names())
      {
         String[] values = metadata.getValues(name);
         if (values.length > 0)
         {
            // Multiple values are normally duplicates found rather
            // than an actual multi value property.  We will always just look
            // at first
            String value = values[0];
            if (value != null)
            {
               // May not be required on new version of tika.
               String cleanValue = values[0].replaceAll("[\\x00]", "");
               this.properties.put(name, cleanValue);
            }
            else
            {
               log.debug("Metadata value for " + name + " is null");
            }
         }
      }
   }

   
   /**  Convert this object to a Tika metadata object
    * @return
    */
   public Metadata getTikaMetadata()
   {

      Metadata tikaMeta = new Metadata();
      for (Entry<String, String> entry : this.properties.entrySet())
      {
         tikaMeta.add(entry.getKey(), entry.getValue());
      }
      return tikaMeta;
   }

   /* (non-Javadoc)
    * @see java.util.Map#clear()
    */
   public void clear()
   {
      properties.clear();
   }

   /* (non-Javadoc)
    * @see java.util.Map#containsKey(java.lang.Object)
    */
   public boolean containsKey(Object arg0)
   {
      return properties.containsKey(arg0);
   }

   /**
    * Does this object contain the specified key.
    * @param arg0
    * @return
    */
   public boolean containsKey(String arg0)
   {
      return properties.containsKey(arg0);
   }

   /* (non-Javadoc)
    * @see java.util.Map#containsValue(java.lang.Object)
    */
   public boolean containsValue(Object arg0)
   {
      return properties.containsValue(arg0);
   }

   /* (non-Javadoc)
    * @see java.util.Map#entrySet()
    */
   public Set<Entry<String, String>> entrySet()
   {
      return properties.entrySet();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileMeta#get(java.lang.String)
    */
   public String get(String arg0)
   {
      return properties.get(arg0);

   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return properties.hashCode();
   }

   /* (non-Javadoc)
    * @see java.util.Map#isEmpty()
    */
   public boolean isEmpty()
   {
      return properties.isEmpty();
   }

   /* (non-Javadoc)
    * @see java.util.Map#keySet()
    */
   public Set<String> keySet()
   {
      return properties.keySet();
   }

   /* (non-Javadoc)
    * @see java.util.Map#put(java.lang.Object, java.lang.Object)
    */
   public String put(String arg0, String arg1)
   {
      return properties.put(arg0, arg1);
   }

   /* (non-Javadoc)
    * @see java.util.Map#putAll(java.util.Map)
    */
   public void putAll(Map<? extends String, ? extends String> arg0)
   {
      properties.putAll(arg0);
   }

   /* (non-Javadoc)
    * @see java.util.Map#remove(java.lang.Object)
    */
   public String remove(Object arg0)
   {
      return properties.remove(arg0);
   }

   /* (non-Javadoc)
    * @see java.util.Map#size()
    */
   public int size()
   {
      return properties.size();
   }

   /* (non-Javadoc)
    * @see java.util.Map#values()
    */
   public Collection<String> values()
   {
      return properties.values();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "Meta [properties=" + properties + "]";
   }

   /* (non-Javadoc)
    * @see java.util.Map#get(java.lang.Object)
    */
   public String get(Object key)
   {
      return (key instanceof String) ? this.get(((String) key)) : null;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileMeta#getMimeType()
    */
   public String getMimeType()
   {
      return StringUtils.substringBefore(properties.get(HttpHeaders.CONTENT_TYPE), ";");
   }

   /**
    * @param mimeType
    */
   public void setMimeType(String mimeType)
   {
      if (mimeType != null)
         // Sometimes metadata includes encoding, we want to strip this off e.g.
         // text/plain; charset=UTF-;
         properties.put(HttpHeaders.CONTENT_TYPE, StringUtils.substringBefore(mimeType, ";"));
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileMeta#getEncoding()
    */
   public String getEncoding()
   {
      return properties.get(HttpHeaders.CONTENT_ENCODING);
   }

   /**
    * Set the encoding string
    * @param encoding
    */
   public void setEncoding(String encoding)
   {
      if (encoding != null)
         properties.put(HttpHeaders.CONTENT_ENCODING, encoding);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileMeta#getOriginalFilename()
    */
   public String getOriginalFilename()
   {

      return properties.get(Metadata.RESOURCE_NAME_KEY);
   }

   /**
    * Set the originalFilename
    * @param originalFilename
    */
   public void setOriginalFilename(String originalFilename)
   {
      if (originalFilename != null)
         properties.put(Metadata.RESOURCE_NAME_KEY, originalFilename);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileMeta#getLength()
    */
   public long getLength()
   {
      String metaLength = properties.get(HttpHeaders.CONTENT_LENGTH);
      return NumberUtils.toLong(metaLength);
   }

   /**
    * Set the length.
    * @param length
    */
   public void setLength(long length)
   {
      properties.put(HttpHeaders.CONTENT_LENGTH, String.valueOf(length));
   }

   /**
    * @param hash
    * @throws PSBinaryServiceException
    */
   public void setHash(String hash) throws PSBinaryServiceException
   {
      String currentHash = getHash();
      if (currentHash != null)
      {
         throw new PSBinaryServiceException("Cannot reset hash value for binary");
      }
      else
      {
         properties.put(HASH_KEY, hash);
      }

   }

   /**
    * Get the hash stored in the metadata.
    * @return
    */
   public String getHash()
   {
      return properties.get(HASH_KEY);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSFileMeta#getParseError()
    */
   public String getParseError()
   {
      return properties.get(PARSE_ERROR);
   }

   /**
    * Get the parse Error can be <code>null</code>
    * @param error
    */
   public void setParseError(String error)
   {
      properties.put(PARSE_ERROR, error);
   }

   /**
    * Is there a parse error stored in this object
    * @return true if parse error.
    */
   public boolean hasParseError()
   {
      return properties.get(PARSE_ERROR) != null;
   }

}
