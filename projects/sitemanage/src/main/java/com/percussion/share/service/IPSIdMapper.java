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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.share.service;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;

/**
 * Used to handle translation of id's between {@link IPSGuid} and their
 * respective string representations.
 */
public interface IPSIdMapper
{
	
	
	/**
	 * Returns a GUID from a contentid
	 * @param id  A valid content id
	 * @return a valid guid for the supplied id
	 */
	public IPSGuid getGuidFromContentId(long id);
	
   /**
    * Performs string to {@link IPSGuid} translation.
    * 
    * @param id the string representation of the id, never blank.
    * 
    * @return the id as an {@link IPSGuid}, never <code>null</code>.
    */
   public IPSGuid getGuid(String id);
   
   /**
    * Gets the content ID from an item GUID.
    * @param guid the item GUID, not <code>null</code>.
    * @return the content ID.
    */
   public int getContentId(IPSGuid guid);
   
   /**
    * Gets the content ID from the string representation of an item GUID.
    * @param guid the string representation of an item GUID, not blank.
    * @return the content ID.
    */
   public int getContentId(String guid);
   
   /**
    * Convert string to {@link IPSGuid} for an item ID. 
    * @param id the item ID, never blank.
    * @return the item GUID with "correct" version number, never <code>null</code>.
    */
   public IPSGuid getItemGuid(String id);
   
   /**
    * Performs {@link IPSGuid} to string translation.
    * 
    * @param id as an {@link IPSGuid}, never <code>null</code>.
    * 
    * @return the string representation of the id, never <code>null</code>.
    */
   public String getString(IPSGuid id);
   
   /**
    * Calls {@link #getGuid(String)} for a list of string.
    * 
    * @param ids list of IDs in string representation, not <code>null</code> 
    * 
    * @return a list of converted {@link IPSGuid} objects.
    */
   public List<IPSGuid> getGuids(List<String> ids);
   
   /**
    * Calls {@link #getString(IPSGuid)} for a list of {@link IPSGuid}
    * 
    * @param ids list of IDs in {@link IPSGuid} objects, not <code>null</code> 
    * 
    * @return a list of string representation of the IDs.
    */
   public List<String> getStrings(List<IPSGuid> ids);
   
   /**
    * Performs {@link IPSGuid} to string translation for the specified
    * locator.
    * 
    * @param locator the {@link PSLocator}, never <code>null</code>.
    * 
    * @return the string representation of the locator's id, never
    *  <code>null</code>.
    */
   public String getString(PSLocator locator);
   
   /**
    * Performs {@link IPSGuid} translation for the specified
    * locator.
    * 
    * @param locator the {@link PSLocator}, never <code>null</code>.
    * 
    * @return the id as an {@link IPSGuid}, never <code>null</code>.
    */
   public IPSGuid getGuid(PSLocator locator);
   
   /**
    * Performs string to {@link PSLocator} translation.
    * 
    * @param id the string representation of the id, never blank.
    * 
    * @return the id as a Locator, never <code>null</code>.  Includes updated revision.
    */
   public PSLocator getLocator(String id);
   
   /**
    * Performs {@link IPSGuid} to {@link PSLocator} translation.
    * 
    * @param id as an {@link IPSGuid}, never <code>null</code>.
    * 
    * @return the guid as a Locator, never <code>null</code>.  Includes updated revision.
    */
   public PSLocator getLocator(IPSGuid id);
   
   /**
    * Generates a new, unique id which can be used for local content items.  The id is generated from next number
    * table's "PSX_LOCAL_CONTENT" value.
    * 
    * @return the id, never <= 0.
    */
   public int getLocalContentId();
 
}
