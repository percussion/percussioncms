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

import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class to represent error m_message.
 * 
 * @author bjoginipally
 * 
 */
@XmlRootElement(name = "Response")
public class PSPkgUiResponse
{
   /**
    * No arg ctor for the framework.
    *
    */
   PSPkgUiResponse()
   {
      super();
   }
   /**
    * Ctor for creating package ui response.
    * 
    * @param type the type of the response.
    * @param message, sets as empty string if it is <code>null</code>.
    */
   PSPkgUiResponse(PSPkgUiResponseType type, String message)
   {
      setType(type);
      setMessage(message);
   }

   /**
    * Gets the message associated with this error.
    * 
    * @return message never <code>null</code> may be empty.
    */
   @XmlElement(name = "message")
   public String getMessage()
   {
      return m_message;
   }

   /**
    * Returns the type of the response.
    * 
    * @return response type.
    */
   @XmlElement(name = "type")
   public PSPkgUiResponseType getType()
   {
      return type;
   }

   public void setMessage(String message)
   {
      m_message = StringUtils.defaultString(message);
   }

   public void setType(PSPkgUiResponseType m_type)
   {
      this.type = m_type;
   }

   private String m_message = "";

   private PSPkgUiResponseType type;

   /**
    * Enum class for package ui response type, has two values success and
    * failure.
    * 
    * @author bjoginipally
    * 
    */
   public enum PSPkgUiResponseType
   {
      FAILURE(0), SUCCESS(1);
      PSPkgUiResponseType(int value)
      {
         m_value = value;
      }

      public int getValue()
      {
         return m_value;
      }

      public String toString()
      {
         return m_value + "";
      }

      private int m_value;
   }
}
