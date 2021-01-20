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
package com.percussion.test.xml;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The PSXmlDocumentMerger class is used to simplify the merging of
 * two XML documents by appending the contents of one to the other...
 * (merge the source into the destination)
 *
 * This class now supports merging elements below the root node.
 *
 * Usage: java com.percussion.xml.PSXmlDocumentMerger
 *    sourceFile destFile [mergeElementName [last | first | create]]
 *
 * If a merge element name is specified we will find the first instance of an 
 * element by that name in the destination doc and append the children of the 
 * first element in the source doc with the same name. If no element is
 * found in the destination doc and the <code>create</code> option is
 * specified, a new element with that name will be created and appended to
 * the destinations document element. Otherwise if no element is found is
 * reported as an error. If no option is defined this defaults to 
 * <code>first</code>.
 *  
 * The <code>last</code> option tells the merger to append the children of 
 * the first element in the source doc into the last occurence of the element 
 * with the specified name in the destination doc.
 */
public class PSXmlDocumentMerger
{
   /**
    * main method for this utility
    */
   public static void main(String[] args)
   {
      if ((args.length < 2) || (args.length > 4)) 
      {
         System.err.println("Usage: java PSXmlDocumentMerger sourceFile " +
               "destinationMergeFile [insertionElementName [last | first | " +
               "create]] (defaults to first if not specified)");
         System.exit(1);
      }

      Document source = null;
      Document destination = null;

      File sourceFile = new File(args[0]);
      File destFile = new File(args[1]);
      FileInputStream sourceIn = null;
      FileInputStream destIn = null;
      
      File file = null;
      try
      {
         file = sourceFile;
         sourceIn = new FileInputStream(sourceFile);
         file = destFile;
         destIn = new FileInputStream(destFile);
      }
      catch (FileNotFoundException e)
      {
         System.err.println("Failure to open file " + file);
         e.printStackTrace();
         System.exit(1);
      }

      try
      {
         file = sourceFile;
         source = PSXmlDocumentBuilder.createXmlDocument(sourceIn, false);
         sourceIn.close();
         file = destFile;
         destination = PSXmlDocumentBuilder.createXmlDocument(destIn, false);
         destIn.close();
      }
      catch (IOException e)
      {
         System.err.println("Failure to read file " + file);
         e.printStackTrace();
         System.exit(1);
      }
      catch (SAXException e)
      {
         System.err.println("Failure to read file " + file);
         e.printStackTrace();
         System.exit(1);
      }

      /* Merge the source into the destination */
      PSXmlTreeWalker walker = new PSXmlTreeWalker(source);
      Element dstRoot = destination.getDocumentElement();

      if (args.length >= 3)
      {
         String mergeElementName = args[2];
         
         NodeList kids = dstRoot.getElementsByTagName(mergeElementName);
         if ((kids != null) && (kids.getLength() > 0))
         {
            if ((args.length >= 4) && (args[3].equalsIgnoreCase("last")))
            {
               dstRoot = (Element) kids.item(kids.getLength() - 1);
            }
            else
            {
               /* If it isn't last, it's first! (default) */
               dstRoot = (Element) kids.item(0);
            }
         }
         else
         {
            if ((args.length >= 4) && (args[3].equalsIgnoreCase("create")))
            {
               dstRoot = destination.createElement(mergeElementName);
               destination.getDocumentElement().appendChild(dstRoot);
            }
            else
            {
               System.err.println("Specified element not found! (" + 
                  mergeElementName + ")");
               System.exit(1);
            }
         }
      }

      Element nextElement = null;

      if (args.length >= 3)
      {
         nextElement = walker.getNextElement(args[2]);
      }

      nextElement = walker.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      final List<Element> nodeList = new ArrayList<Element>();
      while (nextElement != null)
      {
         System.out.println("Adding child node for element - "
            + nextElement.getTagName());
         nodeList.add(nextElement);
         nextElement = walker.getNextElement(
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
      System.out.println("Done.");
      
      for (int i = 0; i < nodeList.size(); i++)
      {
         Node tmp = nodeList.get(i);
         Node importNode = destination.importNode(tmp, true);
         dstRoot.appendChild(importNode);
      }

      /* overwrite the destination file with the merged result */
      FileOutputStream destOut = null;
      try
      {
         destOut = new FileOutputStream(destFile);

         PSXmlDocumentBuilder.write(destination, destOut);

         destOut.close();
      }
      catch (IOException e)
      {
         System.err.println("Error writing to destination file " + destFile);
         e.printStackTrace();
         System.exit(1);
      } 
   }
}
