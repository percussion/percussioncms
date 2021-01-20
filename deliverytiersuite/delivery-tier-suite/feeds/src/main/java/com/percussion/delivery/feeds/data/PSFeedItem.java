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

import java.util.Date;

/**
 * @author erikserating
 *
 */
public class PSFeedItem
{
   private String title;
   private String description;
   private Date publishDate;
   private String link;
   
/**
 * @return the title
 */
public String getTitle()
{
    return title;
}
/**
 * @param title the title to set
 */
public void setTitle(String title)
{
    this.title = title;
}
/**
 * @return the description
 */
public String getDescription()
{
    return description;
}
/**
 * @param description the description to set
 */
public void setDescription(String description)
{
    this.description = description;
}
/**
 * @return the publishDate
 */
public Date getPublishDate()
{
    return publishDate;
}
/**
 * @param publishDate the publishDate to set
 */
public void setPublishDate(Date publishDate)
{
    this.publishDate = publishDate;
}
/**
 * @return the link
 */
public String getLink()
{
    return link;
}
/**
 * @param link the link to set
 */
public void setLink(String link)
{
    this.link = link;
}
   
   
}
