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
import java.util.Collection;

/**
 * Simple class to wrap a list of comment IDs.
 * 
 * @author miltonpividori
 *
 */
public class PSCommentIds
{

    private Collection<String> comments;
    
    public PSCommentIds()
    {
        comments = new ArrayList<>();
    }

    /**
     * @return the ids
     */
    public Collection<String> getComments()
    {
        return comments;
    }

    /**
     * @param comments the ids to set
     */
    public void setComments(Collection<String> comments)
    {
        this.comments = comments;
    }
    
}
