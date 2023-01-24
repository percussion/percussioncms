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

import java.util.Map;

/**
 * Results to be used to decide if the task completed successfully and 
 * information to be returned for use in notifications.
 * 
 * @author Doug Rand
 */
public interface IPSTaskResult
{
   /**
    * Was the scheduled task successful.
    * @return <code>true</code> if the task succeeded.
    */
   boolean wasSuccess();
   
   /**
    * If the task failed, this should provide a meaningful (to an end user) 
    * description of the failure. It is acceptable to include information that
    * only a developer can use, but it must be secondary to the primary 
    * description, which all users should be able to understand.
    * 
    * @return the problem description, will be <code>null</code> if the 
    * task was successful.
    */
   String getProblemDescription();
   
   /**
    * The notification variables to be used when creating notification emails.
    *  
    * @return the notification variables, may be empty but not 
    * <code>null</code>.
    */
   Map<String, Object> getNotificationVariables();
}
