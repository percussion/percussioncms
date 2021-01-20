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
package com.percussion.delivery.likes.services;

/**
 * The likes service is used to store, total and show total rating - "likes"
 * only. It will run in the delivery tier.
 * 
 * @author davidpardini
 * 
 */
public interface IPSLikesService
{

    /**
     * Returns the total number of likes for the given URL, site and type
     * 
     * @param type
     * @param likeId
     * @param site
     * 
     * @return int total
     */
    public int getTotalLikes(String site, String likeId, String type);

    /**
     * Increments the total number of likes for the given URL and returns the
     * number of total likes
     * 
     * @param type
     * @param likeId
     * @param site
     * 
     * @return int total
     */
    public int like(String site, String likeId, String type);

    /**
     * Decrements the total number of likes for the given URL and returns the
     * number of total likes
     * 
     * @param type
     * @param likeId
     * @param site
     * 
     * @return int total
     */
    public int unlike(String site, String likeId, String type);

    /**
     * After a site rename in CM1, this method needs to be called to
     * update the current likes for a page so that they are pointed
     * to the new site.
     * @param prevSiteName the old site name.
     * @param newSiteName the new site name.
     */
    public void updateLikesForSiteAfterRename(String prevSiteName, String newSiteName);

}
