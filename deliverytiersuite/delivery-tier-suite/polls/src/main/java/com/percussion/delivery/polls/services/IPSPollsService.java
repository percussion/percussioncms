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
