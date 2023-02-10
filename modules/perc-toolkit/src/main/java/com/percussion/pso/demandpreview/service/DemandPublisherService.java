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
package com.percussion.pso.demandpreview.service;

import com.percussion.rx.publisher.IPSPublisherJobStatus.State;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.utils.guid.IPSGuid;

import java.util.concurrent.TimeoutException;

/**
 * Service facade for the demand publisher.  This service 
 * allows demand publishing of a specific single item. 
 *
 * The edition used for this publication <b>MUST</b> have
 * at least one content list that uses the "Selected Items" 
 * content list generator.  Failure to observe this rule
 * will result in a publisher timeout, rather than an error. 
 * 
 * @author davidbenua
 *
 */
public interface DemandPublisherService
{
   /**
    * Publish a single item and wait for the publication to complete.
    * 
    * @param edition the edition to use. 
    * @param content the content item to publish.
    * @param folder the folder where the content item resides. 
    * @throws TimeoutException when the publisher fails to complete before the time limit. 
    * @throws PSAssemblyException when an unspecified problem occurs during assembly. 
    */
   public void publishAndWait(IPSEdition edition, IPSGuid content,
         IPSGuid folder) throws TimeoutException, PSAssemblyException;
   
   /**
    * Queue an item of demand work without waiting for completion. 
    * @param edition the edition to use
    * @param content the content item to publish   
    * @param folder the folder where the content item resides. 
    * @return the Request ID 
    * @throws TimeoutException when the publisher fails to start (or queue) the edition within the time limit. 
    */
   public long queueDemandWork(IPSEdition edition, IPSGuid content,
         IPSGuid folder) throws TimeoutException;
   
   /**
    * Wait for a demand work request to complete. 
    * @param requestId the request Id returned from the <code>queueDemandWork()</code> method. 
    * @return the final state of the publishing job. 
    * @throws TimeoutException when the publisher fails to complete within the time limit. 
    */
   public State waitDemandWorkComplete(long requestId) throws TimeoutException;
  
}