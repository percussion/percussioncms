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

import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSPubStatus;

import java.util.List;

/**
 * An implementation of this interface is made available to allow post edition
 * tasks to find item status without knowing the details of the publishing 
 * service interface.
 * 
 * @author dougrand
 */
public interface IPSEditionTaskStatusCallback
{
   
   /**
    * Retrieves the current publish status which
    * contains useful statistics like how many items
    * succeeded or failed to publish.
    * @return the publish status
    * @author adamgent
    */
   IPSPubStatus getCurrentPubStatus();
   
   /**
    * Retrieve the status for all items involved in a particular job.
    * 
    * @return the status, never <code>null</code>, but might be empty for 
    * a job that had no content such as an incremental with no changes.
    * @deprecated Use {@link #getIterableJobStatus()} instead as it memory efficient.
    */
   List<IPSPubItemStatus> getJobStatus();
   
   /**
    * Retrieve the status for all items involved in a particular job.
    * If you need statistics (like number of items) see {@link #getCurrentPubStatus()}.
    * @return never null.
    * @author adamgent
    */
   Iterable<IPSPubItemStatus> getIterableJobStatus();
   
}
