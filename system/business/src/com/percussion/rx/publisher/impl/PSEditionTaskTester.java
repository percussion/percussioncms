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
package com.percussion.rx.publisher.impl;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.sitemgr.IPSSite;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Test the edition task system by printing out information passed in. To use,
 * register the extension and create an edition (or modify an existing edition)
 * and use this both before and after the edition is run.
 * 
 * @author dougrand
 */
public class PSEditionTaskTester implements IPSEditionTask
{

   public TaskType getType()
   {
      return TaskType.PREANDPOSTEDITION;
   }

   public void perform(IPSEdition edition, IPSSite site, Date start_time,
         Date end_time, long jobid, long duration, boolean success,
         Map<String, String> params, IPSEditionTaskStatusCallback status)
   {
      // Check for consistent behavior and correct semantics
      if (edition == null)
      {
         throw new IllegalArgumentException("edition may not be null");
      }
      if (site == null)
      {
         throw new IllegalArgumentException("site may not be null");
      }
      if (start_time == null)
      {
         throw new IllegalArgumentException("start_time may not be null");
      }
      if (end_time == null)
      {
         if (duration > -1)
         {
            throw new IllegalStateException("Pre task should have no duration");
         }
         if (status != null)
         {
            throw new IllegalStateException(
                  "Pre task should have no status callback");
         }
      }
      else
      {
         if (duration < 0)
         {
            throw new IllegalStateException("Post task should have a duration");
         }
         if (status == null)
         {
            throw new IllegalStateException(
                  "Post task should have a status callback");
         }
         
         // Use the status callback and let's assume that there should
         // be something there
         List<IPSPubItemStatus> entries = status.getJobStatus();
         if (entries == null)
         {
            throw new IllegalStateException(
               "Post task should have status information");
         }
         if (entries.size() == 0                                                                                                                                            )
         {
            throw new IllegalStateException(
               "Post task should have log entries");
         }
      }

   }

   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // No init
   }

}
