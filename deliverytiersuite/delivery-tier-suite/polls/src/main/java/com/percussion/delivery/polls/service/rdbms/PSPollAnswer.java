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

package com.percussion.delivery.polls.service.rdbms;

import com.percussion.delivery.polls.data.IPSPollAnswer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.io.Serializable;

@Entity
@Table(name = "PERC_ANSWERS")
public class PSPollAnswer implements IPSPollAnswer, Serializable
{
    @Id
    @GeneratedValue
    @Column(name = "ANSWER_ID")
	private long id;

    @Column(name = "ANSWER", nullable = false, length = 4000)
	private String answer;

    @Column(name = "COUNT")
	private int count;

    @Version
    @Column(name = "VERSION")
    Integer version;

	@ManyToOne(optional = false)
	@JoinColumn(name = "POLL_ID")
	private PSPoll poll;



	@Override
	public long getId() 
	{
		return id;
	}

    @Override
	public void setId(long id) 
	{
		this.id = id;
	}

	@Override
	public String getAnswer()
	{
		return answer;
	}

	@Override
	public void setAnswer(String answer) 
	{
		this.answer = answer;
	}

	@Override
	public int getCount()
	{
		return count;
	}

	@Override
	public void setCount(int count) 
	{
		this.count = count;
	}

    /**
     * @return Returns the version.
     */
    public Integer getVersion()
    {
        return version;
    }

	public PSPoll getPoll() {
		return poll;
	}

	public void setPoll(PSPoll poll) {
		this.poll = poll;
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
