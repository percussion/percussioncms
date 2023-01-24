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

package com.percussion.server.agent;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Simple Utility class for storing static methods and variables.
 */
public class PSUtils
{
   /**
    * Helper function that returns the W3C DOM Element value in the tree.
    *
    * @param elem - the Element
    * @param elemname  the element tag name, can be <code>null</code>.
    *
    * @return  the value of the element, can be <code>empty</code>.
    *
    */
   public static String getElemValue(Element elem, String elemname)
   {
      String value = "";

      if(null == elem || null == elemname || elemname.trim().length()<1)
         return value;

      NodeList nl = elem.getElementsByTagName(elemname);
      if(null == nl || nl.getLength() < 1)
         return value;

      //we consider only the first text child for element value.
      Element child = (Element)nl.item(0);
      Node node = child.getFirstChild();
      if(null == node || Node.TEXT_NODE != node.getNodeType())
         return value;

      return ((Text)node).getData().trim();
   }

   /**
    * Get the program resources. If the resource bundle is already loaded,
    * returns the loaded one otherwise tries to load a new one.
    *
    * @return resource bundle as ResourceBundle never <code>null</code>.
    *
    * @throws MissingResourceException if it cannot find the reosurces file.
    *
    */
   public static ResourceBundle getRes()
      throws MissingResourceException
   {
      if(null == ms_Res)
      {
         ms_Res = ResourceBundle.getBundle("com.percussion.server.agent." +
            "PSStringResources", Locale.getDefault());
      }
      return ms_Res;
   }

   /**
    * Returns DocumentBuilder object for parsing XML documents
    * @return DocumentBuilder object for parsing XML documents. Never
    * <code>null</code>
    */
   public static DocumentBuilder getDocumentBuilder()
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
         dbf.setValidating(false);
         return dbf.newDocumentBuilder();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.getMessage());
      }
   }

   /**
    * Program resource bundle containng all the strings and messages
    */
   private static ResourceBundle ms_Res = null;

   /**
    * The namespace for agents
    */
   public static  final String NS_URI_PERCUSSION_AGENT =
                           "urn:www.percussion.com/agent";

   /**
    * Default date format string we use through the agent manager
    *
    */
   public static final String DEFAULT_DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

   /**
    * New line characters
    *
    */
   public static final String NEWLINE = "\r\n";

   /**
    * default Java character encoding
    *
    */
   public static final String DEFAULT_JAVA_CHAR_ENC = "UTF8";

   /**
    * Publisher default timeout for http requests (in milliseconds).
    */
   public static final int DEFAULT_TIMEOUT_MILLIS = 100000;
}
