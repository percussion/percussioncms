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
