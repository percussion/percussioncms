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
package com.percussion.extension;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Comparator;
import java.util.StringTokenizer;

/**
 * A simple, immutable class used to refer uniquely to an extension by its
 * handler name, context within the handler, and the extension name itself.
 */
public class PSExtensionRef implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = -1488227756687242734L;

   /**
    * Constructs a new, immutable extension reference that refers uniquely
    * to the given extension name.
    *
    * @param handlerName The handler name. Must not be <CODE>null</CODE>
    * and must be a valid handler name.
    *
    * @param context The context. Must not be <CODE>null</CODE> and
    * must be a valid context.
    *
    * @param extensionName The extension name. Must not be <CODE>null</CODE>
    * and must be a valid extension name.
    *
    * @see #isValidExtensionName
    * @see #isValidContext
    */
   public PSExtensionRef(String handlerName,
      String context,
      String extensionName
      )
   {
      init(handlerName, context, extensionName,"");
   }

   /**
    * Constructs a new, immutable extension reference that refers uniquely
    * to the given extension name.
    * @param category the category of the extension.
    * Never <CODE>null</CODE>, but it can be an empty string
    * @param handlerName The handler name. Must not be <CODE>null</CODE>
    * and must be a valid handler name.
    * @param context The context. Must not be <CODE>null</CODE> and
    * must be a valid context.
    * @param extensionName The extension name. Must not be <CODE>null</CODE>
    * and must be a valid extension name.    
    * @see #isValidExtensionName
    * @see #isValidContext
    */
    public PSExtensionRef(String category,String handlerName, String context,
      String extensionName)      
   {
      init(handlerName, context, extensionName, category);
   }

   /**
    * Constructs a new PSExtensionRef from the given full ref, which is
    * of the format returned by toString().
    *
    * @param fullName The full extension ref. Must not be <CODE>null</CODE>.
    *
    * throws IllegalArgumentException If any param is invalid.
    */
   public PSExtensionRef(String fullName)
   {
      if (fullName == null)
         throw new IllegalArgumentException("fullName cannot be null");

      String[] parsed = parseFullName(fullName);
      if (parsed == null)
         throw new IllegalArgumentException(
               "extension name not valid for full name " + fullName);
         
      init(parsed[0], parsed[1], parsed[2],"");
   }

   /**
    * Factory method to create a new well-formed PSExtensionRef that refers
    * to an extension handler with the given name.
    *
    * @param handlerName The extension handler name. Must not be
    * <CODE>null</CODE> and must be a valid extension name.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public static PSExtensionRef handlerRef(String handlerName)
   {
      return new PSExtensionRef(
         IPSExtensionHandler.HANDLER_HANDLER,
         IPSExtensionHandler.HANDLER_CONTEXT,
         handlerName);
   }

   /**
    * Returns the name of the handler for this extension, which is unique
    * within the server.
    *
    * @return The name of the extension handler. Never <CODE>null</CODE>.
    */
   public String getHandlerName()
   {
      return m_handlerName;
   }

   /**
    * Returns the canonical context for this extension, which is a sequence of
    * valid extension names separated by forward slashes. There will
    * be a single trailing slash at the end of the context name, but
    * no slash at the beginning of the name.
    * <p>
    * There are no semantic restrictions context names, there are only syntactic
    * restrictions. This means that other server components can
    * use a systematic context-naming system to impose a sub-structure or
    * namespace on the extensions they install and use.
    *
    * @return The canonical context for this extension. Never
    * <CODE>null</CODE>.
    */
   public String getContext()
   {
      return m_context;
   }

   /**
    * Returns the category of this extension
    * @return the category of this extension, never <CODE>null</CODE>,
    * but it can be an empty string
    */
   public String getCategory()
   {
      return m_category;
   }

   /**
    * Returns the name of this extension, which is unique within its handler.
    *
    * @return The name of this extension. Never <CODE>null</CODE>.
    */
   public String getExtensionName()
   {
      return m_extName;
   }

   /**
    * Setter for the extension name field.
    * 
    * @param extName extension name, must not be <code>null</code> or empty.
    */
   public void setExtName(String extName)
   {
      if (extName == null || extName.length() == 0)
      {
         throw new IllegalArgumentException("extName must not "
            + "be null or empty");
      }
      m_extName = extName;
   }

   /**
    * Returns the canonical fully-qualified String form of this reference.
    * 
    * @return string representation of this object, never <code>null</code> or
    * empty.
    * @see #getFQN
    */
   public String toString()
   {
      return getFQN();
   }

   
   /**
    * Gets the canonical fully-qualified name (FQN) for this reference, by
    * concatenating the handler name, context, and extension name.
    * 
    * @return fully-qualified name for this reference, never <code>null</code>
    * or empty.
    */ 
   public String getFQN()
   {
      StringBuilder fqn = new StringBuilder();
      fqn.append( getHandlerName() );
      fqn.append( "/" );
      fqn.append( getContext() );
      fqn.append( getExtensionName() );
      return fqn.toString();
   }
   
   @Override
   public boolean equals(Object b)
   {
      if (!(b instanceof PSExtensionRef))
         return false;
      
      PSExtensionRef second = (PSExtensionRef) b;
      return new EqualsBuilder().append(
         m_handlerName, second.m_handlerName).append(
            m_context, second.m_context).append(
               m_extName, second.m_extName).isEquals();
   }

   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(m_handlerName).append(
         m_context).append(m_extName).toHashCode();
   }

   /**
    * Returns <CODE>true</CODE> if and only if the given
    * String is a well-formed context. A well-formed context is
    * defined as a sequence of valid extension names separated
    * by forward slashes.
    *
    * @param context The context to check for validity. If
    * <CODE>null</CODE>, <CODE>false</CODE> will be returned.
    *
    * @return <CODE>true</CODE> iff the given String is
    * a well-formed context.
    */
   public static boolean isValidContext(String context)
   {
      if (context == null || context.trim().length() == 0)
         return false;

      // break up into slash-separated components, each of which
      // must be a valid extension name
      StringTokenizer toker = new StringTokenizer(context, "/");
      if (!toker.hasMoreTokens())
      {
         return false;
      }

      while (toker.hasMoreTokens())
      {
         String contextPart = toker.nextToken();
         if (!isValidContextName(contextPart))
            return false;
      }

      return true;
   }


   /**
    * Returns <CODE>true</CODE> if and only if the given
    * String is a well-formed context name. Context name is more forgiving than
    * an extension name, &apos;-&apos; characters are acceptable after the
    * leading character.
    *
    * @param name The extension name to check for validity.
    * If <CODE>null</CODE>, <CODE>false</CODE> will be returned.
    *
    * @return <CODE>true</CODE> iff the given String is
    * a well-formed context name.
    */
   public static boolean isValidContextName(String name)
   {
      if (name == null || name.trim().length() == 0)
         return false;

      char c = name.charAt(0);
      if (!Character.isJavaIdentifierStart(c))
         return false;

      final int len = name.length();
      for (int i = 1; i < len; i++)
      {
         c = name.charAt(i);
         if (!(Character.isJavaIdentifierPart(c) || c == '-') )
            return false;
      }
      return true;
   }

   /**
    * Returns <CODE>true</CODE> if and only if the given
    * String is a well-formed extension name.
    *
    * @param name The extension name to check for validity.
    * If <CODE>null</CODE>, <CODE>false</CODE> will be returned.
    *
    * @return <CODE>true</CODE> iff the given String is
    * a well-formed extension name.
    */
   public static boolean isValidExtensionName(String name)
   {
      if (name == null || name.trim().length() == 0)
         return false;

      char c = name.charAt(0);
      if (!Character.isJavaIdentifierStart(c))
         return false;

      final int len = name.length();
      for (int i = 1; i < len; i++)
      {
         c = name.charAt(i);
         if (!Character.isJavaIdentifierPart(c))
            return false;
      }
      return true;
   }

   /**
    * Returns the canonical version of this context.
    *
    * @param context A valid context name. Must not be <CODE>null</CODE>.
    *
    * @return A canonical version of this context.
    * @see #getContext
    */
   public static String canonicalizeContext(String context)
   {
      // break up into slash-separated components, each of which
      // must be a valid extension name
      StringBuilder buf = new StringBuilder(context.length());
      StringTokenizer toker = new StringTokenizer(context, "/");
      while (toker.hasMoreTokens())
      {
         buf.append(toker.nextToken());
         buf.append('/');
      }      

      return buf.toString();
   }

   /**
    * Returns an object that implements Comparator and is capable of comparing
    * two PSExtensionRef objects.
    *
    * @return The Comparator.  Never <code>null</code>, compares the two objects
    * lexicographically, ignoring case.
    */
   public static Comparator getComparator()
   {
      return new PSExtensionRefComparator();
   }

   /**
    * Checks for a valid extension's full Name. The extension full name is expected
    * to be formed as handlerName/context/extensionName.
    * @param fullName an extension's name to be checked for validity. Can
    * be <CODE>null</CODE>
    * @return <CODE>true</CODE> if the extension's name is a valid name
    * otherwise <CODE>false</CODE>
    */
   public static boolean isValidFullName(String fullName)
   {
      boolean isValid = false;
      if(fullName == null)
         return isValid;

      String [] parsedName = parseFullName(fullName);
      
      if(parsedName != null)
      {
         String handlerName = parsedName[0];
         String context = parsedName[1];
         String extName = parsedName [2];
         if(isValidExtensionName(handlerName) &&
            isValidContext(context) &&
            isValidExtensionName(extName))
               isValid = true;
      }
      return isValid;
   }

   /**
    * A private utility method to parse a full extension name (in the form
    * returned by toString()) into its three components. The return value
    * is an array of 3 strings, where the first element is the handler
    * name, the second element is the context, and the third element is
    * the extension name.
    * <P>
    * <STRONG>Note</STRONG> : Full validation is not done on the individual
    * components, so the caller should still subject them to validation using
    * the isValidExtensionName and canonicalizeContext methods.
    *
    * @param fullName The full name. Must not be <CODE>null</CODE>.
    *
    * @return An array of 3 Strings. Can be <CODE>null</CODE>.
    */
   private static String[] parseFullName(String fullName)
   { 
      int firstSlash = fullName.indexOf('/');
      boolean isValid = true;
      if (-1 == firstSlash || firstSlash == (fullName.length() - 1))
          isValid = false;

      int lastSlash = fullName.lastIndexOf('/');
      if (-1 == lastSlash || lastSlash == firstSlash ||
         lastSlash == (fullName.length() - 1))
            isValid = false;

      String [] ret = null;
      if(isValid)
      {
         ret = new String[3];
         ret[0] = fullName.substring(0, firstSlash);
         ret[1] = fullName.substring(firstSlash + 1, lastSlash);
         ret[2] = fullName.substring(lastSlash + 1);
      }
      return ret;
   }

   /**
    * A private utility method to validate the handler name components
    * and then initialize the proper member variables.    
    * @param handlerName The handler name. Must not be <CODE>null</CODE>.
    * @param context The context. Must not be <CODE>null</CODE>.
    * @param extName The extension name. Must not be <CODE>null</CODE>.
    * @param category the category of the extension.
    * Never <CODE>null</CODE>, but it can be an empty string
    * @throws IllegalArgumentException If any param is invalid.
    */
   private void init(String handlerName, String context, String extName, String category)
   {
      // handlers are extensions, too, so their names are subject to validation
      if (!isValidExtensionName(handlerName))
      {
         throw new IllegalArgumentException(handlerName +
            " is not a valid extension handler name.");
      }

      if (!isValidContext(context))
      {
         throw new IllegalArgumentException(context +
            " is not a valid context.");
      }

      if (!isValidExtensionName(extName))
      {
         throw new IllegalArgumentException(extName +
            " is not a valid extension name.");
      }
      if(category == null)
         throw new IllegalArgumentException(
            "Category can not be null");
      m_handlerName = handlerName;
      m_context = canonicalizeContext(context);
      m_extName = extName; 
      m_category = category;
   }


   /**
    * The extension handler name.
    * Never <CODE>null</CODE>, always well-formed.
    */
   private String m_handlerName;

   /**
    * The extension context.
    * Never <CODE>null</CODE>, always canonical and well-formed.
    *
    * @see #canonicalizeContext
    */
   private String m_context;

   /** The extension name. Never <CODE>null</CODE>, always well-formed. */
   private String m_extName;

   /**The category of the extension, might be empty string, but never
    <CODE>null</CODE>*/
   private String m_category = "";

   /**
    * A class that can compare two PSExtensionRef objects
    * lexicographically by extension name.
    */
   private static class PSExtensionRefComparator implements Comparator
   {
      /**
       * Compares the extension names of two PSExtensionRef objects
       * lexicographically, case insensitive.
       *
       * @param o1 The first PSExtensionRef object.  May not be <code>null
       * </code>.
       * @param o2 The second PSExtensionRef object. May not be <code>null
       * </code>.
       *
       * @return a negative integer, zero, or a positive integer as the
       * first object's name is less than, equal to, or greater than the second
       * lexicographically, case insensitive.
       *
       * @throws IllegalArgumentException if either argument is <code>null
       * </code>.
       * @throws ClassCastException if either parameter is not an instance of
       * a PSExtensionRef object.
       */
      public int compare(Object o1, Object o2)
      {
         if (o1 == null || o2 == null)
            throw new IllegalArgumentException("one or more params is null");
            
         return ((PSExtensionRef)o1).getExtensionName().compareToIgnoreCase(
            ((PSExtensionRef)o2).getExtensionName());
      }
   }

}
