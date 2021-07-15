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
package com.percussion.services.content.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Primary key for the {@link PSAutoTranslation}.
 */
@Embeddable
public class PSAutoTranslationPK implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Column(name = "CONTENTTYPEID", nullable = false)
   private long contentTypeId;
   
   @Column(name = "LOCALE", nullable = false)
   private String locale;
   
   /**
    * Default ctor
    */
   public PSAutoTranslationPK()
   {
      
   }
   
   /**
    * Construct a primary key
    * 
    * @param cTypeId The content type id
    * @param lang The locale's language string, may not be <code>null</code> or 
    * empty.
    */
   public PSAutoTranslationPK(long cTypeId, String lang)
   {
      if (StringUtils.isBlank(lang))
         throw new IllegalArgumentException("lang may not be null or empty");
      
      contentTypeId = cTypeId;
      locale = lang;
   }

   /**
    * Get the content type id of this auto translation
    * 
    * @return The id
    */
   public long getContentTypeId()
   {
      return contentTypeId;
   }

   /**
    * Set the content type id of this auto translation
    * 
    * @param id The id
    */   
   public void setContentTypeId(long id)
   {
      contentTypeId = id;
   }

   /**
    * Get the locale code of this auto translation.
    * 
    * @return the locale code, never <code>null</code> or empty.
    */
   public String getLocale()
   {
      return locale;
   }
   
   /**
    * Set a new locale code for this auto translation.
    * 
    * @param lang the new locale code, not <code>null</code> or
    *    empty.
    */
   public void setLocale(String lang)
   {
      if (StringUtils.isBlank(lang))
         throw new IllegalArgumentException(
            "locale cannot be null or empty");
      
      locale = lang;
   }

   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }
}

