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
package com.percussion.i18n.tmxdom;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * This class provides implementation for the merge configuration interface
 * which provides rules to merge different kinds of TMX nodes. The unique
 * nodeids for specifying the configuration parameters are based on the current
 * and its parent element names.
 * @see IPSTmxDtdConstants
 */
public class PSTmxMergeConfig
   implements IPSTmxMergeConfig
{
   /**
    * Constructor. Loads the default merge configuration document from the class
    * file archive and builds map of configuration parameter property map.
    * @throws IOException
    * @throws SAXException
    */
   public PSTmxMergeConfig()
      throws IOException, SAXException
   {
      try(InputStream is =
         getClass().getResourceAsStream(DEFAULT_MERGE_CONFIG_FILE_NAME)) {
         setConfigDoc(PSXmlDocumentBuilder.createXmlDocument(is, false));
      }
   }

   /**
    * Constructor. Takes the DOM document specifying the merge configuration and
    * builds the map of parameter maps.
    * @param doc Must not be <code>null</code>.
    * @throws IllegalArgumentException
    */
   public PSTmxMergeConfig(Document doc)
   {
      setConfigDoc(doc);
   }

   /*
    * Implementation of the method defined in the interface.
    */
   public void setConfigDoc(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc must not be null");

      m_PSTmxConfigParams = new HashMap();
      //Process "tu" element
      Element tu = processConfigElement(doc.getDocumentElement(),
         IPSTmxDtdConstants.ELEM_TU, MERGE_NODEID_TU);

      //Process "tu/note" element
      processConfigElement(tu,
         IPSTmxDtdConstants.ELEM_NOTE, MERGE_NODEID_TU_NOTE);

      //Process "tu/prop" element
      processConfigElement(tu,
         IPSTmxDtdConstants.ELEM_PROP, MERGE_NODEID_TU_PROPERTY);

      //Process "tuv" element
      Element tuv = processConfigElement(tu,
         IPSTmxDtdConstants.ELEM_TUV, MERGE_NODEID_TUV);

      //Process "tuv/note" element
      processConfigElement(tuv,
         IPSTmxDtdConstants.ELEM_NOTE, MERGE_NODEID_TUV_NOTE);

      //Process "tu/prop" element
      processConfigElement(tuv,
         IPSTmxDtdConstants.ELEM_PROP, MERGE_NODEID_TUV_PROPERTY);

      //Process "tu/seg" element
      processConfigElement(tuv,
         IPSTmxDtdConstants.ELEM_SEG, MERGE_NODEID_SEGMENT);
   }

   /**
    * Helper method to process the given element for the configuration
    * parameters.
    * @param parent Parent DOM element whose child happens to be the element
    * with name supplied as second parameter.
    * @param elemName Name of the element for which the configuration parameter
    * map is to be built.
    * @param nodeId one of the nodeids defined in {@link IPSTmxMergeConfig}
    * @return the DOM element with name as in the second parameter, 
    * never <code>null</code>
    */
   private Element processConfigElement(
      Element parent, String elemName, String nodeId)
   {
      PSTmxConfigParams configParams = new PSTmxConfigParams();
      m_PSTmxConfigParams.put(nodeId, configParams);

      NodeList nl = parent.getElementsByTagName(elemName);
      Element child = (Element)nl.item(0);
      nl = child.getElementsByTagName("config");
      Element cfg = (Element)nl.item(0);
      //process
      nl = cfg.getElementsByTagName("param");
      Element param = null;
      String name;
      String value;
      Node node = null;
      for(int i=0; nl!=null && i<nl.getLength(); i++)
      {
         param = (Element)nl.item(i);
         name = param.getAttribute("name");
         node = param.getFirstChild();
         if(node instanceof Text)
         {
            value = ((Text)node).getData();
         }
         else
            value = "";
         configParams.addParam(name, value);
      }
      return child;
   }

   /*
    * Implementation of the method defined in the interface.
    */
   public PSTmxConfigParams getConfigParams(String nodeId)
   {
      return (PSTmxConfigParams)m_PSTmxConfigParams.get(nodeId);
   }

   /**
    * Map of nodeid-parameter map pairs defined in the configuration document.
    * Nodeid locates the node for which the merge configuration is specified.
    * Built when the configuration XML document is set, never <code>null</code>
    * after that.
    */
   protected Map m_PSTmxConfigParams = null;

   /**
    * String constant representing the default merge config file name. This file
    * is shipped as part of the JAR file.
    */
   static public final String DEFAULT_MERGE_CONFIG_FILE_NAME =
      "defaultmergeconfig.xml";
}
