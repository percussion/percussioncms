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
