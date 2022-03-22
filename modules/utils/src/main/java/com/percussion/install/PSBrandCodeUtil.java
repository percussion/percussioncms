/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.install;

// java

import com.percussion.xml.PSXmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

/**
 * This class has general utility methods.
 */
public class PSBrandCodeUtil
{

   /**
    * Returns the value of the specified attribute of the given element.
    * If required is <code>true</code> and the attribute does not exist then
    * <code>CodeException</code> is thrown.
    * If required is <code>false</code> and the attribute does not exist then
    * <code>null</code> is returned.
    *
    * @param el the reference to the element whose attribute value is required,
    * may not be <code>null</code>
    * @param attrName the name of the attribute whose value is required, may not
    * be <code>null</code> or empty
    * @param required if the attribute is a required attribute of the element,
    * <code>true</code> if require, <code>false</code> if optional.
    *
    * @return the value of the specified attribute of the element,
    * if required is <code>true</code> then never <code>null</code> but may be empty
    * if required is <code>false</code> then may be <code>null</code> or empty
    *
    * @throws IllegalArgumentException if el is <code>null</code>
    * or attrName is <code>null</code> or empty
    * @throws CodeException if required is <code>true</code> and the element
    * does not have the specified attribute defined.
    */
   public static String getAttributeValue(Element el, String attrName, boolean required)
      throws CodeException
   {
      if (el == null)
         throw new IllegalArgumentException("el may not be null");
      if ((attrName == null) || (attrName.trim().length() < 1))
         throw new IllegalArgumentException("attrName may not be null or empty");
      if (!el.hasAttribute(attrName))
      {
         if (required)
         {
            throw new CodeException("Attribute : " + attrName +
               " is not specoified for the element : " +
               el.getTagName());
         }
         else
         {
            return null;
         }
      }
      return el.getAttribute(attrName);
   }

   /**
    * Returns the reference to the first child element of the given parent
    * element <code>parentElement</code> whose tag name matches
    * <code>elementName</code>
    * @param parentElement the reference to the parent element, may not be
    * <code>null</code>
    * @param elementName the tag name of the child element whose reference
    * is required
    *
    * @return the reference to the child element, never <code>null</code>.
    * @throws IllegalArgumentException if parentElement is <code>null</code>
    * or elementName is <code>null</code> or empty
    * @throws CodeException if the child element cannot be found.
    */
   public static Element getRequiredChildElement(Element parentElement, String elementName)
      throws CodeException
   {
      if (parentElement == null)
         throw new IllegalArgumentException("parentElement may not be null");
      if ((elementName == null) || (elementName.trim().length() < 1))
         throw new IllegalArgumentException("elementName may not be null or empty");
      NodeList nl = parentElement.getElementsByTagName(elementName);
      if ((nl == null) || (nl.getLength() == 0))
         throw new CodeException("Failed to find child element : " +
            elementName + " under the parent element : " +
            parentElement.getTagName());
      return (Element)nl.item(0);
   }

    /**
     * Serializes an Xml document to the specified file.
     * @param doc the document to write to the file, may not be <code>null</code>
     * @param filePath the path of the file to which this object should
     * be serialized, may not be <code>null</code> or empty.
     * @throws IllegalArgumentException if filePath is <code>null</code> or empty,
     * or if doc is <code>null</code>
     * @throws IOException
     * @throws ParserConfigurationException
     */
   public static void write(Document doc, String filePath)
      throws IOException, ParserConfigurationException
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      if ((filePath == null) || (filePath.trim().length() < 1))
         throw new IllegalArgumentException("filePath may not be null or empty");
      File f = new File(filePath);
      if (!f.isFile())
         f.createNewFile();
      Writer out = new FileWriter(f);
      String output = PSXmlUtil.toString(doc);
      out.write(output);
   }

   /**
    * Creates a empty Xml document.
    * @return empty Xml document, never <code>null</code>
    * @throws ParserConfigurationException if the parser is not configured
    * correctly in the xerces jar file
    */
   public static Document createXmlDocument()
          throws ParserConfigurationException
   {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      dbf.setValidating(false);
      DocumentBuilder db = dbf.newDocumentBuilder();
      return db.newDocument();
   }

   /**
    * Create an XML document by parsing the specified input stream.
    * @param in the byte input stream to read from, not <code>null</code>
    * @return the document created by parsing the input stream.
    * @throws IllegalArgumentException if <code>in</code> is <code>null</code>
    * @throws IOException if an I/O error occurs
    * @throws SAXException if a parsing error occurs
    * @throws ParserConfigurationException if the parser is not configured
    * correctly in the xerces jar file
    */
   public static Document createXmlDocument(InputStream in)
      throws IOException, SAXException, ParserConfigurationException
   {
      if (null == in)
         throw new IllegalArgumentException("Input stream may not be null");
      // We will use xerces to create the document, as we do not want
      // this class to depend upon com.percussion.xml package.
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
      dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
      dbf.setNamespaceAware(true);
      dbf.setValidating(false);
      DocumentBuilder db = dbf.newDocumentBuilder();

      return db.parse(in);
   }

   /**
    * Returns the integer represented by the string argument.
    * @param convertStr the String containing the integer, may not be
    * <code>null</code> or empty.
    * @param errMsg the error message to be passed on to the generated
    * CodeException if any error occurs converting the string to an integer,
    * may not be <code>null</code> or empty.
    * @return the integer represented by the string argument.
    * @throws CodeException if if any error occurs converting the string to
    * an integer.
    * @throws IllegalArgumentException if convertStr or errMsg is
    * <code>null</code> or empty.
    */
   public static int toInt(String convertStr, String errMsg)
      throws CodeException
   {
      if ((convertStr == null) || (convertStr.trim().length() < 1))
         throw new IllegalArgumentException(
            "convertStr may not be null or empty");
      if ((errMsg == null) || (errMsg.trim().length() < 1))
         throw new IllegalArgumentException(
            "errMsg may not be null or empty");

      int ret = 0;
      try
      {
         ret = Integer.parseInt(convertStr);
      }
      catch (NumberFormatException nfe)
      {
         throw new CodeException(errMsg);
      }
      return ret;
   }
}



