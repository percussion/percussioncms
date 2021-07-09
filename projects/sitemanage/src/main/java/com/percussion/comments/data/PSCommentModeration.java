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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
