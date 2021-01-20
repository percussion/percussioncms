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
package com.percussion.fastforward.relationship;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSRelationshipProcessor;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;

/**
 * Uses the assembly service and relationship api to query relationships.
 * 
 * @author James Schultz
 * @since 6.0
 */
public class PSChildRelationshipParser extends PSChildRelationshipBase
{
   /**
    * Creates an instance of this class that will use the specified processor
    * for querying and updating relationships.
    * 
    * @param relProcessor processor for querying and updating relationships, not
    *           <code>null</code>
    */
   public PSChildRelationshipParser(IPSRelationshipProcessor relProcessor)
   {
      super(relProcessor);
   }

   /**
    * Extracts the content ids from the owners of relationships in the specified
    * slot with the specified item as the dependent.
    * 
    * @param childId id of the item that must be the dependent of relationship
    * @param slotName name of the slot to be queried, not blank.
    * @return ids of relationship owners from the specified slot with the
    *         specified child id, or <code>null</code> if there are no
    *         matching relationships
    * @throws PSAssemblyException propagated from assembly service, if there are
    *            problems loading the slot
    * @throws PSCmsException propagated from relationship API, if there are
    *            problems querying relationships
    */
   public List<Integer> parse(int childId, String slotName)
         throws PSAssemblyException, PSCmsException
   {
      if (StringUtils.isBlank(slotName))
         throw new IllegalArgumentException("slotName may not be blank");

      IPSTemplateSlot slot = findSlot(slotName);
      PSRelationshipSet relationships = getRelationships(childId, slot);
      if (relationships != null)
      {
         List<Integer> ownerContentIds = extractOwnerIds(relationships);
         return ownerContentIds;
      }

      // a null return means do not change field value
      return null;
   }

}
