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
