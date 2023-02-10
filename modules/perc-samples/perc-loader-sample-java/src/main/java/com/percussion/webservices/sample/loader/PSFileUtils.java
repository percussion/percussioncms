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
package com.percussion.webservices.sample.loader;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is a utility class, which is used to read the data files for the sample 
 * loader.
 */
public class PSFileUtils
{
   /**
    * Loads the Content Item data from the file specified by the path.
    * The XML file is expected conform to the following dtd:
    * <pre>
    * &lt;!ELEMENT Items (Item*)>
    * &lt;!ELEMENT Item (Field*)>
    * &lt;!ELEMENT Field (#PCDATA)>
    * &lt;!ATTLIST Field
    *    name CDATA #REQUIRED
    *    >
    * </pre>
    *
    * @param dataFile the path to the data file, defined relative to the
    *   location of this class.
    *
    * @return a list of Content Item data.  Each element in the returned data 
	*   set is a field name/value mapping.
    *
    * @throws Exception if any error occurs.
    */
   public static List<Map<String,String>> loadDataFile(String dataFile)
      throws Exception
   {
      Element root;

      if(Files.exists(Paths.get("./LoaderData.xml"))){
         root = loadLocalXMLFile("./LoaderData.xml");
      }else{
         root = loadXmlResource(dataFile);
      }

      List<Map<String,String>> items = new ArrayList<Map<String,String>>();

      NodeList nodes = root.getElementsByTagName("Item");
      for (int i=0; i < nodes.getLength(); i++)
      {
         Element itemEl = (Element) nodes.item(i);
         Map<String,String> fields = new HashMap<String,String>();
         NodeList fieldNodes = itemEl.getElementsByTagName("Field");
         for (int j=0; j < fieldNodes.getLength(); j++)
         {
            Element fieldEl = (Element) fieldNodes.item(j);
            String name = fieldEl.getAttribute("name");
            String value = getElementData(fieldEl);

            fields.put(name, value);
         }
         items.add(fields);
      }
      return items;
   }

   /**
    * Static helper function to get the element data of the specified node.
    *
    * @param node the node where the text data resides; may be <code>null</code>
    *           in which case this funtion will return ""
    *
    * @return the complete text of the specified node, or an empty string if the
    *         node has no text or is <code>null</code>
    */
   public static String getElementData(Node node)
   {
      StringBuilder ret = new StringBuilder();

      if (node != null)
      {
         Node text;
         for (text = node.getFirstChild(); text != null; text = text
               .getNextSibling())
         {
            /**
             * the item's value is in one or more text nodes which are its
             * immediate children
             */
            if (text.getNodeType() == Node.TEXT_NODE)
            {
               ret.append(text.getNodeValue());
            }
            else
            {
               if (text.getNodeType() == Node.ENTITY_REFERENCE_NODE)
               {
                  ret.append(getElementData(text));
               }
            }
         }
      }
      return ret.toString();
   }

   /**
    * Loads the loader properties from 'Loader.xml', which is located at the
    * same directory as this class. The content of the file is expected to
    * comply with the following dtd:
    * <pre>
    * &lt;!ELEMENT Loader (ConnectionInfo, LoginRequest, ContentType, DataFile, TargetFolder)>
    * &lt;!ELEMENT ConnectionInfo (Protocol, Host, Port)>
    * &lt;!ELEMENT Protocol (#PCDATA)>
    * &lt;!ELEMENT Host (#PCDATA)>
    * &lt;!ELEMENT Port (#PCDATA)>
    * &lt;!ELEMENT LoginRequest (Username, Password, Community)>
    * &lt;!ELEMENT Username (#PCDATA)>
    * &lt;!ELEMENT Password (#PCDATA)>
    * &lt;!ELEMENT Community (#PCDATA)>
    * &lt;!ELEMENT ContentType (#PCDATA)>
    * &lt;!ELEMENT DataFile (#PCDATA)>
    * &lt;!ELEMENT TargetFolder (#PCDATA)>
    * </pre>
    *
    * @return the properties specified from <code>Loader.xml</code> file,
    *    never <code>null</code> or empty.
    *
    * @throws Exception if any error occurs.
    */
   public static Properties getLoaderProperties() throws Exception
   {
      Element root;
      if(Files.exists(Paths.get("./Loader.xml"))){
          root = loadLocalXMLFile("./Loader.xml");
    }else{
          root = loadXmlResource("Loader.xml");
      }


      Properties props = new Properties();
      for (String name : LOADER_PROP_NAMES)
      {
         String value = getElementData(root, name);
         props.put(name, value);
      }
      return props;
   }

