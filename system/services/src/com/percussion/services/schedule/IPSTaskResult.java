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
