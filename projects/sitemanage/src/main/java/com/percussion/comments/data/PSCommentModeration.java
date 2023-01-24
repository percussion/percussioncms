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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Comment moderation object sent by client, with a list of
 * comments IDs to moderate in the delivery side.
 * 
 * @author miltonpividori
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "deletes",
    "approves",
    "rejects"
})
@XmlRootElement(name = "moderation")
public class PSCommentModeration
{
    private Collection<PSSiteComments> deletes;
    
    private Collection<PSSiteComments> approves;

    private Collection<PSSiteComments> rejects;

    public PSCommentModeration()
    {
        deletes = new ArrayList<>();
        approves = new ArrayList<>();
        rejects = new ArrayList<>();
    }

    /**
     * Returns the actual (modificable) collection of 'deletes'.
     * 
     * @return The actual (modificable) collection of 'deletes'.
     */
    public Collection<PSSiteComments> getDeletes()
    {
        return deletes;
    }

    public void setDeletes(Collection<PSSiteComments> deletes)
    {
        this.deletes = deletes;
    }
    
    /**
     * Returns the actual (modificable) collection of 'approves'.
     * 
     * @return The actual (modificable) collection of 'approves'.
     */
    public Collection<PSSiteComments> getApproves()
    {
        return approves;
    }

    public void setApproves(Collection<PSSiteComments> approves)
    {
        this.approves = approves;
    }

    /**
     * Returns the actual (modificable) collection of 'rejects'.
     * 
     * @return The actual (modificable) collection of 'rejects'.
     */
    public Collection<PSSiteComments> getRejects()
    {
        return rejects;
    }

    public void setRejects(Collection<PSSiteComments> rejects)
    {
        this.rejects = rejects;
    }
}
