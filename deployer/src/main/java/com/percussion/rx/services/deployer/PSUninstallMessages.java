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
package com.percussion.rx.services.deployer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Collection of uninstall messages object.
 * @author bjoginipally
 *
 */
@XmlRootElement(name = "Messages")
public class PSUninstallMessages
{

   /**
    * 
    */
   public PSUninstallMessages()
   {
      super();
   }

   /**
    * @param messages
    */
   public PSUninstallMessages(List<PSUninstallMessage> messages)
   {
      super();
      if(messages != null)
         m_messages = messages;
   }

   /**
    * @return the messages
    */
   @XmlElement(name="Message", type=PSUninstallMessage.class)
   public List<PSUninstallMessage> getMessages()
   {

      return m_messages;
   }



   /**
    * @param messages the messages to set
    */
   public void setMessages(List<PSUninstallMessage> messages)
   {
      m_messages = messages;
   }
   
   /**
    * Adds a message to the collection.
    * @param message the message to add, cannot be <code>null</code>.
    */
   public void add(PSUninstallMessage message)
   {
      if(message == null)
         throw new IllegalArgumentException("message cannot be null.");
      m_messages.add(message);   
   }
   
   /**
    * Removes the specified message from the collection
    * if it exists.
    * @param message the message to be removed. May be <code>null</code>.
    */
   public void remove(PSUninstallMessage message)
   {
      m_messages.remove(message);   
   }
   
   /**
    * Removes all the messages from the collection.
    */
   public void clear()
   {
      m_messages.clear();
   }
   
   /**
    * The list of messages, never <code>null</code>, may
    * be empty.
    */

   private List<PSUninstallMessage> m_messages = new ArrayList<>();

   private Integer status = 0;
   public Integer getStatus() {
      return status;
   }
   public void setStatus(Integer status) {
      this.status = status;
   }

}
