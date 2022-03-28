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
