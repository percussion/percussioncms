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
package com.percussion.services.schedule;

import com.percussion.extension.IPSExtension;

import java.util.Map;

/**
 * An extension to be run by the scheduler.
 * Examples out of the box of this interface implementations include running
 * aging transitions for workflow and scheduled jobs for publishing.
 *
 * @author Doug Rand
 */
public interface IPSTask extends IPSExtension
{
   /**
    * Perform the task.
    * 
    * @param parameters the parameters registered for the task, never
    * <code>null</code> but may be empty. These parameters will be documented
    * for the specific implementation.
    * The changes to this map are persisted across invocations for this task.
    * @return the result, see the result documentation, never <code>null</code>.
    */
   IPSTaskResult perform(Map<String,String> parameters);
}
