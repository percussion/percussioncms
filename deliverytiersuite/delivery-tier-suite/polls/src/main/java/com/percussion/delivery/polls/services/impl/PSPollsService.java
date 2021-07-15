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

package com.percussion.delivery.polls.services.impl;

import com.percussion.delivery.polls.data.IPSPoll;
import com.percussion.delivery.polls.data.IPSPollAnswer;
import com.percussion.delivery.polls.service.rdbms.PSPoll;
import com.percussion.delivery.polls.service.rdbms.PSPollAnswer;
import com.percussion.delivery.polls.services.IPSPollsDao;
import com.percussion.delivery.polls.services.IPSPollsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PSPollsService implements IPSPollsService 
{
	private IPSPollsDao pollsDao;
	
	@Autowired
	public PSPollsService(IPSPollsDao pollsDao)
	{
		this.pollsDao = pollsDao;
	}
	
	@Override
	public IPSPoll findPoll(String pollName) 
	{
		return pollsDao.find(pollName);
	}

	@Override
	public void savePoll(String pollName, String pollQuestion, Map<String, Boolean> pollAnswers) 
	{
		IPSPoll poll = pollsDao.findByQuestion(pollQuestion);
		if(poll == null)
			poll = pollsDao.createEmptyPoll();
		poll.setPollName(pollName);
		poll.setPollQuestion(pollQuestion);
		Set<IPSPollAnswer> dbPollAnswers = poll.getPollAnswers();
		if(dbPollAnswers == null)
		{
			dbPollAnswers = new HashSet<>();
			updateAnswers(dbPollAnswers, pollAnswers,(PSPoll)poll);

			poll.setPollAnswers(dbPollAnswers);
		}
		else
		{
		    updateAnswers(dbPollAnswers, pollAnswers,(PSPoll)poll);
		}
		pollsDao.save(poll);
	}

	/**
	 *
	 * @param dbPollAnswers
	 * @param pollAnswers
	 * @return
	 */
	private void updateAnswers(Set<IPSPollAnswer> dbPollAnswers, Map<String, Boolean> pollAnswers, PSPoll poll)
    {
        for (Entry<String, Boolean> pollAnswer : pollAnswers.entrySet())
        {
            boolean found = false;
            for (IPSPollAnswer dbPollAnswer : dbPollAnswers)
            {
                if (dbPollAnswer.getAnswer().equalsIgnoreCase(pollAnswer.getKey()))
                {
                    if (pollAnswer.getValue())
                        dbPollAnswer.setCount((dbPollAnswer.getCount() + 1));
                    found = true;
                    break;
                }
            }
            if (!found && pollAnswer.getValue())
            {
                PSPollAnswer newPollAnswer = (PSPollAnswer)pollsDao.createEmptyAnswer();
                newPollAnswer.setAnswer(pollAnswer.getKey());
                newPollAnswer.setCount(1);
                newPollAnswer.setPoll(poll);
                dbPollAnswers.add(newPollAnswer);
            }
        }
    }

    @Override
    public IPSPoll findPollByQuestion(String pollQuestion)
    {
        return pollsDao.findByQuestion(pollQuestion);
    }
}
