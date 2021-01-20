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

package com.percussion.delivery.polls.service.rdbms;

import com.percussion.delivery.polls.data.IPSPoll;
import com.percussion.delivery.polls.data.IPSPollAnswer;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "PERC_POLLS")
public class PSPoll implements IPSPoll, Serializable
{

    @Id
    @GeneratedValue
    @Column(name = "POLL_ID")
	private long pollId;

    @Column(name = "POLL_NAME", nullable = false, length = 256)
	private String pollName;

    @Column(name = "POLL_QUESTION", nullable = false, length = 4000)
	private String pollQuestion;


    @OneToMany(cascade = CascadeType.ALL,  fetch = FetchType.EAGER, mappedBy = "poll", targetEntity = PSPollAnswer.class, orphanRemoval=true)
	private Set<IPSPollAnswer> pollAnswers;

    @Version
    @Column(name = "VERSION")
    Integer version;	


	public long getPollId()
	{
		return pollId;
	}

	public void setPollId(long id)
	{

            this.pollId = id;
	}

	public String getId()
	{
	    return String.valueOf(this.pollId);
	}
	
    public void setId(String id)
    {
        this.pollId = Long.parseLong(id);
    }
	
	@Override
	public String getPollName()
	{
		return pollName;
	}

	@Override
	public void setPollName(String pollName) 
	{
		this.pollName = pollName;
	}

	@Override
	public String getPollQuestion()
	{
		return pollQuestion;
	}

	@Override
	public void setPollQuestion(String pollQuestion) 
	{
		this.pollQuestion = pollQuestion;
	}

    @Override
	public Set<IPSPollAnswer> getPollAnswers()
	{
		return this.pollAnswers;
	}

	@Override
	public void setPollAnswers(Set<IPSPollAnswer> pollAnswers) 
	{
		this.pollAnswers = pollAnswers;
	}
    /**
     * @return Returns the version.
     */
    public Integer getVersion()
    {
        return version;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(Integer version)
    {
        if (this.version != null && version != null)
            throw new IllegalStateException("Version can only be set once");

        this.version = version;
    }

}
