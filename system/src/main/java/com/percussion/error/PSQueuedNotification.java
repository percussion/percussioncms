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
