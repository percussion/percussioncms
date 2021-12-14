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

package com.percussion.xmldom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;


/**
 * The PSXmlTreeWalker class is used to simplify processing of XML trees.
 * @deprecated Use com.percussion.xml.PSXmlTreeWalker instead
 */
public class PSXmlTreeWalker
   extends com.percussion.xml.PSXmlTreeWalker
   implements Serializable 
{
   /**
    * @see com.percussion.xml.PSXmlTreeWalker#PSXmlTreeWalker(Document)
    */
   public PSXmlTreeWalker(Document doc)
   {
      super(doc);
   }

   /**
    * @see com.percussion.xml.PSXmlTreeWalker#PSXmlTreeWalker(Element)
    */
   public PSXmlTreeWalker(Element root)
   {
      super(root);
   }

   /**
    * @see com.percussion.xml.PSXmlTreeWalker#PSXmlTreeWalker(Node)
    */
   public PSXmlTreeWalker(Node root)
   {
      super(root);
   }

   /**
    * @see com.percussion.xml.PSXmlTreeWalker#write(Writer)
    */
   public void write(Writer out)
   {
      write(out,true);
   }
   
   /**
    * @see com.percussion.xml.PSXmlTreeWalker#write(Writer, boolean)
    */
   public void write(Writer out, boolean indentFlag)
   {
      try
      {
         super.write(out, indentFlag);
      }
      catch (IOException e)
      {
      }
   }
}
