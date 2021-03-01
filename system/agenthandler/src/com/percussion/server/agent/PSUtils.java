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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.server.agent;

import com.percussion.security.xml.PSSecureXMLUtils;
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
    * @param String - the element tag name, can be <code>null</code>.
    *
    * @return Strign - the value of the element, can be <code>empty</code>.
    *
    */
   static public String getElemValue(Element elem, String elemname)
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
   static public DocumentBuilder getDocumentBuilder()
   {
      try
      {
         DocumentBuilderFactory dbf = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
                 false);

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
   static public final String NS_URI_PERCUSSION_AGENT =
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
