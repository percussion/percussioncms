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

package com.percussion.services.touchitem;

import com.percussion.design.objectstore.PSRelationship;
import com.percussion.server.IPSRequestContext;
import com.percussion.utils.guid.IPSGuid;

/**
 * Touches Items for relationship changes particularly 
 * folder relationships.
 * Item's need to be touched so that incremental publishing
 * works properly.
 * <p>
 * This services is called by Relationship and Workflow Extensions.
 * 
 * @author agent
 * @see PSTouchItemConfiguration
 */
public interface IPSTouchItemService
{
   
   /**
    * Touches the items that may be impacted by the
    * {@link PSRelationship#getDependent() dependent item} of the
    * <code>relationship</code>.  This could be an item or folder.
    * Impacted items are based on {@link PSTouchItemConfiguration}. 
    * 
    * @param relationship the relationship, not null.
    * @param context the request, not null.
    * @return the number of items that were touched.
    * @see PSTouchItemConfiguration
    */
   int touchItems(IPSRequestContext context, PSRelationship relationship);
   
   /**
    * Touches items that may be impacted by the given item (id).
    * The determination of which items are impacted (and thus touched) are based on 
    * {@link PSTouchItemConfiguration}.
    * 
    * @param id item id can be any item type except folder, never null.
    * @return the number of items that were touched.
    * @see PSTouchItemConfiguration
    */
   int touchItems(IPSGuid id);
   
   /**
    * Updates site items based on a folder relationship change.
    * This is to make incremental publishing pick up folder changes.
    * 
    * @param requestContext not null.
    * @param relationship should be a folder relationship and not null.
    * @see PSTouchItemConfiguration
    */
   void updateSiteItems(IPSRequestContext requestContext, PSRelationship relationship);
   
   /**
    * The current configuration for the touch item server.
    * 
    * @return should never be null.
    * @see PSTouchItemConfiguration
    */
   public PSTouchItemConfiguration getConfiguration();
   
   /**
    * See Getter.
    * @param config should never be null.
    * @see #getConfiguration()
    */
   public void setConfiguration(PSTouchItemConfiguration config);
}
