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

import com.percussion.services.guidmgr.data.PSLegacyGuid;

import java.io.Serializable;

/**
 * Serializable id class that is used for Hibernate to identify generated class
 * records. Note that the naming of the accessor methods doesn't follow the
 * normal coding practices, but is necessary to match property names in the
 * hibernate configuration.
 * 
 * @author dougrand
 */
public class PSLegacyCompositeId implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = -2025905370029396658L;

   private int sys_contentid;

   private int sys_revision;

   /**
    * No arg constructor required for Hibernate interceptor
    */
   public PSLegacyCompositeId()
   {
      
   }
   
   /**
    * Public ctor
    * 
    * @param cid content id
    * @param rev revision
    */
   public PSLegacyCompositeId(int cid, int rev) {
      sys_contentid = cid;
      sys_revision = rev;
   }

   /**
    * Ctor
    * 
    * @param legacyguid guid, never <code>null</code>
    */
   public PSLegacyCompositeId(PSLegacyGuid legacyguid) {
      if (legacyguid == null)
      {
         throw new IllegalArgumentException("legacyguid may not be null");
      }
      sys_contentid = legacyguid.getContentId();
      sys_revision = legacyguid.getRevision();
   }

   /**
    * @return Returns the content_id.
    */
   public int getSys_contentid()
   {
      return sys_contentid;
   }

   /**
    * @param content_id The content_id to set.
    */
   public void setSys_contentid(int content_id)
   {
      sys_contentid = content_id;
   }

   /**
    * @return Returns the revision.
    */
   public int getSys_revision()
   {
      return sys_revision;
   }

   /**
    * @param revision The revision to set.
    */
   public void setSys_revision(int revision)
   {
      sys_revision = revision;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      PSLegacyCompositeId b = (PSLegacyCompositeId) obj;
      return (sys_contentid == b.sys_contentid) && (sys_revision == b.sys_revision);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return sys_contentid * 10 + sys_revision;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "<composite-key content_id = " + sys_contentid + " revision = "
            + sys_revision + ">";
   }

}
