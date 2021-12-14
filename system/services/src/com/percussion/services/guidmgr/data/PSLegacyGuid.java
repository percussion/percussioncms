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
package com.percussion.services.guidmgr.data;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.xml.IPSXmlSerialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class allows the creation of pseudo guids from the legacy content store.
 * These are not globally unique, but are rather unique to a single database.
 * However, they are useful to maintain a single set of apis for the future.
 * This class will be deprecated in the future.
 * <P>
 * <EM>Important</EM>: These GUIDs may not be used across JVM invocations as
 * the child guids can resolve to different objects. Please keep this in mind!
 * This means that they may not be stored. Another caveat, these guids are 
 * invalid after content type changes, so they should not be stored even in 
 * memory across such changes.
 * 
 * @author dougrand
 */
public class PSLegacyGuid extends PSGuid
{
   /**
    * 
    */
   private static final long serialVersionUID = -3200949933035613891L;

   /**
    * Holds the last child id allocated. Note that the algorithm involved
    * assumes only in-memory reference for these guids. Access to the methods
    * that manipulate this value must be synchronized.
    * <p>
    * NOTE: the initial value must be greater then 0 or the constructor 
    * {@link #PSLegacyGuid(long)} may fail when converted through webservices.
    */
   private static int ms_childId = 1;

   /**
    * Holds information about what tuples of contenttypeid + childid have
    * already been given an in-memory id
    */
   private static Map<List<Number>,Long> ms_childIdsAllocated = 
      new HashMap<>();
   
   /**
    * Holds the reverse mapping from a given child id to a key. This is
    * used to obtain the original information back from the child id.
    */
   private static Map<Long,List<Number>> ms_keysFromIds = 
      new HashMap<>();

   /**
    * Create a legacy guid for a content item. The revision uses the site slot,
    * the contentid uses the uuid slot.
    * 
    * @param contentid the contentid of the item
    * @param revision the revision of the item. It may be <code>-1</code> if
    *    the revision of the item is undefined.
    */
   public PSLegacyGuid(int contentid, int revision) 
   {
      if (revision == -1) revision = UNDEFINED_REVISION;
      assemble(revision, PSTypeEnum.LEGACY_CONTENT, contentid);
   }
   
   /**
    * Create a legacy guid for a content child. The content type and
    * 
    * @param contenttypeid a reference to the content type id from the content
    *           editor
    * @param childid a reference to the mapper id from the content editor for
    *           the specific child
    * @param sysid the primary key for the child object
    */
   public PSLegacyGuid(long contenttypeid, int childid, int sysid) {
      long virtualsite = mapChildType(contenttypeid, childid);
      assemble(virtualsite, PSTypeEnum.LEGACY_CHILD, sysid);
   }

   /**
    * Creates a legacy guid from regular guid.
    * 
    * @param guid the regular guild, never <code>null</code>.
    */
   @SuppressWarnings("cast")
   public PSLegacyGuid(PSGuid guid)
   {
      if (guid.getType() != PSTypeEnum.LEGACY_CONTENT
            .getOrdinal() && guid.getType() != PSTypeEnum.LEGACY_CHILD.getOrdinal())
      {
         throw new IllegalArgumentException(
               "guid type must be either LEGACY_CONTENT or LEGACY_CHILD.");
      }
      PSTypeEnum type = (guid.getType() == PSTypeEnum.LEGACY_CONTENT
            .getOrdinal())
            ? PSTypeEnum.LEGACY_CONTENT
            : PSTypeEnum.LEGACY_CHILD;
      assemble(guid.getHostId(), type, (long)guid.getUUID());
   }
   
   /**
    * Reconstitute a legacy guid. If the supplied value does not have a type,
    * {@link PSTypeEnum#LEGACY_CONTENT} will be used. If it does not have a
    * revision (hostid), a value representing an undefined revision will be
    * used.
    * 
    * @param value If a type is provided, then it must be either
    * {@link PSTypeEnum#LEGACY_CONTENT} or {@link PSTypeEnum#LEGACY_CHILD}.
    */
   public PSLegacyGuid(long value)
   {
      m_guid = value;
      if (getType() == 0)
         setType(PSTypeEnum.LEGACY_CONTENT.getOrdinal());
      else
      {
         if (getType() != PSTypeEnum.LEGACY_CONTENT.getOrdinal()
               && getType() != PSTypeEnum.LEGACY_CHILD.getOrdinal())
         {
            throw new IllegalArgumentException(
               "Only LEGACY_CHILD and LEGACY_CONTENT types are supported by this class.");
         }
      }
      if (getHostId() == 0)
         setHostId(UNDEFINED_REVISION);
   }
   
