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

package com.percussion.delivery.polls.data;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement(name = "poll")
public class PSRestPoll 
{
	private String pollName;
	private String pollQuestion;
	private Map<String, Integer> pollResults;
	private int totalVotes;
    private Map<String, Boolean> pollSubmits;
    private boolean restrictBySession;

    public PSRestPoll(){

	}
	
    public String getPollName() 
	{
		return pollName;
	}

	public void setPollName(String pollName) 
	{
		this.pollName = pollName;
	}

	public String getPollQuestion() 
	{
		return pollQuestion;
	}

	public void setPollQuestion(String pollQuestion) 
	{
		this.pollQuestion = pollQuestion;
	}

	public Map<String, Integer> getPollResults() 
	{
		return pollResults;
	}

	public void setPollResults(Map<String, Integer> pollResults) 
	{
		this.pollResults = pollResults;
	}
	
	public Map<String, Boolean> getPollSubmits() 
	{
		return pollSubmits;
	}

	public void setpollSubmits(Map<String, Boolean> pollSubmits) 
	{
		this.pollSubmits = pollSubmits;
	}

    public int getTotalVotes()
    {
        return totalVotes;
    }

    public void setTotalVotes(int totalVotes)
    {
        this.totalVotes = totalVotes;
    }

    public boolean isRestrictBySession()
    {
        return restrictBySession;
    }

    public void setRestrictBySession(boolean restrictBySession)
    {
        this.restrictBySession = restrictBySession;
    }
}
