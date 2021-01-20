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
   @XmlElement(name = "message")
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
   private List<PSUninstallMessage> m_messages = new ArrayList<PSUninstallMessage>();
}
