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

package com.percussion.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An error handler for JAXP Transformers which keeps the errors in
 * a list accessible after transformation is complete.
 */
public class PSTransformErrorListener implements ErrorListener
{
   /**
    * Construct a new XSL Transform error listener/handler. This handler will
    * not throw on warnings or errors or fatal errors, but will simply
    * record and keep track of them.
    * <P>
    * You can change the throw behavior of the error handler
    * using the throwOnFatalErrors and similar methods.
    * If fatalError doesn't throw an exception the transformation continues,
    * but the output may not be correct.
    *
    * @see #throwOnWarnings
    * @see #throwOnErrors
    * @see #throwOnFatalErrors
    */
   public PSTransformErrorListener()
   {
   }
   
   /**
    * Use this constructor when parser errors should be written usually to
    * a log or trace file, Otherwise can use default constructor.
    * Wraps the passed in writer instance to <code>PrintWriter</code> if it is
    * not of type <code>PrintWriter</code> and sets autoflushing to
    * <code>true</code>.
    *
    * @param pw The writer to log the warnings, errors and fatal errors, may
    * not be <code>null</code>.
    *
    * @throws IllegalArgumentException if the writer is <code>null</code>.
    *
    * @see #throwOnWarnings
    * @see #throwOnErrors
    * @see #throwOnFatalErrors
    */
   public PSTransformErrorListener(Writer pw)
   {
      if(pw == null)
         throw new IllegalArgumentException(
            "The writer to which errors should be written can not be null.");

      if(pw instanceof PrintWriter)
         m_printWriter = (PrintWriter)pw;
      else
         m_printWriter = new PrintWriter(pw, true);
   }

   /**
    * Sets throw exception on fatal error flag.If it is set to <code>true</code>
    * exception will be thrown when fatal error happens during transformation
    * otherwise not. The default value is <code>false</code>.
    *
    * @param shouldThrow throw exception on fatal error flag
    **/
   public void throwOnFatalErrors(boolean shouldThrow)
   {
      m_throwFatalErrors = shouldThrow;
   }

   /**
    * Sets throw exception on error flag. If it is set to <code>true</code>,
    * exception will be thrown when error happens during transformation
    * otherwise not. The default value is <code>false</code>.
    *
    * @param shouldThrow throw exception on error flag
    **/
   public void throwOnErrors(boolean shouldThrow)
   {
      m_throwErrors = shouldThrow;
   }

   /**
    * Sets throw exception on warning flag. If it is set to <code>true</code>,
    * exception will be thrown when error happens during transformation
    * otherwise not. The default value is <code>false</code>.
    *
    * @param shouldThrow throw exception on warning flag
    **/
   public void throwOnWarnings(boolean shouldThrow)
   {
      m_throwWarnings = shouldThrow;
   }

   /**
    * Receive notification of an error during transformation. Adds the error to
    * the error list and logs the exception to the writer if it is specified.
    * <p>
    * Throws TransformerException if throw exception on error flag is set to
    * <code>true</code>.
    *
    * @see ErrorListener#error error
    *
    * @throws TransformerException when throw exception on error flag is set to
    * <code>true</code>.
    **/
   public void error(TransformerException exception) throws TransformerException
   {
      Logger l = LogManager.getLogger(getClass());
      l.error(exception);
      
      if(m_printWriter != null)
         m_printWriter.println("Parser Error: "+exception.toString());

      m_errors.add(exception);

      if (m_throwErrors)
         throw exception;
   }

   /**
    * Receive notification of a fatal error during transformation.Adds the error
    * to the error list and logs the exception to the writer if it is
    * specified.
    * Throws TransformerException if throw exception on fatal error flag is set
    * to <code>true</code>
    *
    * @see ErrorListener#fatalError  fatalError
    *
    * @throws TransformerException when throw exception on fatal error flag is
    * set to <code>true</code>
    **/
   public void fatalError(TransformerException exception)
                              throws TransformerException
   {
      Logger l = LogManager.getLogger(getClass());
      l.fatal(exception);
            
      if(m_printWriter != null)
         m_printWriter.println("Parser Fatal Error: "+exception.toString());

      m_fatalErrors.add(exception);

      if (m_throwFatalErrors)
         throw exception;
   }

   /**
    * Receive notification of a warning during transformation.
    * Logs the exception to the writer if it is specified. Throws
    * TransformerException if throw exception on warning flag is set to
    * <code>true</code>.
    * <p>
    * If the exception causing this exception is
    * <code>FileNotFoundException</code>, adds this to error list, otherwise
    * adds this to the warning list.
    *
    * @see ErrorListener#warning  warning
    *
    * @throws TransformerException when throw exception on warning flag is
    * set to <code>true</code>.
    **/
   public void warning(TransformerException exception)
                           throws TransformerException
   {
      Logger l = LogManager.getLogger(getClass());
      l.warn(exception);
            
      if(m_printWriter != null)
         m_printWriter.println("Parser Warning: " + exception.toString());

      if(exception.getException() instanceof FileNotFoundException)
         m_errors.add(exception);
      else
         m_warnings.add(exception);

      if (m_throwWarnings)
         throw exception;
   }

   /**
    * Removes all messages from warning, error and fatal error lists.
    **/
   public void clear()
   {
      m_errors.clear();
      m_warnings.clear();
      m_fatalErrors.clear();
   }

   /**
    * Returns number of errors.
    *
    * @return number of errors
    **/
   public int numErrors()
   {
      return m_errors.size();
   }

   /**
    * Returns number of fatal errors.
    *
     * @return number of fatal errors
    **/
   public int numFatalErrors()
   {
      return m_fatalErrors.size();
   }

   /**
    * Returns number of warnings.
    *
     * @return number of warnings
    **/
   public int numWarnings()
   {
      return m_warnings.size();
   }

   /**
    * Returns iterator of errors list.
    *
    * @return iterator of errors list, never <code>null</code>.
    **/
   public Iterator errors()
   {
      return m_errors.iterator();
   }

   /**
    * Returns iterator of fatal errors list.
    *
    * @return iterator of fatal errors list, never <code>null</code>.
    **/
   public Iterator fatalErrors()
   {
      return m_fatalErrors.iterator();
   }

   /**
    * Returns iterator of warnings list.
    *
    * @return iterator of warnings list, never <code>null</code>.
    **/
   public Iterator warnings()
   {
      return m_warnings.iterator();
   }

   /**
    * The list of errors, initialized to empty array list and never
    * <code>null</code> after that.
    **/
    private List m_errors = new ArrayList();

   /**
    * The list of fatal errors, initialized to empty array list and never
    * <code>null</code> after that.
    **/
   private List m_fatalErrors = new ArrayList();

   /**
    * The list of warnings, initialized to empty array list and never
    * <code>null</code> after that.
    **/
   private List m_warnings = new ArrayList();

   /**
    * The Print Writer to which warnings, errors and fatal errors should be
    * written if it is specified. Initialized to <code>null</code> and new
    * object is created in the constructor if user wants to write the messages
    * to the writer.
    **/
   private PrintWriter m_printWriter = null;

   /**
    * The flag for throwing exception on fatal errors, initially set to
    * <code>false</code>.
    * @see #throwOnFatalErrors
    **/
   private boolean m_throwFatalErrors = false;

   /**
    * The flag for throwing exception on errors, initially set to
    * <code>false</code>.
    * @see #throwOnErrors
    **/
   private boolean m_throwErrors = false;

   /**
    * The flag for throwing exception on warnings, initially set to
    * <code>false</code>.
    * @see #throwOnWarnings
    **/
   private boolean m_throwWarnings = false;
}
