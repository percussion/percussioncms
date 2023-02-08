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

package com.percussion.utils.xml;

import org.xml.sax.SAXParseException;

import java.util.Iterator;
import java.util.List;

/**
 * This class extends SAXException in order to provide access to a
 * collection of SAXParseExceptions that are generated when parsing an Xml
 * document.
 */
public class PSSaxParseException extends SAXParseException
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Constructor, takes in an collection of SAXParseExceptions.
    * Calls <code>super()</code> using the first exception in the collection.
    *
    * @param parseExceptions PSCollection of one or more fatal or non-fatal
    * SAXParseExceptions returned when parsing a document.  May not be <code>
    * null</code> and must contain at least one SAXParseException.
    *
    * @throws NullPointerException if parseExceptions is <code>null</code>
    * @throws IndexOutOfBoundsException if parseExceptions is empty.
    * does not contain at least one SAXParseException.
    */
   public PSSaxParseException(List<SAXParseException> parseExceptions)
   {
      super(parseExceptions.get(0).getMessage(),
         parseExceptions.get(0).getPublicId(),
         parseExceptions.get(0).getSystemId(),
         parseExceptions.get(0).getLineNumber(),
         parseExceptions.get(0).getColumnNumber());

      m_errors = parseExceptions;
   }


   /**
    * Returns an iterator over one or more SAXParseExceptions.
    *
    * @return The iterator, never <code>null</code> or empty.
    */
   public Iterator getExceptions()
   {
      return m_errors.iterator();
   }

   /**
    * Collection of SAXParseExceptions, initialized by the ctor, never <code>
    * null</code> or empty after that.
    */
   private List<SAXParseException> m_errors = null;
}
