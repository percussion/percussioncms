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


