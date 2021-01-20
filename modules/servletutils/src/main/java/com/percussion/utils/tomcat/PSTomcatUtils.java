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

package com.percussion.utils.tomcat;

import com.percussion.utils.container.IPSConnector;
import com.percussion.utils.xml.IPSXmlErrors;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.utils.xml.PSXmlUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class to load and save HTTP based connectors from a Tomcat server.xml
 * file.  See {@link PSTomcatConnector}.
 */
public class PSTomcatUtils
{
   /**
    * Private ctor to enforce static use.
    */
   private PSTomcatUtils()
   {
      
   }

   /**
    * Get list of all configured HTTP connectors.  These are connectors with
    * a scheme of {@link PSTomcatConnector#SCHEME_HTTP} or 
    * {@link PSTomcatConnector#SCHEME_HTTPS}  
    * 
    * @param serverFile The server.xml file in which the connectors are stored,
    * may not be <code>null</code>.
    * 
    * @return The list, never <code>null</code> or empty.
    * 
    * @throws IOException If there is an error reading from the file.     
    * @throws SAXException If the file is malformed.
    * @throws PSInvalidXmlException If the file format is invalid.
    */
   public static  List<IPSConnector> loadHttpConnectors(File serverFile) 
      throws IOException, SAXException, PSInvalidXmlException
   {
      if (serverFile == null)
         throw new IllegalArgumentException("serverFile may not be null");

      // load the doc
      Document doc = PSXmlUtils.getDocFromFile(serverFile);
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
      Element root = (Element)tree.getCurrent();
      if (root == null || !root.getNodeName().equals(SERVER_NODE_NAME))
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
            SERVER_NODE_NAME);
      
