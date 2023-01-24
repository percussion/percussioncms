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
package com.percussion.services.error;

import com.percussion.i18n.PSI18nUtils;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

/**
 * The base class for unchecked runtime exception in services package.
 * The base message should be defined in SystemResources.tmx.
 *
 * @author Yu-Bing Chen
 */
public class PSRuntimeException extends RuntimeException
{
  
   /**
    * Default constructor.
    */
   public PSRuntimeException()
   {
      super();
   }
   
   /**
    * Constructs an exception with the specified detail message.
    * @param errorMsg the specified detail message.
    */
   public PSRuntimeException(String errorMsg)
   {
      super(errorMsg);
      m_rawMessage = errorMsg;
   }
   
   /**
    * Constructs an exception with the specified cause.
    * @param e the cause of the exception.
    */
   public PSRuntimeException(Throwable e)
   {
      super(e);
      m_rawMessage = e.getMessage();
   }

   /**
    * Constructs an exception with the specified detail message and the cause.
    * @param errorMsg the specified detail message.
    * @param e the cause of the exception.
    */
   public PSRuntimeException(String errorMsg, Throwable e)
   {
      super(errorMsg, e);
      m_rawMessage = errorMsg;
   }

   /**
    * The key of the base message defined in SystemResources.tmx.
    * It is initialized by {@link #setMsgKeyAndArgs(String, Object[])},
    * never <code>null</code> after that.
    */
   private String m_msgKey;
   
   /**
    * The arguments for the base message defined in SystemResources.tmx.
    * It is initialized by {@link #setMsgKeyAndArgs(String, Object[])},
    * never <code>null</code> after that.
    */
   private Object[] m_msgArgs;

   /**
    * The raw message that is provided by the caller, but is not in the 
    * SystemResources.tmx. 
    */
   private String m_rawMessage = null;
   
   /**
    * Set both message key and its arguments of the exception message.
    * 
    * @param key key of the base message defined in SystemResources.tmx, 
    *    never <code>null</code> or empty.
    * @param args the arguments for the base message, never <code>null</code>.
    */
   protected void setMsgKeyAndArgs(String key, Object[] args)
   {
      if (StringUtils.isBlank(key))
         throw new IllegalArgumentException("key may not be null or empty.");
      if (args == null)
         throw new IllegalArgumentException("args may not be null.");
      
      m_msgKey = key;
      m_msgArgs = args;
   }
   
   @Override
   public String getMessage()
   {
      if (m_rawMessage != null)
         return m_rawMessage;
      
      String text = PSI18nUtils.getString(m_msgKey);
      String message = MessageFormat.format(text, m_msgArgs);
      return message;
   }
   
   @Override
   public String getLocalizedMessage()
   {
      return getMessage();
   }

}
