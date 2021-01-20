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

package com.percussion.delivery.polls.services;

import com.percussion.delivery.polls.data.IPSPoll;

import java.util.Map;

public interface IPSPollsService 
{
	/**
	 * Finds the poll with the supplied poll name. Returns <code>null</code> if not found.
	 * @param pollName name of the poll, must not be blank.
	 * @return IPSPoll or <code>null</code> if poll is not found.
	 */
    public IPSPoll findPoll(String pollName);

    /**
     * Finds the poll with the supplied poll question. Returns <code>null</code> if not found.
     * @param pollQuestion poll question must not be blank.
     * @return IPSPoll or <code>null</code> if poll is not found.
     */    
    public IPSPoll findPollByQuestion(String pollQuestion);
	
    /**
     * Saves the poll.
     * @param pollName Name of the poll, must not be blank.
     * @param pollQuestion question of the poll, must not be blak.
     * @param pollAnswers map of poll answer and its value as boolean, true means the answer count is incremented by one, false means the answer is not tounched,
     */
    public void savePoll(String pollName, String pollQuestion, Map<String, Boolean> pollAnswers);
}
