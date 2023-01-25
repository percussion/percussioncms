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
package com.percussion.rx.publisher.data;

import java.io.Serializable;

/**
 * This simple message is sent if publishing has been cancelled for a given
 * job. The receivers should hold the information for as long as seems 
 * appropriate, until it is very unlikely that any further messages will be
 * processed from the cancelled job. Use the {@link #shouldDiscard()} method
 * to determine this.
 * <p>
 * Cancellation messages are sent with high priority so they are bumped to the
 * front of the JMS queue. However, the messages to be processed may be 
 * interleaved with other publishing jobs. So the "keep" time needs to keep in
 * mind that other jobs may have to complete before the trailing messages for
 * the cancelled jobs are being processed. It is not greatly harmful if such
 * messages are processed, but they will waste cycles better used for other
 * purposes.
 * <p>
 * When the delivery manager receives the cancellation message, any queued
 * result data is rolled back.
 * 
 * @author dougrand
 *
 */
public class PSCancelPublishingMessage implements Serializable
{
   /**
    * Serial id for serialization
    */
   private static final long serialVersionUID = 6513066661821822172L;

   /**
    * Set on construction to the current system time. Used by the recipients
    * to discard this message after an appropriate time lapse.
    */
   private long m_cancellationTime;
   
   /**
    * The job that has been cancelled. Used to decide if a message should be
    * processed or not.
    */
   private long m_jobId;
   
   /**
    * The time after which cancellations should be removed. 24 hours seems
    * sufficient.
    */
   public static final long DISCARD_TIME = 24 * 3600 * 1000; 
   
   /**
    * Ctor
    * @param jobId job id
    */
   public PSCancelPublishingMessage(long jobId)
   {
      m_jobId = jobId;
      m_cancellationTime = System.currentTimeMillis();
   }

   /**
    * Return the job id, used to decide if a particular message should be 
    * discarded as being from a cancelled job.
    * 
    * @return the jobId
    */
   public long getJobId()
   {
      return m_jobId;
   }
   
   /**
    * Should this cancellation be discarded? Cancellation messages are held by
    * the processors until this method returns <code>true</code>.
    * 
    * @return <code>true</code> if the cancellation is old enough to be
    *         discarded.
    */
   public boolean shouldDiscard()
   {
      return (System.currentTimeMillis() - m_cancellationTime) > DISCARD_TIME;
   }
   
   
}
