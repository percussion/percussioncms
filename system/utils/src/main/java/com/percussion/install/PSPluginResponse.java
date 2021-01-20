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

package com.percussion.install;


/**
 * This class encapsulates the response from a pre-upgrade plugin.  Three types
 * of responses are supported: SUCCESS, EXCEPTION, and WARNING.
 */
public class PSPluginResponse
{
   /**
    * Constructor
    * 
    * @param type the type of the plugin response.
    * <code>SUCCESS</code>, <code>EXCEPTION</code>, and <code>WARNING</code>
    * @param message the plugin response message.
    * @throws IllegalArgumentException if type is not one of
    * <code>SUCCESS</code>, <code>EXCEPTION</code>, or <code>WARNING</code>
    */
   public PSPluginResponse(int type, String message)
   {
      if (!((type == SUCCESS) || (type == EXCEPTION) || (type == WARNING)))
         throw new IllegalArgumentException("Illegal response type");
      m_type = type;
      m_message = message;
   }

   /**
    * Gets the response type.
    * 
    * @return the type of the plugin response.
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Sets the response type.
    * 
    * @param type the type of the plugin response.
    * <code>SUCCESS</code>, <code>EXCEPTION</code>, and <code>WARNING</code>
    * @throws IllegalArgumentException if type is not one of
    * <code>SUCCESS</code>, <code>EXCEPTION</code>, or <code>WARNING</code>
    */
   public void setType(int type)
   {
      if (!((type == SUCCESS) || (type == EXCEPTION) || (type == WARNING)))
         throw new IllegalArgumentException("Illegal response type");
      m_type = type;
   }

   /**
    * Returns the response message.
    * @return the message part of the plugin response.
    */
   public String getMessage()
   {
      return m_message;
   }

   /**
    * Sets the response message.
    * @param message the plugin response message.
    */
   public void setMessage(String message)
   {
      m_message = message;
   }

   /**
    * The type of response, valid values are:
    * <code>SUCCESS</code> or
    * <code>EXCEPTION</code> or
    * <code>WARNING</code>
    */
   private int m_type = SUCCESS;

   /**
    * The response message
    */
   private String m_message = "";

   /**
    * The success constant
    */
   static public final int SUCCESS = 1;

   /**
    * The exception constant
    */
   static public final int EXCEPTION = 0;
   
   /**
    * The warning constant
    */
   static public final int WARNING = 2;

}



