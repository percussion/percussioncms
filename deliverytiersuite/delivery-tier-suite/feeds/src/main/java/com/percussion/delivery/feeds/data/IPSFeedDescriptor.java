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
