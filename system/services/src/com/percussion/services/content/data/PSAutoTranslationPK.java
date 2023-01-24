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
package com.percussion.services.content.data;

import java.io.Serializable;
import java.util.Objects;

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
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSAutoTranslationPK)) return false;
      PSAutoTranslationPK that = (PSAutoTranslationPK) o;
      return getContentTypeId() == that.getContentTypeId() && Objects.equals(getLocale(), that.getLocale());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getContentTypeId(), getLocale());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSAutoTranslationPK{");
      sb.append("contentTypeId=").append(contentTypeId);
      sb.append(", locale='").append(locale).append('\'');
      sb.append('}');
      return sb.toString();
   }
}

