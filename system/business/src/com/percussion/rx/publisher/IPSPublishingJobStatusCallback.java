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

/**
 * An implementation of this interface is made available to allow a caller
 * get notified (with the job status) when the job is finished. 
 *
 * @author Yu-Bing Chen
 */
public interface IPSPublishingJobStatusCallback
{
   /**
    * Notify the caller with the job status. This is invoked after the job is
    * finished.
    * 
    * @param status the status of the job, never <code>null</code>.
    */
   void notifyStatus(IPSPublisherJobStatus status);
}
