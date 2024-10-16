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
package com.percussion.i18n.rxlt;

import com.percussion.i18n.PSTmxResourceBundle;
import com.percussion.i18n.tmxdom.IPSTmxDocument;
import com.percussion.i18n.tmxdom.IPSTmxHeader;
import com.percussion.i18n.tmxdom.PSTmxDocument;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

/**
 * This class handles the process of merging a translated TMX document with that
 * on Rhythmyx Server. Uses a specific merge configuration document while merging.
 */
public class PSMergeWithMaster extends PSIdleDotter
   implements IPSActionHandler
{
   /**
    * Name of the merge configuration file to be used while merging the local
    * TMX document with that on the server.
    */
   static final String MERGE_CONFIG_FILE = "configmergewithmaster.xml";

   /**
    * DOM document of the merge configuration file to be used while merging the
    * local TMX document with that on the server. Initialized in the constructor.
    * Never <code>null</code> after that.
    */
   protected Document m_DocConifigMergeWithMaster = null;

   /**
    * Constructor. Initializes the merge configuration document by loading from
    * the JAR file.
    * @throws PSActionProcessingException if the configuration document could
    * not be loaded from the jar file
    */
   public PSMergeWithMaster()
      throws PSActionProcessingException
   {
      try
      {
         try(InputStream ir = getClass().getResourceAsStream(MERGE_CONFIG_FILE) ) {
            m_DocConifigMergeWithMaster = PSXmlDocumentBuilder.createXmlDocument(ir
                    , false);
         }
      }
      catch (Exception e) //potentially IOException, SAXException
      {
         throw new PSActionProcessingException(e.getMessage());
      }
   }

   /*
    * Implementation of the method defined in the interface.
    * See {@link IPSSectionHandler#process(Element)} for details about this
    * method.
    */
   public void process(Element cfgdata)
      throws PSActionProcessingException
   {
      if(cfgdata == null)
      {
         throw new IllegalArgumentException(
            "Configuration element must not be null");
      }
      try
      {
         PSCommandLineProcessor.logMessage("processingAction",
            cfgdata.getAttribute(PSRxltConfigUtils.ATTR_NAME));

         //start displaying idle dots
         showDots(true);

         String rxroot = cfgdata.getOwnerDocument().getDocumentElement()
            .getAttribute(PSRxltConfigUtils.ATTR_RXROOT);
         Document destDoc = PSTmxResourceBundle.getMasterResourceDoc(rxroot);
         IPSTmxDocument destTmxdoc = new PSTmxDocument(destDoc);
         destTmxdoc.setMergeConfigDoc(m_DocConifigMergeWithMaster);

         String path = cfgdata.getAttribute(PSRxltConfigUtils.ATTR_FILE_PATH);
         File file = new File(path);

         if (file.exists())
         {
            try (FileInputStream fis = new FileInputStream(file)) {
               try(InputStreamReader ir = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                  Document srcDoc = PSXmlDocumentBuilder.createXmlDocument(ir
                          , false);
                  IPSTmxDocument srcTmxdoc = new PSTmxDocument(srcDoc, false);
                  destTmxdoc.merge(srcTmxdoc);
               }
            }
         }
         //make sure to set the language tool name and version in the header
         IPSTmxHeader header = destTmxdoc.getHeader();
         header.setProperty(IPSTmxHeader.PROP_CREATION_TOOL,
            PSRxltMain.PROGRAM_NAME);
         header.setProperty(IPSTmxHeader.PROP_CREATION_TOOL_VERSION,
            PSRxltMain.getVersionNumberString());

         //save the resource file creating a backup copy
         destTmxdoc.save(
            new File(rxroot, PSTmxResourceBundle.MASTER_RESOURCE_FILEPATH), true);
         //stop displaying idle dots
         showDots(false);
         //Just disply blank line
         System.out.println();
      }
      catch(Exception e)//SAXException, IOException
      {
         PSCommandLineProcessor.logMessage("errorMessageException",
            e.getMessage());
         throw new PSActionProcessingException(e.getMessage());
      }
      finally
      {
         endDotSession();
      }
      PSCommandLineProcessor.logMessage("processFinished", "");
   }
}
