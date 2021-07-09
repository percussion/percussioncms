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
package com.percussion.utils.xml;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Utility class for DOM XML processing
 */
public class PSXmlUtils
{
   /**
    * Get the element data
    * 
    * @param source The element, may be <code>null</code>.
    * @param name The name of the expected element, may not be <code>null</code> 
    * or empty.
    * @param required <code>true</code> if an value is required, 
    * <code>false</code> if not.
    * 
    * @return The data, <code>null</code> if the element is <code>null</code>, 
    * may be empty if the value of the element is empty.
    * 
    * @throws PSInvalidXmlException if <code>required</code>
    * is <code>true</code> and either the supplied element is 
    * <code>null</code> or the value of the element is empty.
    */
   public static String getElementData(Element source, String name,
      boolean required) throws PSInvalidXmlException
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
    
      String data = null; 
         
      if (source == null)
      {
         if (required)
         {
            throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
               name);
         }
         
         return null;
      }
      
      data = PSXmlTreeWalker.getElementData(source);
      if (required && StringUtils.isEmpty(data))
      {
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_INVALID_VALUE, 
            new String[] {name, data});
      }
      
      return data;
   }
   
   /**
    * Static helper method to check an attribute of a specific element.
    * 
    * @param el the element to get the attribute from, must not be <code>null
    * </code>
    * 
    * @param name the name of the attribute to retrieve, must not be <code>
    * null</code>
    *           or empty
    * 
    * @param required a boolean flag to determine if we throw an error if it
    *           does not exist or just return blank, if true, we throw an error,
    *           otherwise we just return "" if not found
    * 
    * @return the value of the specified attribute or blank if not found and not
    *         required
    * 
    * @throws PSInvalidXmlException if the attribute was not found and was
    *            required
    */
   public static String checkAttribute(Element el, String name, 
      boolean required) throws PSInvalidXmlException
   {
      if (el == null || name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
               "arguments must not be null or empty");

      String val = el.getAttribute(name);
      if (required && (val == null || val.trim().length() == 0))
      {
         Object[] args =
         {el.getNodeName(), name, val};
         throw new PSInvalidXmlException(
               IPSXmlErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      return (val == null) ? "" : val;
   }

   /**
    * Static helper method to check an attribute of a specific element and
    * returns an integer of that value.
    * 
    * @param el the element to get the attribute from, must not be <code>null
    * </code>
    * 
    * @param name the name of the attribute to retrieve, must not be <code>
    * null</code>
    *           or empty
    * 
    * @param required a boolean flag to determine if we throw an error if it
    *           does not exist or just return -1, if true, we throw an error,
    *           otherwise we just return -1 if not found
    * 
    * @return the value of the specified attribute or -1 if not found and not
    *         required
    * 
    * @throws PSInvalidXmlException if the attribute was not found and was
    *            required
    * 
    * @throws NumberFormatException if the value does not contain a parsable
    *            integer
    */
   public static int checkAttributeInt(Element el, String name, boolean required)
         throws PSInvalidXmlException, NumberFormatException
   {
      if (el == null || name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
               "arguments must not be null or empty");

      String val = checkAttribute(el, name, required);

      return (val == null || val.trim().length() == 0) ? -1 : Integer
            .parseInt(val);
   }

   /**
    * Save the Xml doc to the specified file.
    * 
    * @param serverFile The file to save to, may not be<code>null</code>.
    * @param doc The doc to save, may not be <code>null</code>.
    * 
    * @throws FileNotFoundException If the file cannot be opened for edit.
    * @throws IOException If there is an error writing to the file.
    */
   public static void saveDocToFile(File serverFile, Document doc) 
      throws FileNotFoundException, IOException
   {
      if (serverFile == null)
         throw new IllegalArgumentException("serverFile may not be null");
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      FileOutputStream out = new FileOutputStream(serverFile);
      try
      {
         PSXmlDocumentBuilder.write(doc, out);
      }
      finally
      {
         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (IOException e)
            {
            }
         }
      }
   }

   /**
    * Loads the supplied file as a document.
    * 
    * @param serverFile The file to load, may not be <code>null</code> and must
    * exist.
    * 
    * @return The document, never <code>null</code>. 
    * 
    * @throws IOException If there is an error reading from the file.     
    * @throws SAXException If the file is malformed.
    */
   public static Document getDocFromFile(File serverFile) 
      throws IOException, SAXException
   {
      if (serverFile == null)
         throw new IllegalArgumentException("serverFile may not be null");
      FileInputStream in = null;
      try
      {
         in = new FileInputStream(serverFile);
         return PSXmlDocumentBuilder.createXmlDocument(in, false);
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
            }
         }
      }
   }   
}

