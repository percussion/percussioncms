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

package com.percussion.soln.linkback.utils;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class finds content summaries for content item ids.
 * 
 * Taken from the PSO Toolkit.
 * 
 * @author adamgent
 * 
 */
public class ItemSummaryFinder {

    /**
     * Logger for this class
     */
    private static final Logger log = LogManager.getLogger(ItemSummaryFinder.class);

    private IPSCmsContentSummaries cmsContentSummaries = null;

    public ItemSummaryFinder() {
        super();
    }

    /**
     * Programmatic constructor
     * 
     * @param cmsContentSummaries
     *            - CMS content summaries service.
     */
    public ItemSummaryFinder(IPSCmsContentSummaries cmsContentSummaries) {
        super();
        this.cmsContentSummaries = cmsContentSummaries;
    }

    public PSLocator getCurrentOrEditLocator(IPSGuid guid) throws PSException {
        int id = guid.getUUID();
        return getCurrentOrEditLocator(id);
    }

    public PSLocator getCurrentOrEditLocator(String contentId) throws PSException {
        if (StringUtils.isBlank(contentId) || !StringUtils.isNumeric(contentId)) {
            String emsg = "Content id must be numeric " + contentId;
            log.error(emsg);
            throw new IllegalArgumentException(emsg);
        }
        int id = Integer.parseInt(contentId);
        return getCurrentOrEditLocator(id);
    }

    public PSLocator getCurrentOrEditLocator(int id) throws PSException {
        PSComponentSummary cs = getSummary(id);
        PSLocator loc = cs.getCurrentLocator();
        if (cs.getEditLocator() != null && cs.getEditLocator().getRevision() > 0) {
            loc = cs.getEditLocator();
            log.debug("Using edit locator" + loc);
        }
        return loc;
    }

    public static final int CHECKOUT_NONE = 1;

    public static final int CHECKOUT_BY_ME = 2;

    public static final int CHECKOUT_BY_OTHER = 3;

    public int getCheckoutStatus(String contentId, String userName) throws PSException {
        if (StringUtils.isBlank(userName)) {
            String emsg = "User name must not be blank";
            log.error(emsg);
            throw new IllegalArgumentException(emsg);
        }
        PSComponentSummary sum = getSummary(contentId);
        String uname = sum.getCheckoutUserName();
        if (StringUtils.isBlank(uname)) {
            return CHECKOUT_NONE;
        }
        if (userName.equalsIgnoreCase(uname)) {
            return CHECKOUT_BY_ME;
        }
        return CHECKOUT_BY_OTHER;
    }

    /**
     * Gets the component summary for an item.
     * 
     * @param contentId
     *            the content id
     * @return the component summary. Never <code>null</code>.
     * @throws PhotoGalleryException
     *             when the item does not exist
     */
    public PSComponentSummary getSummary(String contentId) throws PSException {
        if (StringUtils.isBlank(contentId) || !StringUtils.isNumeric(contentId)) {
            String emsg = "Content id must be numeric " + contentId;
            log.error(emsg);
            throw new IllegalArgumentException(emsg);
        }
        int id = Integer.parseInt(contentId);
        return getSummary(id);
    }

    public PSComponentSummary getSummary(IPSGuid guid) throws PSException {
        int id = guid.getUUID();
        return getSummary(id);
    }

    public PSComponentSummary getSummary(int id) throws PSException {

        PSComponentSummary sum = getCmsContentSummaries().loadComponentSummary(id);
        if (sum == null) {
            String emsg = "Content item not found " + id;
            log.error(emsg);
            throw new PSException(emsg);
        }
        return sum;
    }

    public IPSCmsContentSummaries getCmsContentSummaries() {
        return cmsContentSummaries;
    }

    public void setCmsContentSummaries(IPSCmsContentSummaries cmsContentSummaries) {
        this.cmsContentSummaries = cmsContentSummaries;
    }

}
