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

package com.percussion.content;

import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;


/**
 * Similar to HTMLText, but this class specifically represents the context of
 * a CDATA within the HTML.
 */
public class HTMLCDATA extends HTMLText implements CDATASection
{
   /**
    * Initializes CDATASection node with an initial value within CDATA.
    */
   public HTMLCDATA( String initialValue )
   {
      super( );
      setData( initialValue );
   }


   /**
    * Default constructor.
    */
   public HTMLCDATA()
   {
      super();
   }

   /**
    * Defines this node as a CDATA Section.
    *
    * @return CDATA_SECTION_NODE.
    */
   public short getNodeType()
   {
      return CDATA_SECTION_NODE;
   }

   /**
    * @return The data string of this CDATA object.
    */
   public String getData() throws DOMException
   {
      return m_data.toString();
   }

   /**
    * @param data The data string to set/replace the data owned by this CDATA.
    */
   public void setData( String data ) throws DOMException
   {
      m_data.setLength( 0 );
      m_data.append( data );
   }

   private StringBuffer m_data = new StringBuffer();
}
