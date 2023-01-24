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

package com.percussion.delivery.polls.data;


import java.util.Set;

public interface IPSPoll 
{
	public String getId();
	public void setId(String id);
	public String getPollName();
	public void setPollName(String pollName);
	public String getPollQuestion();
	public void setPollQuestion(String pollQuestion);
	public Set<IPSPollAnswer> getPollAnswers();
	public void setPollAnswers(Set<IPSPollAnswer> pollAnswers);

}
