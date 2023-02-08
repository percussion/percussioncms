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
