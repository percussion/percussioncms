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

package com.percussion.deployer.error;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Exception class to used to report general exceptions, or may be subclassed
 * if necessary.  Handles formatting of messages stored in the
 * PSDeploymentErrorStringBundle resource bundle using error codes and 
 * arguments.  Localization is also supported.
 */
public class PSDeployException extends Exception
{

   /**
    * Eclipse was complaining
    */
   private static final long serialVersionUID = 4192931463127484706L;
   /**
    * Construct an exception for messages taking only a single argument.
    *
    * @param msgCode The code of the error string to load.
    *
    * @param singleArg The argument to use as the sole argument in
    *    the error message, may be <code>null</code>.
    */
   public PSDeployException(int msgCode, Object singleArg)
   {
      this(msgCode, new Object[] { singleArg });
   }

   /**
    * Same as {@link #PSDeployException(int, Object[])} but takes one additional
    * parameter to indicate the exception that caused this exception.
    * @param msgCode The code of the error string to load.
    * @param cause The original exception that caused this exception to be
    * thrown, may be <code>null</code>.
    * @param arrayArgs The array of arguments to use as the arguments
    *    in the error message.  May be <code>null</code>, and may contain
    *    <code>null</code> elements.
    */

   public PSDeployException(int msgCode, Throwable cause, Object... arrayArgs)
   {
      this(msgCode, arrayArgs);
      fillInStackTrace();
      initCause(cause);
   }
   /**
    * Construct an exception for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode The code of the error string to load.  
    *
    * @param arrayArgs The array of arguments to use as the arguments
    *    in the error message.  May be <code>null</code>, and may contain
    *    <code>null</code> elements.
    */
   public PSDeployException(int msgCode, Object[] arrayArgs)
   {
      for (int i = 0; arrayArgs != null && i < arrayArgs.length; i++)
      {
         if (arrayArgs[i] == null)
            arrayArgs[i] = "";
      }
      
      m_code = msgCode;
      m_args = arrayArgs;
   }

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode The error string to load.
    */
   public PSDeployException(int msgCode)
   {
      this(msgCode, null);
   }
   
