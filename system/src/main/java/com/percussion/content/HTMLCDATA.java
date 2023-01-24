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

   private StringBuilder m_data = new StringBuilder();
}
