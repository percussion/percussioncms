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

import com.percussion.utils.guid.IPSGuid;

import java.io.Serializable;

/**
 * Message send at start and end of a job to initialize and cleanup. Control
 * messages, like other non-work messages, are sent with the highest priority 
 * so they are processed before waiting work messages. Otherwise we'd be waiting
 * for job messages to clear before initializing new jobs or committing finished
 * jobs.
 * 
 * @author dougrand
 *
 */
public class PSJobControlMessage implements Serializable
{
   /**
    * Type of control message
    */
   public enum ControlType
   {
      /**
       * Start a job, sends the site id (IPSGuid) as data. The site information 
       * is held by the delivery manager to later initialize delivery handlers.
       * Processors also use the message to allocate storage for use by the job.
       */
      START, 
      /**
       * End a job. Resources are freed by processors and the delivery manager 
       * will cause the job's queued results to be committed. If there was an
       * earlier cancellation then results will have been flushed already. Note
       * that there is no associated data for the end message.
       */
      END
   }
   
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Type of message
    */
   private ControlType m_type;
   
   /**
    * Data, dictated by the message type
    */
   private Serializable m_data;
   
   /**
    * The jobid
    */
   private long m_jobId;

   /**
    * The site id, never <code>null</code>.
    */
   private IPSGuid m_siteId;
   
   private IPSGuid m_pubServerId;
   /**
    * Create a message
    * @param jobId the job's id.
    * @param type the type, never <code>null</code>.
    * @param siteid the site that this publishing is being done for, 
    *   never <code>null</code>.
    * @param data the data, which is dictated by the message type.
    */
   public PSJobControlMessage(long jobId, ControlType type, IPSGuid siteid, IPSGuid pubServerId,
         Serializable data)
   {
      if (type == null)
      {
         throw new IllegalArgumentException("type may not be null");
      }
      if (siteid == null)
      {
         throw new IllegalArgumentException("siteid may not be null");
      }
      m_type = type;
      m_data = data;
      m_jobId = jobId;
      m_siteId = siteid;
      m_pubServerId = pubServerId;
   }

   /**
    * The job id is used to identify all messages for a given instance of an 
    * edition being run. The job id is an essentially never repeating id that
    * is also used for storing status in the database.
    * 
    * @return the jobId
    */
   public long getJobId()
   {
      return m_jobId;
   }



   /**
    * Some messages, currently just the start message, have a data payload. The
    * data is dictated by the message type.
    * 
    * @return the data, may be <code>null</code>.
    */
   public Serializable getData()
   {
      return m_data;
   }

   /**
    * The message type.
    * 
    * @return the type, never <code>null</code>.
    * @see ControlType
    */
   public ControlType getType()
   {
      return m_type;
   }

   /**
    * @return the siteId
    */
   public IPSGuid getSiteId()
   {
      return m_siteId;
   }

   /**
    * @return the publishing server id.
    */
   public IPSGuid getPubServerId()
   {
      return m_pubServerId;
   }

}
