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



