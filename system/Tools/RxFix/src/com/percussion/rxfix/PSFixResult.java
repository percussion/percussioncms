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
package com.percussion.rxfix;

import org.apache.commons.lang.StringUtils;

/**
 * Each fix (or preview) results in a result. The result has a status and a 
 * message. There can be multiple results per "thing" fixed. 
 * 
 * @author dougrand
 *
 */
public class PSFixResult
{
   /**
    * The status, which is always preview for preview mode
    */
   public enum Status
   {
      /**
       * This result was a preview
       */
      PREVIEW, 
      /**
       * This result was a debug comment
       */
      DEBUG, 
      /**
       * This result was from a successful operation
       */
      SUCCESS, 
      /**
       * This result was from a warning during operation
       */
      WARNING, 
      /**
       * This result was from a failure during operation
       */
      FAILURE, 
      /**
       * This result was from information output during operation
       */
      INFO;
   }

   private Status m_status;
   private String m_id;
   private String m_operation;
   private String m_message;
   
   /**
    * Ctor
    * @param status the status, never <code>null</code>
    * @param id the id or ids, may be <code>null</code>
    * @param op the operation, never <code>null</code> or empty
    * @param message the message, never <code>null</code> or empty
    */
   public PSFixResult(Status status, String id, String op, String message)
   {
      if (status == null)
      {
         throw new IllegalArgumentException("status may not be null");
      }
      if (StringUtils.isBlank(op))
      {
         throw new IllegalArgumentException("op may not be null or empty");
      }
      if (StringUtils.isBlank(message))
      {
         throw new IllegalArgumentException("message may not be null or empty");
      }
      m_status = status;
      m_id = id;
      m_operation = op;
      m_message = message;
   }
   
   /**
    * Get the message for the result
    * @return the message, never <code>null</code> or empty
    */
   public String getMessage()
   {
      return m_message;
   }
   
   /**
    * The name of the operation being performed
    * @return the name of the operation, never <code>null</code> or empty
    */
   public String getOperation()
   {
      return m_operation;
   }
   
   /**
    * The <q>id</q> of the thing fixed by the result. Multiple results may be
    * collected for one thing. The id may be <code>null</code> or contain a 
    * comma separated list of ids.
    * @return the id or ids of the thing or things fixed
    */
   public String getId()
   {
      return m_id;
   }
   
   /**
    * Get the status for the result
    * @return the status, never <code>null</code>
    */
   public Status getStatus()
   {
      return m_status;
   }
   
   @Override
   public String toString()
   {
      StringBuilder b = new StringBuilder();
      
      b.append(m_status);
      b.append(" ");
      b.append(m_message);
      if (StringUtils.isNotBlank(m_id))
      {
         b.append(" item or items: ");
         b.append(m_id);
      }
      return b.toString();
   }
}
