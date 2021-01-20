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
package com.percussion.xml;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An error handler for SAX parsers which keeps the errors in
 * a list accessible after parsing is complete.
 */
public class PSSaxErrorHandler implements ErrorHandler
{
   /**
    * Construct a new SAX error handler. This handler will
    * immediately throw on fatal errors, but will simply
    * record and keep track of non-fatal errors and warnings.
    * <P>
    * You can change the throw behavior of the error handler
    * using the throwOnFatalErrors and similar methods.
    */
   PSSaxErrorHandler()
   {
      m_errors = new ArrayList<SAXParseException>();
      m_fatalErrors = new ArrayList<SAXParseException>();
      m_warnings = new ArrayList<SAXParseException>();
      m_printWriter = null;
   }

   /**
    * Use this constructor when parser errors should be "printed", usually to
    * a log or trace file.
    */
   PSSaxErrorHandler(PrintWriter pw)
   {
      this();
      m_printWriter = pw;
   }

   public void throwOnFatalErrors(boolean shouldThrow)

   {
      m_throwFatalErrors = shouldThrow;
   }

   public void throwOnErrors(boolean shouldThrow)
   {
      m_throwErrors = shouldThrow;
   }

   public void throwOnWarnings(boolean shouldThrow)
   {
      m_throwWarnings = shouldThrow;
   }

   // report an error
   public void error(SAXParseException exception) throws SAXException
   {
      if(m_printWriter != null)
      {
         m_printWriter.println("Parser Error: "+exception.toString());
      }

      m_errors.add(exception);

      if (m_throwErrors)
         throw exception;
   }

   // report a fatal error
   public void fatalError(SAXParseException exception) throws SAXException
   {
      if(m_printWriter != null)
      {
         m_printWriter.println("Parser Fatal Error: "+exception.toString());
      }

      m_fatalErrors.add(exception);

      if (m_throwFatalErrors)
         throw exception;
   }

   // report a warning
   public void warning(SAXParseException exception) throws SAXException
   {
      if(m_printWriter != null)
      {
         m_printWriter.println("Parser Warning: " + exception.toString());
      }

      m_warnings.add(exception);

      if (m_throwWarnings)
         throw exception;
   }

   public int numErrors()
   {
      return m_errors.size();
   }

   public int numFatalErrors()
   {
      return m_fatalErrors.size();
   }

   public int numWarnings()
   {
      return m_warnings.size();
   }

   public Iterator errors()
   {
      return m_errors.iterator();
   }

   public Iterator fatalErrors()
   {
      return m_fatalErrors.iterator();
   }
   
   /**
    * Get copy of list of the fatal errors received by this handler.
    * 
    * @return A copy of the list, never <code>null</code>, may be empty.
    */
   public List<SAXParseException> getFatalErrorList()
   {
      return new ArrayList<SAXParseException>(m_fatalErrors);
   }

   /**
    * Get copy of list of the errors received by this handler.
    * 
    * @return A copy of the list, never <code>null</code>, may be empty.
    */
   public List<SAXParseException> getErrorList()
   {
      return new ArrayList<SAXParseException>(m_errors);
   }

   public Iterator warnings()
   {
      return m_warnings.iterator();
   }

   private List<SAXParseException> m_errors;
   private List<SAXParseException> m_fatalErrors;
   private List<SAXParseException> m_warnings;
   private PrintWriter m_printWriter;

   private boolean m_throwFatalErrors = true;
   private boolean m_throwErrors = false;
   private boolean m_throwWarnings = false;
}
