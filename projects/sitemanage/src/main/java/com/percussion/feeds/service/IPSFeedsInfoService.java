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
package com.percussion.feeds.service;

import com.percussion.feeds.data.PSFeedInfo;
import com.percussion.feeds.error.PSFeedInfoServiceException;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;

import java.util.Collection;

/**
 * @author erikserating
 *
 */
public interface IPSFeedsInfoService
{
    /**
     * Retrieve all feed information objects for the specified site.
     * @param siteid , uuid must be specified.
     * @return list of feed info objects, never <code>null</code>, may be empty.
     * @throws <code>PSFeedInfoServiceException</code> if any error occurs.
     */
    public Collection<PSFeedInfo> getFeeds(long serverid) throws PSFeedInfoServiceException;
    
    /**
     * Retrieves all feed information objects for the specified site, creates the feeds descriptors
     * then pushes the descriptors onto the feeds info queue. The queue will then push the descriptors to the
     * feeds service on the delivery tier.
     * @param site the site object, cannot be <code>null</code>.
     * @throws <code>PSFeedInfoServiceException</code> if any error occurs.
     */
    public void pushFeeds(IPSSite site, PSPubServer server) throws PSFeedInfoServiceException;
}