   /**
    * Gets the text value of the specified element.
    *
    * @param root the root of the element whose text is to be retrieved; assumed
    *   not to be <code>null</code>.
    * @param elementName the name of the element whose text is to be retrieved.
    *
    * @return the retrieved text value of the specified element.
    */
   private static String getElementData(Element root, String elementName)
   {
      NodeList nodes = root.getElementsByTagName(elementName);
      if (nodes.getLength() < 1)
         throw new RuntimeException("Cannot find element: " + elementName);

      return getElementData(nodes.item(0));
   }


   private static Element loadLocalXMLFile(String name) throws IOException, SAXException {
      try (InputStream in = Files.newInputStream(Paths.get(name))) {
         Document doc = createXmlDocument(in, false);
         return doc.getDocumentElement();
      }
   }


   /**
    * Loads the xml file into an xml document and returns the
    * root element.
    *
    * @param name the fully qualified name of the XML file to load; assumed not
    *   to be <code>null</code>.
    *
    * @return root element of the xml document, never <code>null</code>.
    *
    * @throws Exception on any error
    */
   private static Element loadXmlResource(String name) throws Exception
   {
         try (InputStream in = PSFileUtils.class.getResourceAsStream(name)) {
            Document doc = createXmlDocument(in, false);
            return doc.getDocumentElement();
         }
   }
   
   /**
    * Returns a <code>DocumentBuilder</code>, which is used for parsing XML
    * documents.
    * 
    * @param validating if <code>true</code> sets the validation feature to on;
    *    any other value sets the validation feature to off.
    *
    * @return a <code>DocumentBuilder</code> which is used for parsing XML
    *         documents. Never <code>null</code>.
    */
   private static DocumentBuilder getDocumentBuilder(boolean validating)
   {
      try
      {
         DocumentBuilderFactory dbf = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
                 new PSXmlSecurityOptions(
                         true,
                         true,
                         true,
                         false,
                         true,
                         false
                 ));

         dbf.setNamespaceAware(true);
         dbf.setValidating(validating);

         return dbf.newDocumentBuilder();
      }
      catch (ParserConfigurationException e)
      {
         e.printStackTrace();
         throw new RuntimeException(e.getMessage());
      }
   }

   /**
    * Create an XML document by parsing the specified input stream. 
    * 
    * @param in the byte input stream to read from; assumed not to be
    *    <code>null</code>
    * @param validate <code>true</code> to validate the document
    * 
    * @return the parsed documentl; never <code>null</code> but may be empty.
    * 
    * @throws IOException if an I/O error occurs
    * @throws SAXException if a parsing error occurs
    */
   private static Document createXmlDocument(InputStream in, boolean validate)
         throws IOException, SAXException
   {
      InputSource src = new InputSource(in);
      src.setEncoding("UTF-8");
      DocumentBuilder db = getDocumentBuilder(validate);
      Document doc = db.parse(in);
      return doc;
   }

   /**
    * All property names which are defined in 'Loader.xml'
    */
   public static String[] LOADER_PROP_NAMES = new String[]
   {
      PSLoader.PROTOCOL, PSLoader.HOST, PSLoader.PORT, PSLoader.USER_NAME,
      PSLoader.PASSWORD, PSLoader.COMMUNITY, PSLoader.CONTENT_TYPE,
      PSLoader.TARGET_FOLDER, PSLoader.DATA_FILE
   };
}
