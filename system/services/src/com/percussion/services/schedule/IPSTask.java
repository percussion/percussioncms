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
