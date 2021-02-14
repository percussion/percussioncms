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
package com.percussion.comments.service;

import com.percussion.comments.data.PSComment;
import com.percussion.comments.data.PSCommentModeration;
import com.percussion.comments.data.PSCommentsSummary;

import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * @author davidpardini
 * 
 */
public interface IPSCommentsService 
{
    /**
     * Provides a list of all pages with comments for the given site.
     * 
     * @return a list of summary of pages, sorted by name (ascendant order)
     *         (per-server), never <code>null</code>, may be empty.
     */
    public List<PSCommentsSummary> getPagesWithComments(@PathParam("site") String site, @QueryParam("max") Integer max,
            @QueryParam("start") Integer start);

    /**
     * Provides a summary of the comment information for the given page.  The count information is combined from all
     * delivery servers.
     * 
     * @param id of the page, never blank.
     * 
     * @return the comment summary information for the page, never <code>null</code>.
     */
    public PSCommentsSummary getCommentsSummary(String id);
    
    /**
     * Provides a list of count info only for all pages with comments for the given site.
     * 
     * @param siteName The name of the site, not <code>null<code/> or empty.
     * 
     * @return a list of summaries w/out page specific info (counts and path only), never <code>null</code>, may be empty. 
     */
    List<PSCommentsSummary> getCommentCountsForSite(String siteName);
    
    /**
     * 
     * @param site The name of the site containing the page.
     * @param pagePath The path of the desired page in the form /Sites/sitename/.../page.html
     * @param max Unused
     * @param start Unused
     * @return All comments on the requested page. Never <code>null</code>.
     */
    List<PSComment> getCommentsOnPage(@PathParam("site") String site, @PathParam("url") String pagePath,
            @QueryParam("max") Integer max, @QueryParam("start") Integer start);

    /**
     * Approves or rejects comments according to the PSCommentModeration object.
     * 
     * @param commentModeration An object that contains information about comments
     * and the site which they belong to. Must not be <code>null</code>.
     */
    public void moderate(String site,PSCommentModeration commentModeration);
    
    /***
     * When set the specified license # will be passed in as an override to any
     * underlying calls. Intended for Unit Testers. 
     * @param licenseId
     */
    public void setLicenseOverride(String licenseId);
    
    /**
     * Returns the current license override if any. 
     * @return "" or a value, should never be null
     */
    public String getLicenseOverride();
}