   /**
    * Construct an exception from a class derived from PSException.  The name of 
    * the original exception class is saved.
    *
    * @param ex The exception to use.  Its message code and arguments are stored
    * along with the original exception class name.  May not be 
    * <code>null</code>.
    */
   public PSDeployException(PSException ex)
   {
      this(ex.getErrorCode(), ex.getErrorArguments());
      m_originalExceptionClass = ex.getClass().getName();
   }

   
   /**
    * Construct an exception from its XML representation.  
    *
    * @param source The root element of this object's XML representation.  
    * Format expected is defined by the {@link #toXml(Document) toXml} method
    * documentation.  May not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>source</code> is 
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not 
    * represent a type supported by the class.
    */
   public PSDeployException(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
         
      if (!XML_NODE_NAME.equals(source.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, source.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      // get message code
      String sTemp = null;
      sTemp = source.getAttribute(XML_ATTR_MSG_CODE);
      try 
      {
         m_code = Integer.parseInt(sTemp);
      }
      catch (NumberFormatException e) 
      {
         Object[] args = { XML_NODE_NAME, XML_ATTR_MSG_CODE, sTemp == null ? 
            "null" : sTemp };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         
      }
      
      // get optional exception class
      sTemp = source.getAttribute(XML_ATTR_EXCEPTION_CLASS);
      if (sTemp != null && sTemp.trim().length() > 0)
         m_originalExceptionClass = sTemp;
       
      // get args
      List<String> argList = new ArrayList<String>();
      PSXmlTreeWalker tree = new PSXmlTreeWalker(source);
      Element arg = tree.getNextElement(XML_ELEMENT_ARG,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      while (arg != null)
      {
         argList.add(tree.getElementData());
         arg = tree.getNextElement(XML_ELEMENT_ARG, 
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
      m_args = argList.toArray();
   }

   /**
    * This method is called to create an XML element node with the
    * appropriate format for this object. The format is: 
    * 
    * <pre><code>
    * <!ELEMENT PSXDeployException (Arg*)
    * <!ATTLIST PSXDeployException 
    *    msgCode CDATA #REQUIRED
    *    exceptionClass CDATA #IMPLIED
    * >
    * <!ELEMENT Arg (#PCDATA)>
    * </code></pre>
    * 
    * @param doc The document to use to create the element, may not be 
    * <code>null</code>.
    * 
    * @return the newly created XML element node, never <code>null</code>
    * 
    * @throws IllegalArgumentException if <code>doc</code> is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
         
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_MSG_CODE, String.valueOf(m_code));
      if (m_originalExceptionClass != null)
         root.setAttribute(XML_ATTR_EXCEPTION_CLASS, m_originalExceptionClass);
      for (int i = 0; m_args != null && i < m_args.length; i++) 
      {
         if (m_args[i] == null)
            PSXmlDocumentBuilder.addEmptyElement(doc, root, XML_ELEMENT_ARG);
         else
            PSXmlDocumentBuilder.addElement(doc, root, XML_ELEMENT_ARG, 
               m_args[i].toString());
      }
      
      return root;
   }
   
   /**
    * Returns the localized detail message of this exception.
    *
    * @param locale The locale to generate the message in.  If <code>null
    *    </code>, the default locale is used.
    *
    * @return  The localized detail message, never <code>null</code>, may be 
    * empty.
    */
   public String getLocalizedMessage(Locale locale)
   {
      return createMessage(m_code, m_args, locale);
   }

   /**
    * Returns the localized detail message of this exception in the
    * default locale for this system.
    *
    * @return  The localized detail message, never <code>null</code>, may be 
    * empty.
    */
   public String getLocalizedMessage()
   {
      return getLocalizedMessage(Locale.getDefault());
   }

   /**
    * Returns the localized detail message of this exception in the
    * default locale for this system.
    *
    * @return  The localized detail message, never <code>null</code>, may be 
    * empty.
    */
   public String getMessage()
   {
      return getLocalizedMessage();
   }

   /**
    * Returns a description of this exception. The format used is
    * "ExceptionClass: ExceptionMessage"
    *
    * @return the description, never <code>null</code> or empty.
    */
   public String toString()
   {
      return this.getClass().getName() + ": " + getLocalizedMessage();
   }

   /**
    * Get the parsing error code associated with this exception.
    *
    * @return The error code
    */
   public int getErrorCode()
   {
      return m_code;
   }

   /**
    * Get the parsing error arguments associated with this exception.
    *
    * @return The error arguments, may be <code>null</code>.
    */
   public Object[] getErrorArguments()
   {
      return m_args;
   }

   /**
    * Get the stack trace for the specified exception as a string.
    *
    * @param t The throwable (usually an exception), never <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>t</code> is <code>null</code>.
    */
   public static String getStackTraceAsString(Throwable t)
   {
      if (t == null)
         throw new IllegalArgumentException("t may not be null");
      
      // for unknown exceptions, it's useful to log the stack trace
      StringWriter stackTrace = new StringWriter();
      PrintWriter writer = new PrintWriter(stackTrace);
      t.printStackTrace(writer);
      writer.flush();
      writer.close();
      
      return stackTrace.toString();
   }

   /**
    * Create a formatted message for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode The code of the error string to load.
    *
    * @param arrayArgs  The array of arguments to use as the arguments
    *    in the error message, may be <code>null</code> or empty.
    *
    * @param loc The locale to use, may be <code>null</code>, in which case the
    *    default locale is used.
    *
    * @return The formatted message, never <code>null</code>. If the appropriate
    *    message cannot be created, a message is constructed from the msgCode
    *    and args and is returned.
    *
    */
   private String createMessage(int msgCode, Object[] arrayArgs,
      Locale loc)
   {
      if (arrayArgs == null)
         arrayArgs = new Object[0];

      
      String msg = null;
      if (m_originalExceptionClass == null)      
         msg = getErrorText(msgCode, true, loc);
      
      if (msg != null)
      {
         try
         {
            msg = MessageFormat.format(msg, arrayArgs);
         }
         catch (IllegalArgumentException e)
         {
            // some problem with formatting
            msg = null;
         }
      }

      if (msg == null)
      {
         String sArgs = "";
         String sep = "";

         for (int i = 0; i < arrayArgs.length; i++) {
            sArgs += sep + arrayArgs[i].toString();
            sep = "; ";
         }
         
         if (m_originalExceptionClass != null)
            msg = m_originalExceptionClass + ": ";
         else
            msg = "";
         msg += String.valueOf(msgCode) + ": " + sArgs;
      }
      
      return msg;
   }
   

   /**
    * Get the error text associated with the specified error code.
    *
    * @param code The error code.
    *
    * @param nullNotFound  If <code>true</code>, return <code>null</code> if the
    *    error string is not found, if <code>false</code>, return the code as
    *    a String if the error string is not found.
    *
    * @param loc The locale to use, may be <code>null</code>, in which case the
    * default locale is used.
    *
    * @return the error text, may be <code>null</code> or empty.
    */
   public static String getErrorText(int code, boolean nullNotFound, Locale loc)
   {
      if (loc == null)
         loc = Locale.getDefault();

      try
      {
         ResourceBundle errList = getErrorStringBundle(loc);
         if (errList != null)
            return errList.getString(String.valueOf(code));
      }
      catch (MissingResourceException e)
      {
         /* don't exception, return below based on nullNotFound value */
      }

      return (nullNotFound ? null : String.valueOf(code));
   }

   /**
    * Returns a formatted string containing the test of all of the exceptions
    * contained in the supplied SQLException.
    * <p>There seems to be a bug in the Sprinta driver. We get an exception
    * for Primary key constraint violation, which has a sql warning as the
    * next exception (warning). But this next warning has a circular
    * reference to itself in the next link. So we check for this problem and
    * limit the max errors we will process to <code>20</code>.
    *
    * @param e The exception to process. If <code>null</code>, an empty
    *    string is returned.
    *
    * @return The string, never <code>null</code>, may be empty.
    */
   public static String formatSqlException(SQLException e)
   {
      if ( null == e )
         return "";
         
      StringBuffer errorText   = new StringBuffer();

      int errNo = 1;
      final int maxErrors = 20;
      for ( ; e != null && errNo <= maxErrors; )
      {
         errorText.append( "[" );
         errorText.append( errNo );
         errorText.append( "] " );
         errorText.append( e.getSQLState());
         errorText.append( ": " );
         errorText.append( e.getMessage());
         errorText.append( " " );
         SQLException tmp = e.getNextException();
         if ( e == tmp )
            break;
         else
            e = tmp;
         errNo++;
      }
      if ( errNo == maxErrors + 1 )
      {
         errorText.append( "[Maximum # of error messages (" );
         errorText.append( maxErrors );
         errorText.append(  ") exceeded. Rest truncated]" );
      }

      return errorText.toString();
   }

   /**
    * Gets the original exception class if one was supplied at construction. 
    *
    * @return The name of the class, or <code>null</code> if one has not
    * been supplied.
    */
   public String getOriginalExceptionClass()
   {
      return m_originalExceptionClass;
   }

   /**
    * This method is used to get the string resources hash table for a
    * locale. If the resources are not already loaded for the locale,
    * they will be.
    *
    * @param loc The locale, assumed not <code>null</code>.
    * 
    * @return the bundle, never <code>null</code>.
    */
   private static ResourceBundle getErrorStringBundle(Locale loc)
      throws MissingResourceException
   {
      if (ms_bundle == null)
      {
         ms_bundle = ResourceBundle.getBundle(
            "com.percussion.deployer.error.PSDeployErrorStringBundle", loc);
      }

      return ms_bundle;
   }

   /**
    * Constant for the root element name for this object when serialized to and
    * from XML.
    */
   public static final String XML_NODE_NAME = "PSXDeployException";
   
   /**
    * The error code of this exception, set during ctor, never modified after
    * that.
    */
   private int m_code;

   /**
    * The array of arguments to use to format the message with.  Set during
    * ctor, may be <code>null</code>, never modified after that.
    */
   private Object[] m_args;
   
   /**
    * If this exception was constructed from a <code>PSException</code> class,
    * this will contain the name of the class.  May be initialized during ctor,
    * otherwise <code>null</code>, never modified after that.
    */
   protected String m_originalExceptionClass = null;

   /**
    * The resource bundle containing error message formats.  <code>null</code>
    * until the first call to {@link #getErrorStringBundle(Locale)
    * getErrorStringBundle}, never <code>null</code> or modified after that
    * unless an exception occurred loading the bundle.
    */
   private static ResourceBundle ms_bundle = null;
   
   // xml serialization constants
   private static final String XML_ELEMENT_ARG = "Arg";
   private static final String XML_ATTR_MSG_CODE = "msgCode";
   private static final String XML_ATTR_EXCEPTION_CLASS = "exceptionClass";
}



