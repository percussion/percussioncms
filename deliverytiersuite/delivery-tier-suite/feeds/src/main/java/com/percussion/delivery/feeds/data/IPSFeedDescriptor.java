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
package com.percussion.delivery.feeds.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A feed descriptor contains meta data needed to create a feed.
 */
@JsonDeserialize(as = PSFeedDescriptor.class)
public interface IPSFeedDescriptor
{
    /**
     * 
     * @return the name of the feed.
     */
    public String getName();
    
    /**
     * 
     * @return the name of the site the feed belongs to.
     */
    public String getSite();
    
    /**
     * 
     * @return the feed title.
     */
    public String getTitle();
    
    /**
     * 
     * @return the feed description.
     */
    public String getDescription();
    
    /**
     * 
     * @return the link to the page the feed represents.
     */
    public String getLink();
    
    /**
     * 
     * @return the query to get the feed data from the meta-data service.
     */
    public String getQuery();
    
    /**
     * 
     * @return the feed output type. Never <code>null</code>.
     */
    public String getType();



}
