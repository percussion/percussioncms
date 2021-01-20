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
package com.percussion.services.contentchange;

import com.percussion.services.contentchange.data.PSContentChangeEvent;
import com.percussion.services.contentchange.data.PSContentChangeType;

import java.util.List;

/**
 * Track changes to items, used for incremental publishing
 * 
 * @author JaySeletz
 *
 */
public interface IPSContentChangeService
{

   /**
    * Called to store a change with the service
    * 
    * @param changeEvent The change to store, not <code>null</code>.
    */
   void contentChanged(PSContentChangeEvent changeEvent);

   /**
    * Get all stored changes for the supplied parameters.
    * 
    * @param siteId 
    * @param pendingLive
    * 
    * @return The changes, not <code>null</code>, may be empty.
    */
   List<Integer> getChangedContent(long siteId, PSContentChangeType changeType);

   /**
    * Delete stored changes for the supplied paramaters.
    * 
    * @param siteId The id of the site, or -1 to ignore siteid
    * @param contentId
    * @param pendingLive
    */
   void deleteChangeEvents(long siteId, int contentId, PSContentChangeType changeType);
   

   /**
    * Delete stored changes for the supplied paramaters.
    * 
    * @param siteId The id of the site, or -1 to ignore siteid
    */
   void deleteChangeEventsForSite(long siteId);
   
   /**
    * Delete stored changes for the supplied paramaters.
    * 
    * @param siteId The id of the site, or -1 to ignore siteid
    */
   void deleteChangeEventsForSite(long siteId, PSContentChangeType changeType);
   
   /**
    * Add a change handler.  When content items are modified in the system, each handler is 
    * notified of changes to content items, and when notified may either store or remove change 
    * events using this service.
    * @param handler
    */
   void addContentChangeHander(IPSContentChangeHandler handler);
}
