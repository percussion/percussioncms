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

 package com.percussion.i18n.tmxdom;

 import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * A generic and only exception that is thrown by TMX DOM classes.
 *
 * @see java.lang.RuntimeException
 */

   public class PSTmxDomException
   extends RuntimeException
{
   /**
    * Default constructor
    */
   public PSTmxDomException()
   {
      super();
   }

   /**
    * Constructor that takes the message.
    * @param msg error message string, must not be <code>null</code>.
    */
   public PSTmxDomException(String msg)
   {
      super(msg);
   }

   /**
    * Constructor that takes the message text pattern key and single argument.
    * The actual text pattern is looked up in the resource bundle and formatted
    * suitably.
    * @param patternKey pattern lookup key. The pattern is looked up in the
    * resource bundle and then the message is formatted using the argument
    * supplied.
    * @param singleArg the single argument used in formatting the message using
    * the pattern.
    */
   public PSTmxDomException(String patternKey, String singleArg)
   {
      this(patternKey, new String[] {singleArg});
   }

   /**
    * Constructor that takes the message text pattern key and arguments. The
    * actual text pattern is looked up in the resource bundle and formatted
    * suitably.
    * @param patternKey pattern lookup key. The pattern is looked up in the
    * resource bundle and then the message is formatted using the argument
    * supplied, must not be <code>null</code> or <code>empty</code>
    * @param args array of arguments used in formatting the message using
    * the pattern, must not be <code>null</code>
    * @throws IllegalArgumentException if patternKey is <code>null</code> or 
    * <code>empty</code> or args is <code>null</code>
    */
   public PSTmxDomException(String patternKey, Object[]args)
   {
      String pattern = getRes().getString(patternKey);
      String msg = patternKey;
      if(pattern != null)
         msg = MessageFormat.format(pattern, args);

      m_Message = msg;
   }

   /**
    * Get the resources.
    * @return Java Resource bundle, never be <code>null</code> unless packaged
    * wrong.
    */
   public static ResourceBundle getRes()
   {
      /* load the resources first. this will throw an exception if we can't
      find them */
      if (m_res == null)
         m_res = ResourceBundle.getBundle("com.percussion.i18n.tmxdom.PSTmxDom"
         + "Resources");

      return m_res;
   }

   /**
    * Override this method to return the formatted message, if present.
    * @return the formatted error message, if present else return the normal
    * messge at least.
    */
   public String getMessage()
   {
      if(m_Message != null)
         return m_Message;
      return super.getMessage();
   }

   /**
    * The program resources. You must access this variable thru the {@link
    * #getRes getRes} method.
    */
   private static ResourceBundle m_res = null;

   /**
    * Formatted message. Built only when used the constructor
    * {@link PSTmxDomException(String patternKey, Object[]args)}.
    */
   private String m_Message = null;
}
