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
