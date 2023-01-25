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
package com.percussion.services.contentmgr.impl.legacy;

import com.percussion.services.contentmgr.impl.IPSTypeKey;

import java.util.Objects;


/**
 * Represents a content type for lookup in the configuration
 * 
 * @author dougrand
 */
public class PSContentTypeKey implements IPSTypeKey
{   
   /**
    * Id for the content type matches the id in
    * {@link com.percussion.cms.objectstore.server.PSItemDefManager}
    */
   private long m_contentTypeId;
   
   /**
    * Ctor
    * @param ct a content type id
    */
   public PSContentTypeKey(long ct)
   {
      m_contentTypeId = ct;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSContentTypeKey)) return false;
      PSContentTypeKey that = (PSContentTypeKey) o;
      return m_contentTypeId == that.m_contentTypeId;
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_contentTypeId);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      StringBuilder s = new StringBuilder(40);
      s.append("<" + getClass().getCanonicalName());
      s.append("contentType=");
      s.append(m_contentTypeId);
      s.append(" >");
      return s.toString();
   }

   public long getContentType()
   {
      return m_contentTypeId;
   }
   
   
}
