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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
