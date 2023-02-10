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
package com.percussion.services.publisher.data;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Embedded primary key class for the edition content list.
 * 
 * @author dougrand
 */
@Embeddable
public class PSEditionContentListPK implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 379367292031206623L;

   long editionclistid = -1L;

   long editionid;

   long contentlistid;

   /**
    * @return the contentlistid
    */
   public long getContentlistid()
   {
      return contentlistid;
   }

   /**
    * @param contentlistid the contentlistid to set
    */
   public void setContentlistid(long contentlistid)
   {
      this.contentlistid = contentlistid;
   }

   /**
    * @return the editionclistid
    */
   public long getEditionclistid()
   {
      return editionclistid;
   }

   /**
    * @param editionclistid the editionclistid to set
    */
   public void setEditionclistid(long editionclistid)
   {
      this.editionclistid = editionclistid;
   }

   /**
    * @return the editionid
    */
   public long getEditionid()
   {
      return editionid;
   }

   /**
    * @param editionid the editionid to set
    */
   public void setEditionid(long editionid)
   {
      this.editionid = editionid;
   }

   /**
    * Determines if the object has been initialized with a valid id.
    * 
    * @return <code>true</code> if the object has been initialized,
    * <code>false</code> otherwise.
    */
   public boolean isInitialized()
   {
      return editionclistid != -1L;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSEditionContentListPK)) return false;
      PSEditionContentListPK that = (PSEditionContentListPK) o;
      return getEditionclistid() == that.getEditionclistid() && getEditionid() == that.getEditionid() && getContentlistid() == that.getContentlistid();
   }

   @Override
   public int hashCode() {
      return Objects.hash(getEditionclistid(), getEditionid(), getContentlistid());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSEditionContentListPK{");
      sb.append("editionclistid=").append(editionclistid);
      sb.append(", editionid=").append(editionid);
      sb.append(", contentlistid=").append(contentlistid);
      sb.append('}');
      return sb.toString();
   }
}
