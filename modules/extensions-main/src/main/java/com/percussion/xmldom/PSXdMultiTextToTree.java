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

import com.percussion.extension.IPSResultDocumentProcessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A Rhythmyx post-exit called to transform a text node into an XML tree
 * and add it to the result Document. This differs from the
 * <code>PSXdTextToTree</code> exit in that it allows user to create child
 * editors with multiple text editors. <code>PSXdTextToTree</code> exit
 * does not support multiple fields with the same name.
 */
public class PSXdMultiTextToTree extends PSXdTextToTree
   implements IPSResultDocumentProcessor
{
   /**
    * Returns a list which contains all the elements from the specified document
    * <code>resultDoc</code> with the specified tag name
    * <code>textSourceName</code>
    *
    * @param resultDoc the document in which to search for the elements with the
    * specified tag name, may not be <code>null</code>
    * @param textSourceName the tag name of the element to search for in the
    * document, the returned list contains the reference to all the elements
    * in the document with matching tag name, may not be <code>null</code> or
    * empty
    *
    * @return an iterator over all the matching elements in the document, never
    * <code>null</code>. The list contains <code>org.w3c.dom.Node</code> objects
    *
    * @throws IllegalArgumentException if <code>resultDoc</code> is
    * <code>null</code> or if <code>textSourceName</code> is <code>null</code>
    * or empty
    */
   protected Iterator getNodes(Document resultDoc, String textSourceName)
   {
      if (resultDoc == null)
         throw new IllegalArgumentException("resultDoc may not be null");

      if (textSourceName == null)
         throw new IllegalArgumentException(
            "textSourceName may not be null or empty");

      List nodeList = new ArrayList();
      NodeList nl = resultDoc.getElementsByTagName(textSourceName);
      for (int i=0; i < nl.getLength(); i++)
      {
         Node sourceNode = (Node)nl.item(i);
         if (sourceNode != null)
            nodeList.add(sourceNode);
      }
      return nodeList.iterator();
   }

   /**
    * The function name used for error handling
    */
   protected static final String ms_className = "PSXdMultiTextToTree";

}


