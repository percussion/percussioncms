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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.error;

import com.percussion.mail.PSMailMessage;

/**
 * The PSQueuedNotification class is used to control PSErrorHandler to send
 * messages to administrators.
 *
 * @author     Jian Huang
 * @version    1.0
 * @since      1.0
 */
public class PSQueuedNotification extends PSMailMessage
{
   /**
    * Construct an empty constructor.
    */
   public PSQueuedNotification(){
      super();
   }

   /**
    * Append the body text of the e-mail message.
    */
   public void appendBodyText(String text){
      try {
         super.appendBodyText(text + "\r\n\n");
      } catch (java.io.IOException e) {
         // this cannot happen based on our use of the message
         // (we don't mix streams with text data)
      }
   }

   /**
    * Reset the e-mail body to empty after the message has been sent out.
    */
   public void resetBodyText(){
      super.resetBodyText();
   }

   /**
    * Add the counter by one for general error count.
    */
   public void addGeneralErrorCountByOne(){
      m_generalErrorCount += 1;
   }

   /**
    * Get the counter for general error count.
    */
   public int getGeneralErrorCount(){
      return m_generalErrorCount;
   }

   /**
    * Set the counter for general error count.
    */
   public void setGeneralErrorCount(int count){
      m_generalErrorCount = count;
   }

   /**
    * Get the general error's occur time before notifying the administrator.
    * The time unit is in milliseconds.
    */
   public long getGeneralErrorInterval(){
      return m_generalErrorInterval;
   }

   /**
    * Set the general error's occur time before notifying the administrator.
    * The time unit is in milliseconds.
    */
   public void setGeneralErrorInterval(long interval){
      m_generalErrorInterval = interval;
   }

   /**
    * Get the initial date.
    */
   public java.util.Date getInitDate(){
      return m_initTime;
   }

   /**
    * Set the initial date.
    */
   public void setInitDate(java.util.Date now){
      m_initTime = now;
   }

   private int  m_generalErrorCount    = 0;
   private long m_generalErrorInterval = 0; // milliseconds

   private java.util.Date m_initTime = new java.util.Date();
}
