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
