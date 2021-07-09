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
