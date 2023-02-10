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

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


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
