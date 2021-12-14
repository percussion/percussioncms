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
package com.percussion.services.guidmgr;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;

/**
 * Create globally unique ids for use when creating service objects. The methods
 * that begin with 'create' are generally only useful to the system.
 * Implementers will be interested in the methods that begin with 'make'.
 * 
 * There are also methods for converting between old-style item locators and
 * guids. These are provided for interoperability between the old and new
 * models.
 * 
 * @author dougrand
 */
/**
 * @author stephenbolton
 *
 */
public interface IPSGuidManager
{
   /**
    * Create a single new guid for the given type
    * 
    * @param type
    *           the type, never <code>null</code>
    * @return a new guid
    */
   IPSGuid createGuid(PSTypeEnum type);

   /**
    * Create a series of guids for the given type
    * 
    * @param type
    *           the type, never <code>null</code>
    * @param count
    *           the number of guids to create, must be a positive number
    * @return a list of guids, the size of the list will equal count
    */
   List<IPSGuid> createGuids(PSTypeEnum type, int count);

   /**
    * Create a single new guid for the given type and repository
    * 
    * @param repositoryId
    *           the repository id, must be greater than zero. Zero is the same
    *           as not using a repository. Negative is invalid.
    * @param type
    *           the type, never <code>null</code>
    * @return a new guid
    */
   IPSGuid createGuid(byte repositoryId, PSTypeEnum type);

   /**
    * Create a series of guids for the given type and repository
    * 
    * @param repositoryId
    *           the repository id, must be greater than zero. Zero is the same
    *           as not using a repository. Negative is invalid.
    * @param type
    *           the type, never <code>null</code>
    * @param count
    *           the number of guids to create, must be a positive number
    * @return a list of guids, the size of the list will equal count
    */
   List<IPSGuid> createGuids(byte repositoryId, PSTypeEnum type, int count);
   
   /**
    * Create a single id using values stored in the next number table.
    * 
    * @param key the key, never <code>null</code> or empty
    * @return the next allocated id
    */
   int createId(String key);
   
   /**
    * Allocate the next id for a given type and return only the 64 bit id. 
    * Most guids are only using the lower 32 bits, which is not always adequate
    * for longer lived data for non design objects.
    * 
    * @param type the type, provides the index into the saved next numbers
    * for guids, never <code>null</code>.
    * @return the next long id.
    */
   long createLongId(PSTypeEnum type);
   
   /**
    * Allocate a block of ids from the values stored in the next number table.
    * 
    * @param key the key, never <code>null</code> or empty
    * @param blocksize the block size to allocate, must be a positive integer
    * @return an array of allocated ids
    */
   int[] createIdBlock(String key, int blocksize);

   /**
    * Get the persisted host identifier. The host id is calculated internally
    * and is a cryptographic random number, which makes it unlikely that any
    * two host ids will conflict. The actual size of the random number is 2^24.
    * <p>
    * The host id is used to make two references from different machines not
    * collide. The special host id value of <code>0</code> indicates that the
    * guid is not a true guid and will likely collide across servers
    * 
    * @return a valid host id 
    */
   long getHostId();
   
   /**
    * Recreates a guid instance from a value originally obtained from
    * {@link IPSGuid#longValue()} or from a uuid. 
    * 
    * @param raw This value may or may not contain the type id. If it does,
    * then it must match the supplied <code>type</code>, otherwise, the supplied
    * type is used as the type for the new guid.
    *  
    * @param type the type, never <code>null</code>
    * @return a guid of the specified type built from the specified raw value,
    * never <code>null</code>.
    */
   IPSGuid makeGuid(long raw, PSTypeEnum type);
   
   /**
    * Recreates a guid instance from a human readable form of the guid.
    * 
    * @param raw Never <code>null</code> or empty. The generic format of the
    * supplied string is of the form: hostid-typeid-uuid (e.g. 10-103-125). A
    * single long value that is supported by {@link #makeGuid(long, PSTypeEnum)}
    * can also be supplied, in which case, the rules defined in that method must
    * be followed. Two different represenations are allowed: hostid-uuid,
    * hostid-typeid-uuid. If a typeid is supplied, it must match that of the
    * <code>type</code> param, otherwise, the supplied type is used. If the
    * type is {@link PSTypeEnum#LEGACY_CONTENT} or
    * {@link PSTypeEnum#LEGACY_CHILD}, the human readable forms are not
    * supported.
    * 
    * @param type the type, never <code>null</code>
    * @return a guid of the specified type built from the specified raw value,
    * never <code>null</code>.
    */
   IPSGuid makeGuid(String raw, PSTypeEnum type);  
   
   /**
    * Create a legacy guid from a locator
    * 
    * @param loc the locator for a content item, never <code>null</code>
    * @return a guid of the {@link PSTypeEnum#LEGACY_CONTENT} type, never
    * <code>null</code>.
    * 
    * @see #makeLocator(IPSGuid)
    */
   IPSGuid makeGuid(PSLocator loc);
   
   /**
    * Converts a legacy guid back into the locator format. This method is
    * provided for interoperability between the old and new id models.
    * 
    * @param guid Never <code>null</code>. Must have a type of
    * {@link PSTypeEnum#LEGACY_CONTENT}.
    * 
    * @return The locator for the item. The revision may or may not be set
    * depending on whether it was present in the supplied <code>guid</code>.
    * 
    * @see #makeGuid(PSLocator)
    */
   PSLocator makeLocator(IPSGuid guid);
   
   /**
    * Create a GUID from its string representation of the GUID.
    * 
    * @param raw the string representation of the GUID, not blank.
    * 
    * @return the converted object, never <code>null</code>.
    */
   IPSGuid makeGuid(String raw);
   
   /**
    * Extracts the content ids from a set of legacy guids. Non-content guids
    * passed will cause an exception to be thrown.
    * 
    * @param guids a list of guids, never <code>null</code> or empty, and must
    *           contain only content guids
    * @return a list of content ids, equal in length to the input list and in
    *         the same order.
    */
   List<Integer> extractContentIds(List<IPSGuid> guids);

   /**
    * Transactional method to update the nextn number in the db
    * @param key
    * @param blocksize
    * @return
    */
   public int updateNextNumber(String key, int blocksize, long setValue );

   /**
    * Set the next number value transactionally as long as it is more than current value
    * @param key
    * @param value
    * @return the original next number that would have been returned
    */
   int fixNextNumber(String key, int value);

   /** Transactional method to update the next long in the db
    * @param key
    * @return
    */
   long updateNextLong(Integer key);
  
   /**
    *  Transactional method to load the lost id
    */
   void loadHostId();
   
   int peekNextNumber(String nnkey);
}
