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

package com.percussion.utils.guid;

import java.io.Serializable;
/**
 * Guids are abstract references to internal objects. A guid generally is a 
 * globally unique identifier. For Rhythmyx, they are global by benefit of a 
 * site id that is assigned by Percussion to each user's installation and a
 * type id defined for each supported object type. 
 * <p>
 * Guids with a site id of <code>0</code> are reserved for development use.
 * 
 * @author dougrand
 */
public interface IPSGuid extends Serializable
{
   /**
    * Convert a numeric guid into a user readable form with no type integer.
    * Appropriate for times where the type is implied.
    * 
    * @return a formatted string, never <code>null</code> or empty.
    */
   public abstract String toStringUntyped();

   /**
    * Gets the host id, which indicates what customer installation created the
    * object this GUID references. Each customer should have a unique host id,
    * which is an important part of keeping these identifiers globally unique.
    * 
    * @return the host id component of the guid.
    */
   public abstract long getHostId();

   /**
    * Return the type of the GUID, the interpretation of the type depends on
    * the context.
    * 
    * @return the type ordinal used for this guid.
    */
   public abstract short getType();

   /**
    * Return the UUID of the GUID.
    * 
    * @return the uuid without host and type information.
    */
   public abstract int getUUID();

   /**
    * Get the guid value in raw form. This is suitable for storage in 
    * serialized objects or in the database.
    * <p>
    * If there is no hostid, then the GUID was constructed from an old id in 
    * the database. For example, you have a template with a template id of 319. 
    * When that becomes a GUID internally, it has the type added to it. If 
    * longValue() (which is used when finding a template from the GUID) 
    * doesn't strip everything but the UUID, the value won't match the value 
    * in the database. On the other hand, if the GUID is a new GUID, then the 
    * value in the database will be the complete guid, and it is appropriate 
    * for longValue() to return m_guid.
    * 
    * @return the guid with the details as described above.
    */
   public abstract long longValue();
}