      Element serviceEl = tree.getNextElement(SERVICE_NODE_NAME, 
         tree.GET_NEXT_ALLOW_CHILDREN);
      if (serviceEl == null)
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
            SERVICE_NODE_NAME);
      
      int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT; 
      int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT; 

      List<IPSConnector> connList = new ArrayList<IPSConnector>();
      Element connEl = tree.getNextElement(
         PSTomcatConnector.CONNECTOR_NODE_NAME, firstFlag);
      while (connEl != null)
      {
         if (PSTomcatConnector.isHttpConnector(connEl)) {
            connList.add(new PSTomcatConnector(serverFile.toPath(),connEl, new HashMap()));
         }
         
         connEl = tree.getNextElement(PSTomcatConnector.CONNECTOR_NODE_NAME,
            nextFlag);
      }
      
      return connList;
   }


   /**
    * Saves the supplied list of connectors, replacing any existing connectors
    * in the file that have a scheme of {@link PSTomcatConnector#SCHEME_HTTP} or
    * {@link PSTomcatConnector#SCHEME_HTTPS}. Other connectors in the file are
    * unmodified.
    * 
    * @param serverFile The Tomcat server.xml file, never <code>null</code>.
    * @param connectors The connectors to write to the file, never 
    * <code>null</code> or empty.
    * 
    * @throws IOException If there is an error reading from the file.     
    * @throws SAXException If the file is malformed.
    * @throws PSInvalidXmlException If the file format is invalid. 
    */
   public static void saveHttpConnectors(File serverFile,
      List<IPSConnector> connectors) throws IOException, SAXException, 
         PSInvalidXmlException
   {
      if (serverFile == null)
         throw new IllegalArgumentException("serverFile may not be null");
      
      if (connectors == null || connectors.isEmpty())
         throw new IllegalArgumentException(
            "connectors may not be null or empty");
      
      // load the doc
      Document curDoc = PSXmlUtils.getDocFromFile(serverFile);
      
      // create a new one
      Document newDoc = PSXmlDocumentBuilder.createXmlDocument();
      
      // create new server element
      Element newServerEl = newDoc.createElement(SERVER_NODE_NAME);
      PSXmlDocumentBuilder.replaceRoot(newDoc, newServerEl);
      
      // find the old server element, copy attributes to new one
      PSXmlTreeWalker tree = new PSXmlTreeWalker(curDoc);
      Element root = (Element)tree.getCurrent();
      if (root == null || !root.getNodeName().equals(SERVER_NODE_NAME))
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
            SERVER_NODE_NAME);      
      copyAttributes(root, newServerEl);
      
      // create a new service element
      Element newServiceEl = PSXmlDocumentBuilder.addEmptyElement(newDoc, 
         newServerEl, SERVICE_NODE_NAME);
      
      // find the old service element, copy attributes to new one
      Element serviceEl = tree.getNextElement(SERVICE_NODE_NAME, 
         tree.GET_NEXT_ALLOW_CHILDREN);
      if (serviceEl == null)
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
            SERVICE_NODE_NAME);
      copyAttributes(serviceEl, newServiceEl);
      
      // append new connector list
      for (IPSConnector connector : connectors)
      {
         newServiceEl.appendChild(((PSTomcatConnector)connector).toXml());
      }      
      
      // walk the old doc, append non-http connectors and other elements
      int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT; 
      int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT; 

      Element testEl = tree.getNextElement(firstFlag);
      while (testEl != null)
      {
         // copy all but http connectors
         if (!(testEl.getNodeName().equals(
            PSTomcatConnector.CONNECTOR_NODE_NAME) && 
            PSTomcatConnector.isHttpConnector(testEl)))
         {
            PSXmlDocumentBuilder.copyTree(newDoc, newServiceEl, testEl);
         }
         
         testEl = tree.getNextElement(nextFlag);
      }
      
      // now save it
      PSXmlUtils.saveDocToFile(serverFile, newDoc);
   }

   /**
    * Get the port of the first AJP connector found in the specified file.
    * 
    * @param serverFile The file to check, may not be <code>null</code>.
    * 
    * @return The port, or <code>-1</code> if no AJP connector is found.
    * 
    * @throws IOException If there is an error reading from the file.     
    * @throws SAXException If the file is malformed.
    * @throws PSInvalidXmlException If the connector port value is invalid.
    */
   public static int getAJPConnectorPort(File serverFile) throws IOException, 
      SAXException, PSInvalidXmlException
   {
      if (serverFile == null)
         throw new IllegalArgumentException("serverFile may not be null");
      
      Document doc = PSXmlUtils.getDocFromFile(serverFile);
      Element ajpConnEl = getAJPConnectorElement(doc);

      if (ajpConnEl != null)
      {
         String sPort = ajpConnEl.getAttribute(PSTomcatConnector.PORT_ATTR);
         int port;
         try
         {
            port = Integer.parseInt(sPort);
         }
         catch (NumberFormatException e)
         {
            throw new PSInvalidXmlException(
               IPSXmlErrors.XML_ELEMENT_INVALID_ATTR, new String[] {
                  ajpConnEl.getTagName(), PSTomcatConnector.PORT_ATTR, sPort});
         }
         return port;
      }
      
      return -1;
   }

   /**
    * Search the doc for a connector element specifying the AJP protocol.
    * 
    * @param doc The doc to check, assumed not <code>null</code>.
    * 
    * @return The element, or <code>null</code> if not found.
    */
   private static Element getAJPConnectorElement(Document doc)
   {
      Element ajpConnEl = null;
      NodeList connectors = doc.getElementsByTagName(
         PSTomcatConnector.CONNECTOR_NODE_NAME);
      int index = 0;
      Element connEl = null;
      while ((connEl = (Element) connectors.item(index)) != null)
      {
         String protocol = connEl.getAttribute(PSTomcatConnector.PROTOCOL_ATTR);
         if (AJP_PROTOCOL.equals(protocol))
         {
            ajpConnEl = connEl;
            break;
         }
         index++;
      }
      return ajpConnEl;
   }
   
   /**
    * Set the port of the first AJP connector found in the specified file.  It 
    * is an error if none are found.
    * 
    * @param serverFile The file to update, may not be <code>null</code>.
    * @param port The port to update, must be > 0
    * 
    * @throws IOException If there is an error reading from or writing to the 
    * file.     
    * @throws SAXException If the file is malformed.
    * @throws PSInvalidXmlException If the specified connector element is not
    * found.
    */
   public static void setAJPConnectorPort(File serverFile, int port) 
      throws IOException, SAXException, PSInvalidXmlException
   {
      if (serverFile == null)
         throw new IllegalArgumentException("serverFile may not be null");
      
      if (port <= 0)
         throw new IllegalArgumentException("port must be > 0");
      
      Document doc = PSXmlUtils.getDocFromFile(serverFile);
      Element ajpConnEl = getAJPConnectorElement(doc);
      
      if (ajpConnEl == null)
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
            PSTomcatConnector.CONNECTOR_NODE_NAME);
      ajpConnEl.setAttribute(PSTomcatConnector.PORT_ATTR, String.valueOf(port));
      
      PSXmlUtils.saveDocToFile(serverFile, doc);
   }
   
   /**
    * Copy all attributes from the source to the target
    * 
    * @param source The source element, assumed not <code>null</code>.
    * @param target The target element, assumed not <code>null</code>.
    */
   private static void copyAttributes(Element source, Element target)
   {
      NamedNodeMap attrs = source.getAttributes();
      int len = attrs.getLength();
      for (int i = 0; i < len; i++)
      {
         Attr attr = (Attr) attrs.item(i);
         target.setAttribute(attr.getName(), attr.getValue());
      }
   }
   
   /**
    * Constant for the protocol used by the AJP connector.
    */
   private static final String AJP_PROTOCOL = "AJP/1.3";
   
   // private XML constants
   private static final String SERVER_NODE_NAME = "Server";
   private static final String SERVICE_NODE_NAME = "Service";
}
