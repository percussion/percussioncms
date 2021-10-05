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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * The PSStyleSheet class is used to provider simpler access to 
 * the style sheet associated with an XML document.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSStyleSheet
{
   /**
    * Create a style sheet reference.
    *
    * @param   doc      the XML document to get the style sheet from
    *
    * @exception MalformedURLException
    */
   public PSStyleSheet(Document doc)
      throws java.net.MalformedURLException
   {
      super();

      // get the PI node and break it up into the parts we need to know about
      m_PINode = getPINode(doc);
      if (m_PINode != null) {
         String piVal   = m_PINode.getData();
         int start      = piVal.indexOf('"') + 1;
         int end         = piVal.indexOf('"', start);
         m_type         = piVal.substring(start, end);

         start   = piVal.indexOf('"', end+1) + 1;
         end   = piVal.indexOf('"', start);
         piVal   = piVal.substring(start, end);
         m_URL = new URL(piVal);   // may throw MalformedURLException
      }
      else {
         m_URL      = null;
         m_type   = null;
      }
   }

   /**
    * Get the URL of the style sheet associated with this XML document.
    *
    * @return            the style sheet's URL or <code>null</code> if
    *                     a <code>stylesheet</code> processing instruction
    *                     does not exist in the doc
    */
   public java.net.URL getURL()
   {
      return m_URL;
   }

   /**
    * Get the type of the style sheet associated with this XML document.
    *
    * @return            the style sheet's type or <code>null</code> if
    *                     a <code>stylesheet</code> processing instruction
    *                     does not exist in the doc
    */
   public java.lang.String getType()
   {
      return m_type;
   }

   /**
    * Get the PI node containing the style sheet definition.
    *
    * @return            the PI node or <code>null</code> if
    *                     a <code>stylesheet</code> processing instruction
    *                     does not exist in the doc
    */
   public ProcessingInstruction getPINode()
   {
      return m_PINode;
   }

   /**
    * Get the PI node containing the style sheet definition for the
    * specified document.
    *
    * @param   doc      the XML document to get the stylesheet PI node from
    *
    * @return            the PI node or <code>null</code> if
    *                     a <code>stylesheet</code> processing instruction
    *                     does not exist in the doc
    */
   public static ProcessingInstruction getPINode(Document doc)
   {
      ProcessingInstruction pi = null;

      // get the PI node and break it up into the parts we need to know about
      for (Node styleNode = doc.getFirstChild(); styleNode != null; ) {
         if (styleNode instanceof org.w3c.dom.ProcessingInstruction) {
            pi = (ProcessingInstruction)styleNode;
            if ("xml-stylesheet".equalsIgnoreCase(pi.getTarget()))
               break;
            else
               pi = null;   // reset it 
         }
         styleNode = styleNode.getNextSibling();
      }

      return pi;
   }


   private org.w3c.dom.ProcessingInstruction   m_PINode;
   private java.net.URL                        m_URL;
   private java.lang.String                  m_type;
}

