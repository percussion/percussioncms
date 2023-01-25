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
package com.percussion.services.contentchange;

import com.percussion.services.contentchange.data.PSContentChangeEvent;
import com.percussion.services.contentchange.data.PSContentChangeType;
import com.percussion.share.dao.IPSGenericDao;

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
   void contentChanged(PSContentChangeEvent changeEvent) throws IPSGenericDao.SaveException;

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
