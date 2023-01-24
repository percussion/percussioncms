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

package com.percussion.comments.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents a list of comments that belongs to the specified
 * site.
 * 
 * @author miltonpividori
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "site",
    "comments"
})
public class PSSiteComments
{
    private String site;

    private List<String> comments;

    public PSSiteComments()
    {
        comments = new ArrayList<>();
    }

    public String getSite()
    {
        return site;
    }

    public void setSite(String site)
    {
        this.site = site;
    }

    /**
     * Returns the actual (modificable) list of comment's IDs.
     * 
     * @return The actual list of comment's IDs.
     */
    public List<String> getComments()
    {
        return comments;
    }

    public void setComments(List<String> comments)
    {
        this.comments = comments;
    }
}
