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
