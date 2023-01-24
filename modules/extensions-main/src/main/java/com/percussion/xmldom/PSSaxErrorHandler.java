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
package com.percussion.xmldom;

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
      m_errors = new ArrayList();
      m_fatalErrors = new ArrayList();
      m_warnings = new ArrayList();
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
    if(m_printWriter != null) {
      m_printWriter.println("Parser Error: "+exception.toString());
    }

      m_errors.add(exception);

      if (m_throwErrors)
         throw exception;
   }

   // report a fatal error
   public void fatalError(SAXParseException exception) throws SAXException
   {
    if(m_printWriter != null) {
       m_printWriter.println("Parser Fatal Error: "+exception.toString());
    }
      m_fatalErrors.add(exception);

      if (m_throwFatalErrors)
         throw exception;
   }

   // report a warning
   public void warning(SAXParseException exception) throws SAXException
   {
    if(m_printWriter != null) {
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

   public Iterator warnings()
   {
      return m_warnings.iterator();
   }

   private List m_errors;
   private List m_fatalErrors;
   private List m_warnings;
   private PrintWriter m_printWriter;

   private boolean m_throwFatalErrors = true;
   private boolean m_throwErrors = false;
   private boolean m_throwWarnings = false;
}
