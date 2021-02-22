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
package com.percussion.xmldom;

import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;


/**
 * This class provides centralized parameter passing, exception handling
 * and trace message support for the xmldom package.
 */
public class PSXmlDomContext
{
   /**
    * The originating request for the extension
    */
   private IPSRequestContext m_req = null;

   /**
    * The Tidy Properties for this request
    **/
   private Properties m_tidyProperties = new Properties();

   /**
    * ServerPageTags file is used to parse the "ASP/JSP" tags in a file.
    **/
   private String m_serverPageTagsFile = null;

   /**
    * are we logging output to files? used for debugging
    **/
   private boolean m_logging = false;

   /**
    * the extension name determines which function we are performing. This is
    * used for error handling.
    **/
   private String m_function;

   /**
    * determine whether to use the Validating parser or not
    */
   private boolean m_validate = false;

   /**
    * Flag to determine whether to use tidy pretty print or not,
    * default is <code>true</code>.
    */
   private boolean m_pprint = true;
   
   /**
    * Option that specifies whether comments are handled the standard way or
    * rhythmyx specific. This is used to handle server side includes in body
    * fields. Defaults to <code>false</code> which mean comments are handled
    * as usual.
    */
   private boolean m_rxCommentHandling = false;

   /**
    * create a context where an IPSRequestContext is not available. This is
    * primarily used when debugging.
    **/
   public PSXmlDomContext(String functionName)
   {
      m_function = functionName;
      m_logging = false;
   }


   /**
    * create a context for the extension.  The context should be a be a member
    * variable of the Process method in each extension.
    **/
   public PSXmlDomContext(String functionName, IPSRequestContext req)
   {
      m_req = req;
      m_logging = req.isTraceEnabled();
      m_function = functionName;
   }

   /**
    * Are comments handled rhythmyx specific or not?
    * 
    * @return <code>true</code> if comments are handled rhythmyx specific,
    *    <code>false</code> otherwise.
    */
   public boolean rxCommentHandling()
   {
      return m_rxCommentHandling;
   }
   
   /**
    * Set whether comments are handled rhythmyx specific or not.
    * 
    * @param value <code>true</code> to handle comments rhythmyx specific,
    *    <code>false</code> otherwise.
    */
   public void setRxCommentHandling(boolean value)
   {
      m_rxCommentHandling = value;
   }

   /**
    * Allows support routines to print trace messages without knowing about
    * the IPSRequestContext
    */
   public void printTraceMessage(String msg)
   {
      if (!m_logging) return;

      if (m_req != null)
         m_req.printTraceMessage(msg);
      else
         System.out.println(msg);
   }


   /**
    * Gets the server's root value from the request.
    *
    * @return server root string in the format:
    *         127.0.0.1:&lt;port_nr>/&lt;rootname>
    */
   public String getServerRoot()
   {
      if (null == m_req)
         return "127.0.0.1:9992/Rhythmyx"; // defaults
      else
         return "127.0.0.1:" + m_req.getServerListenerPort()
               + PSServer.getRequestRoot();
   }


   /**
    * get the tidy properties for this operation context
    **/
   public Properties getTidyProperties()
   {
      return m_tidyProperties;
   }


   /**
    * set the tidy properties for this operation context
    **/
   public void setTidyProperties(Properties props)
   {
      m_tidyProperties = props;
   }


   /**
    * set the tidy properties from a file
    */
   public void setTidyProperties(String FileName) throws IOException
   {
      IPSRhythmyxInfo rxInfo = PSRhythmyxInfoLocator.getRhythmyxInfo();
      String rxRootDir = (String) rxInfo
            .getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY);
      try(InputStream in = new FileInputStream(rxRootDir + File.separator + FileName)){
         m_tidyProperties.load(in);
      }
   }


   /**
    * determine if Tidy processing is enabled for this context
    **/
   public boolean isTidyEnabled()
   {
      return (null != m_tidyProperties && !m_tidyProperties.isEmpty());
   }


   /**
    * Sets the Server page tags file name for this context
    *
    * @param filename the name of the server page tags file, 
    * it may be <code>null</code>, which will disable the user
    * of server page tags.
    */
   public void setServerPageTags(String filename)
   {
      m_serverPageTagsFile = filename;
   }


   /**
    * Gets the current ServerPageTags file name
    *
    * @return the name of the server page tags file, or <code>null</code> if
    *         one hasn't been assigned
    **/
   public String getServerPageTags()
   {
      return m_serverPageTagsFile;
   }


   /**
    * get the state of serverPageTags.
    *
    * @return true if a ServerPageTags.xml file has been defined
    **/
   public boolean isServerPageTags()
   {
      return (null != m_serverPageTagsFile && m_serverPageTagsFile.length() > 0);
   }


   /**
    * set the state of the logging flag
    *
    * @param logFlag a boolean determining if logging is set.  The logging flag
    *      will be automatically enabled by the constructor if the
    *      IPSRequestContext.isTraceEnabled method returns <code>true</code>
    **/
   public void setLogging(boolean logFlag)
   {
      m_logging = logFlag;
   }


   /**
    * read the logging flag
    *
    * @return <code>true</code> if logging is enabled; <code>false</code>
    *         otherwise
    */
   public boolean isLogging()
   {
      return m_logging;
   }

   /**
    * Sets the Validate flag. If this flag is <code>true</code>, use the
    * validating parser.
    */
   public void setValidate(boolean validate)
   {
      m_validate = validate;
   }

   /**
    * @return <code>true</code> when the validating parser should be used;
    * <code>false</code> when the non-validating parser should be used.
    */
   public boolean isValidate()
   {
      return m_validate;
   }

   /**
    * Sets the use tidy pprint flag. If this flag is <code>true</code>, use the
    * tidy pretty print.
    */
   public void setUsePrettyPrint(boolean pprint)
   {
      m_pprint = pprint;
   }

   /**
    * @return tidy pretty print flag.
    */
   public boolean getUsePrettyPrint()
   {
      return m_pprint;
   }

   /**
    * Prints exception context to the trace file, and optionally throws
    * a new PSExtensionProcessingException.
    * This standard exception handler should be used at the "main" level of
    * each extension to catch all unexpected exceptions.
    *
    * @param e the exception to log
    * @param throwException flag that if <code>true</code> will cause an
    *        exception to be thrown
    * @throws PSExtensionProcessingException if throwException is
    *         <code>true</code>
    */
   public void handleException(Exception e, boolean throwException)
         throws PSExtensionProcessingException
   {
      StringBuffer estr = new StringBuffer("Unexpected exception in ");
      estr.append(m_function).append("\n");
      estr.append(e.toString()).append("\n");
      estr.append(e.getMessage().toString()).append("\n");

      //print the stack trace into the tracing log.
      StringWriter stackWriter = new StringWriter();
      e.printStackTrace(new PrintWriter((Writer) stackWriter, true));
      estr.append(stackWriter.toString());

      printTraceMessage(estr.toString());

      if (throwException)
         throw new PSExtensionProcessingException(m_function, e);
   }


   /**
    * Prints exception context to the trace file and throws a new exception.
    * This standard exception handler should be used at the "main" level of
    * each extension to catch all unexpected exceptions.
    *
    * @param e the exception to log
    * @throws PSExtensionProcessingException always
    */
   public void handleException(Exception e)
         throws PSExtensionProcessingException
   {
      handleException(e, true);
   }
}