   /**
    * Constructs a legacy GUID from its string representation.
    * 
    * @param raw the string representation of an legacy GUID. Never blank
    */
   public PSLegacyGuid(String raw)
   {
      super(raw);
   }
   
   /**
    * Retrieve the mapping from a given tuple of contenttypeid and childid to
    * an id that acts as a placeholder in guids.
    * @param contenttypeid the content type id
    * @param childid the child id
    * @return always returns the allocated temporary id to use
    */
   private long mapChildType(long contenttypeid, int childid)
   {
      List<Number> key = new ArrayList<>();
      key.add(contenttypeid);
      key.add(childid);
      Long rval = null;
      
      synchronized(ms_childIdsAllocated)
      {
         rval = ms_childIdsAllocated.get(key);
         if (rval == null)
         {
            rval = new Long(ms_childId++);
            ms_childIdsAllocated.put(key, rval);
            ms_keysFromIds.put(rval, key);
         }
      }
      
      return rval.longValue();
   }
   
   /**
    * Retrieve original contenttypeid and child id for a given stored id.
    * @param id the allocated id for the tuple
    * @return the original key or <code>null</code> if the key is not found
    */
   private Number[] getKeyFromId(long id)
   {
      Number key[] = null;
      
      synchronized(ms_childIdsAllocated)
      {
         List<Number> list = ms_keysFromIds.get(new Long(id));
         if (list != null)
            key = list.toArray(new Number[list.size()]);
      }
      
      return key;
   }
   
   /**
    * Construct a legacy guid from the passed locator.
    * 
    * @param locator a locator, never <code>null</code>.
    */
   public PSLegacyGuid(PSLocator locator)
   {
      this(locator.getId(), locator.getRevision());      
   }
   
   /**
    * Determine if this guid represents a child. This is determined by what
    * ctor was originally called.
    * @return <code>true</code> if so
    */
   public boolean isChildGuid()
   {
      return getType() == PSTypeEnum.LEGACY_CHILD.getOrdinal();
   }
   
   /**
    * Get the original content type id value
    * @return the original value
    */
   @IPSXmlSerialization(suppress=true)
   public long getContentTypeId() 
   {
      Number keys[] = getKeyFromId(getHostId());
      if (keys == null)
      {
         throw new IllegalStateException("Trying to retrieve content type " +
               "from unknown child id " + getHostId());
      }
      return keys[0].longValue();
   }
   
   /**
    * Get the original child mapper id value
    * @return the original value
    */
   @IPSXmlSerialization(suppress=true)
   public int getChildId() 
   {
      Number keys[] = getKeyFromId(getHostId());
      if (keys == null)
      {
         throw new IllegalStateException("Trying to retrieve child mapper id " +
               "from unknown child id " + getHostId());
      }
      return keys[1].intValue();
   }   
   
   /**
    * Get the content id
    * @return the original content id
    */
   @IPSXmlSerialization(suppress=true)
   public int getContentId()
   {
      return getUUID();
   }
   
   /**
    * Get the revision id
    * 
    * @return the original revision id. It is <code>-1</code> if the revision
    *    is undefined.
    */
   @IPSXmlSerialization(suppress=true)
   public int getRevision()
   {
      int hostId = (int) getHostId();
      
      return hostId == UNDEFINED_REVISION ? -1 : hostId;
   }

    @IPSXmlSerialization(suppress=true)
    public void setRevision(int revisionId)
    {
        setHostId(revisionId);
    }

   
   /**
    * undefined revision number. 
    */
   public static int UNDEFINED_REVISION = (int)BIT24;

   /**
    * Create a locator from the legacy guid. The guid must be a item
    * guid and not a child guid
    * @return a valid locator, never <code>null</code>
    */
   public PSLocator getLocator()
   {
      if (isChildGuid())
      {
         throw new UnsupportedOperationException("Cannot extract locator for child");
      }
      return new PSLocator(getContentId(), getRevision());
   }
}
