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
package com.percussion.rx.publisher;

import com.percussion.extension.IPSExtension;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.sitemgr.IPSSite;

import java.util.Date;
import java.util.Map;

/**
 * An edition task can be run either before an edition is run or after an
 * edition completes. Edition tasks can be registered to enable setup, cleanup
 * and validation activities around the running of an edition.
 * 
 * @author dougrand
 */
public interface IPSEditionTask extends IPSExtension
{
   /**
    * The task type describes the usage of the given extension.
    */
   public enum TaskType {
      /**
       * The task should be run before the edition.
       */
      PREEDITION,
      /**
       * The task should be run after the edition completes.
       */
      POSTEDITION,
      /**
       * The task can be registered for either before or after the edition run.
       */
      PREANDPOSTEDITION;
   }

   /**
    * Perform the task, either before or after the edition is run, depending on
    * the registration. 
    * <h3>Implementation notes</h3>
    * Note for each parameter whether the parameter is
    * available given a usage. 
    * <p>
    * Post edition tasks may also wish to retrieve
    * status information from the service and change behavior according to
    * whether a particular item published successfully or not.
    * 
    * @param edition the edition description, never <code>null</code>
    * @param site the site description, never <code>null</code>
    * @param startTime the time when the edition started to run, this is the
    *            time at which the job was spawned, which is to say the initial
    *            time before the first task is called, never <code>null</code>.
    * @param endTime the time when the job completed, before the first post
    *            task is invoked. This time is only available to post tasks and
    *            will be <code>null</code> for pre edition tasks.
    * @param jobId the job id.
    * @param duration the length of time that the edition ran in seconds, from
    *            the first moment <i>after</i> the pre tasks completed to the
    *            moment just before the first post edition tasks started.
    *            Supplied as <code>0</code> to pre edition tasks.
    * @param success if <code>true</code> then the edition was successful,
    *            which means that all items published without error. If
    *            <code>false</code> then some or all items failed and the
    *            status callback or other service calls must be used to
    *            determine what failures existed. Undefined for pre edition
    *            tasks.
    * @param params registered parameters for the task, may be empty or
    *            <code>null</code> for tasks that don't require parameters.
    * @param status this is <code>null</code> for pre tasks, but post tasks
    *            can use this to obtain status information about the job.
    * @throws Exception if the task fails for any reason an exception should
    *            be thrown that details the reason for the failure. The 
    *            exception will be caught by the job code and recorded as part
    *            of the edition task.
    */
   void perform(IPSEdition edition, IPSSite site, Date startTime,
         Date endTime, long jobId, long duration, boolean success,
         Map<String, String> params, IPSEditionTaskStatusCallback status) throws Exception;

   /**
    * Discover when the extension can be used.
    * 
    * @return the type as specified in {@link TaskType}.
    */
   TaskType getType();
}
