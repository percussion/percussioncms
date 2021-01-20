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

package com.percussion.utils.xml;

import java.util.Iterator;
import java.util.List;

import org.xml.sax.SAXParseException;

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
